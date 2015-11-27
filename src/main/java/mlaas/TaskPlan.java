package mlaas;

import mlaas.util.PowerSet;

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
	public TaskPlan(JobGroup jobGroup) throws RuntimeException {

		if (jobGroup.getJobs().size() <= 8)
			this.generateTaskPlan(jobGroup);
		else
			throw new RuntimeException("Task plans cannot be run with more than 8 jobs.");
	}


	/**
	 * Generates the logical plan based on the current tasks.
	 * @param jobGroup The job group to create a task plan from.
	 */
	private void generateTaskPlan(final JobGroup jobGroup) throws RuntimeException {

		List<Task> tasks = this.findCommonTasks(jobGroup);

		// Set up breadth-first search

		Queue<Task> taskQueue = new LinkedList<>();
		Queue<Map<Job, List<Task>>> remainingTasksQueue = new LinkedList<>();

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

			startingTasks.add(task);
			taskQueue.add(task);
			remainingTasksQueue.add(remainingTasks);

			for (Job job : task.getJobs()) {

				remainingTasks.put(job, new LinkedList<Task>());

				for (Task t : tasks)
					if (t != currentTask && t.getJobs().contains(job))
						remainingTasks.get(job).add(t);

				jobsLeft.remove(job);
			}

			if (jobsLeft.isEmpty())
				break;
		}

		// Perform BFS

		while (!taskQueue.isEmpty()) {
			Task task = taskQueue.remove();
			Map<Job, List<Task>> remainingTasks = remainingTasksQueue.remove();

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

						taskQueue.add(newTask);
						remainingTasksQueue.add(newRemainingTasks);
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
	 * TODO: Optimize this so it's not O(2^N) (from having to find the power set of jobs)
	 *
	 * @param jobGroup
	 * @return A list of tasks with common work.
	 */
	private List<Task> findCommonTasks(JobGroup jobGroup) {

		final List<Task> tasks = new LinkedList<Task>();

		// Create tasks from all combinations of jobs that have common work

		for (List<Job> ps : PowerSet.findPowerSet(jobGroup.getJobs())) {

			Task newTask = TaskFactory.createTask(ps, jobGroup.getType());

			if (newTask.getWork().size() > 0)
				tasks.add(newTask);
		}

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
