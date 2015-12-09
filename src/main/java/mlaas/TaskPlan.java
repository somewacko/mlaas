package mlaas;


import java.util.*;

/**
 * A logical plan for how tasks should be carried out such that the number of training passes is minimized.
 */
public class TaskPlan {

	private List<Task> startingTasks;
	private Map<Job, Task> terminatingTasks;

	/**
	 * Constructor for TaskPlan with a given JobGroup.
	 * @param jobGroup The job group to create a task plan from.
 	 */
	public TaskPlan(JobGroup jobGroup) {
		this.generateTaskPlan(jobGroup);
	}


	/**
	 * Generates the logical plan based on the current tasks.
	 * @param jobGroup The job group to create a task plan from.
	 */
	private void generateTaskPlan(final JobGroup jobGroup) throws RuntimeException {

		// Set up helper classes

		// Class to represent a node in the task plan - contains the task associated for the node and all of the
		// remaining work that still has to be done for that node.
		class TaskNode {
			Task task;
			Map<Job, List<Task>> remainingTasks;
			int distance = Integer.MAX_VALUE;

			public TaskNode(Task task, Map<Job, List<Task>> remainingTasks) {
				this.task = task;
				this.remainingTasks = remainingTasks;

				for (List<Task> list : this.remainingTasks.values())
					this.distance = Math.min(list.size(), this.distance);
			}
		}

		// Comparator for priority queue for best-first search, using the distance from a terminating node as the
		// primary heuristic.
		class TaskNodeComparator implements Comparator<TaskNode> {
			@Override
			public int compare(TaskNode x, TaskNode y) {
				if (x.distance < y.distance)
					return -1;
				else if (x.distance > y.distance)
					return 1;
				else
					return 0;
			}
		}

		// Get all tasks with common work

		List<Task> tasks = this.findCommonTasks(jobGroup);

		// Set up for best-first search

		PriorityQueue<TaskNode> queue = new PriorityQueue<>(new TaskNodeComparator());

		Map<Job, Task> terminatingTasks = new HashMap<Job, Task>(){{
			for (Job job : jobGroup.getJobs())
				put(job, null);
		}};

		// Find starting tasks

		List<Task> startingTasks = new LinkedList<>();
		Set<Job> jobsLeft = new HashSet<>(jobGroup.getJobs());

		for (Task currentTask : tasks) {
			Task task = TaskFactory.createTask(currentTask);

			Map<Job, List<Task>> remainingTasks = new HashMap<>();

			for (Job job : task.getJobs()) {

				remainingTasks.put(job, new LinkedList<Task>());

				for (Task t : tasks)
					if (t != currentTask && t.getJobs().contains(job))
						remainingTasks.get(job).add(t);

				jobsLeft.remove(job);
			}

			queue.add(new TaskNode(task, remainingTasks));
			startingTasks.add(task);

			if (jobsLeft.isEmpty())
				break;
		}

		// Perform best-first search

		while (!queue.isEmpty()) {

			TaskNode node = queue.remove();

			Task task = node.task;
			Map<Job, List<Task>> remainingTasks = node.remainingTasks;

			if (task.getEndJob() != null) {
				if (terminatingTasks.get(task.getEndJob()) == null) {
					terminatingTasks.put(task.getEndJob(), task);
				}
			}
			else {
				for (Job job : task.getJobs()) {

					if (remainingTasks.keySet().contains(job) && terminatingTasks.get(job) == null) {

						List<Task> tasksForJob = remainingTasks.get(job);

						Task nextTask = tasksForJob.get(0);

						Task newTask = TaskFactory.createTask(nextTask);

						task.addNextTask(newTask);
						newTask.setLastTask(task);

						// If the next task is terminating but has no associated work, set this as a terminating task
						// immediately (since the current task is effectively the terminating task for this job).
						if (newTask.getEndJob() != null && newTask.getWork().isEmpty()) {
							if (terminatingTasks.get(newTask.getEndJob()) == null)
								terminatingTasks.put(newTask.getEndJob(), newTask);
							continue;
						}

						// Copy over remaining tasks, sans the next task.

						Map<Job, List<Task>> newRemainingTasks = new HashMap<>();

						for (Job j : newTask.getJobs()) {
							if (remainingTasks.keySet().contains(j)) {
								newRemainingTasks.put(j, new LinkedList<Task>());

								for (Task t : remainingTasks.get(j)) {
									if (t != nextTask)
										newRemainingTasks.get(j).add(t);
								}
							}
						}

						// Add to queues

						queue.add(new TaskNode(newTask, newRemainingTasks));
					}
				}
			}
		}

		// From the terminating tasks, bubble up the tree and color all tasks along the way to mark which ones are
		// valid.

		for (Job job : terminatingTasks.keySet()) {
			Task task = terminatingTasks.get(job);
			task.isValid = true;

			while (task.getLastTask() != null) {
				task = task.getLastTask();
				task.isValid = true;
			}
		}

		// From the starting tasks, go down the tree and remove all tasks that are not valid.

		List<Task> tasksToRemove = new LinkedList<>();

		for (Task task : startingTasks) {
			this.removeInvalidTasks(task);

			if (!task.isValid)
				tasksToRemove.add(task);
		}
		startingTasks.removeAll(tasksToRemove);

		// We're done! Return the starting tasks.

		this.startingTasks = startingTasks;
		this.terminatingTasks = terminatingTasks;
	}


