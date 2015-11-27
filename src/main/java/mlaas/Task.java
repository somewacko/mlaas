package mlaas;

import java.util.HashSet;
import java.util.Set;

/**
 * Object to represent an atomic piece of work to perform for a job.
 *
 * These tasks allow a set of jobs to be decomposed into units of work to perform, where some tasks can be performed
 * once to satisfy multiple jobs with common requirements.
 */
abstract public class Task {

	private Set<Job> jobs;

	private Task lastTask;
	private Set<Task> nextTasks;

	private Set<DataUnit> work;

	private Job endJob;


	/**
	 * Constructor for a Task, given a set of jobs.
	 * @param jobs
	 */
	public Task(Set<Job> jobs) {
		this.jobs = jobs;

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
	}


	/**
	 * Extracts the appropriate unit of work from a given job to define this task. For example, if this was a task
	 * based on common features, then the extracted work would be the features for the job.
	 * @param job
	 * @return The appropriate work for this task.
	 */
	abstract protected Set<DataUnit> extractWork(Job job);


	// Getters and setters

	public void setLastTask(Task task) {
		this.lastTask = task;
	}

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
}
