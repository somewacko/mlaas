package mlaas;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Object to represent an atomic piece of work to perform for a job.
 *
 * These tasks allow a set of jobs to be decomposed into units of work to perform, where some tasks can be performed
 * once to satisfy multiple jobs with common requirements.
 */
abstract public class Task {

	static int next_id = 0;

	private int id;

	private Set<Job> jobs;

	private Task lastTask;
	private Set<Task> nextTasks;

	private Set<DataUnit> work; // Either samples or features

	private Job endJob;

	public Boolean isValid = false;

	/**
	 * Constructor for a Task, given a set of jobs.
	 * @param jobs The jobs to create a task from.
	 */
	public Task(Collection<Job> jobs) {
		this.jobs = new HashSet<>(jobs);

		// Find the common work between all jobs by finding the intersection of all of their work.
		for (Job job : this.jobs) {

			if (this.work == null)
				this.work = this.extractWork(job);
			else
				this.work.retainAll(this.extractWork(job));
		}

		// If this task only has one Job associated with it, make this task terminating for the job.
		if (jobs.size() == 1)
			this.endJob = jobs.iterator().next();

		this.lastTask = null;
		this.nextTasks = new HashSet<Task>();

		this.id = Task.next_id;
		Task.next_id += 1;
	}

	/**
	 * Constructor for a Task to create a copy of another task.
	 * @param task The task to create a copy from.
	 */
	public Task(Task task) {
		this.jobs = new HashSet<>(task.getJobs());
		this.work = new HashSet<>(task.getWork());
		this.endJob = task.getEndJob();
		this.lastTask = task.getLastTask();
		this.nextTasks = new HashSet<>(task.getNextTasks());
	}

	/**
	 * Extracts the appropriate unit of work from a given job to define this task. For example, if this was a task
	 * based on common features, then the extracted work would be the features for the job.
	 * @param job
	 * @return The appropriate work for this task.
	 */
	abstract protected Set<DataUnit> extractWork(Job job);

	abstract public Set<DataFeature> getFeatures();
	abstract public Set<DataSample> getSamples();

	/**
	 * Removes work from the task's set of work.
	 * @param workToRemove
	 */
	public void removeWork(Set<DataUnit> workToRemove) {
		this.work.removeAll(workToRemove);
	}


	// Getters and setters

	public Set<Job> getJobs() { return this.jobs; }

	public void setLastTask(Task task) { this.lastTask = task; }

	public Task getLastTask() {
		return this.lastTask;
	}

	public void addNextTask(Task task) {
		this.nextTasks.add(task);
	}

	public Set<Task> getNextTasks() {
		return this.nextTasks;
	}

	public Set<DataUnit> getWork() {
		return this.work;
	}

	public Job getEndJob() {
		return this.endJob;
	}

	public int getId() {
		return this.id;
	}
}