	/**
	 * Removes all invalid tasks from the current task.
	 * @param task The task to remove invalid tasks from.
	 */
	private void removeInvalidTasks(Task task) {

		List<Task> tasksToRemove = new LinkedList<>();

		for (Task t : task.getNextTasks()) {
			if (t.isValid)
				this.removeInvalidTasks(t);
			else
				tasksToRemove.add(t);
		}
		task.getNextTasks().removeAll(tasksToRemove);
	}


	/**
	 * Finds the set of tasks with common work within a job group, ordered from tasks that satisfy the most number of
	 * jobs to tasks that satisfy the least.
	 *
	 * @param jobGroup
	 * @return A list of tasks with common work.
	 */
	private List<Task> findCommonTasks(JobGroup jobGroup) {

		final List<Task> tasks = new ArrayList<>();

		Set<DataUnit> allWork = jobGroup.allWork();

		List<Job> relatedJobs = new LinkedList<>();

		// Go through each work present in the group
		for (DataUnit work : allWork) {

			relatedJobs.clear();

			// Find the jobs which have this work in common
			for (Job job : jobGroup.getJobs()) {
				if (jobGroup.extractWork(job).contains(work))
					relatedJobs.add(job);
			}

			// Add this job group if there are more than one jobs associated with it (tasks with only one job will
			// be added later)
			if (relatedJobs.size() > 1)
				tasks.add( TaskFactory.createTask(relatedJobs, jobGroup.getType()) );
		}
		// Add tasks which only have one job associated with them
		for (Job job : jobGroup.getJobs()) {
			tasks.add( TaskFactory.createTask(Arrays.asList(job), jobGroup.getType()) );
		}

		// Sort tasks from tasks with the most to least number of associated jobs
		Collections.sort(tasks, new Comparator<Task>() {
			@Override
			public int compare(Task o1, Task o2) {
				if (o1.getJobs().size() > o2.getJobs().size())
					return -1;
				else if (o1.getJobs().size() < o2.getJobs().size())
					return  1;
				else
					return 0;
			}
		});

		// Starting from the top, remove all work in the current task from smaller-sized tasks

		for (ListIterator<Task> outerIter = tasks.listIterator(); outerIter.hasNext(); ) {
			Task task = outerIter.next();

			for (ListIterator<Task> innerIter = tasks.listIterator(outerIter.nextIndex()); innerIter.hasNext(); ) {
				Task lowerTask = innerIter.next();
				lowerTask.removeWork(task.getWork());
			}
		}

		// Remove all tasks that do not have any work

		tasks.removeAll( new LinkedList<Task>(){{
			for (Task task : tasks) {
				if (task.getWork().isEmpty() && task.getJobs().size() > 1)
					add(task);
			}
		}});

		return tasks;
	}


	// Getters and setters

	public List<Task> getStartingTasks() {
		return this.startingTasks;
	}

	public Map<Job, Task> getTerminatingTasks() {
		return this.terminatingTasks;
	}
}
