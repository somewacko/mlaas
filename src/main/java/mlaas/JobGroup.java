package mlaas;

import java.util.*;

/**
 * A grouping of jobs that have shared tasks that can be run together.
 */
public class JobGroup {

	private Collection<Job> jobs; 	// The list of jobs in this group
	private JobGroupType type;	// The type of group this is

	/**
	 * Constructor for a JobGroup with a set of jobs with common tasks and a given type.
	 * @param jobs the set of jobs in this job group.
	 * @param type the type of job group that this is.
	 */
	public JobGroup(Collection<Job> jobs, JobGroupType type) {
		this.jobs = jobs;
		this.type = type;
	};

	/**
	 * Finds the set of all work in this group.
	 * @return The set of all work.
	 */
	public Set<DataUnit> allWork() {

		Set<DataUnit> allWork = new HashSet<>();

		for (Job job : this.jobs) {
			switch (this.type) {
				case SharedFeatures:
					allWork.addAll(job.getFeatures());
					break;
				case SharedSamples: default:
					allWork.addAll(job.getSamples());
			}
		}
		return allWork;
	}

	/**
	 * Extracts the appropriate unit of work from a given job.
	 * @param job
	 * @return The appropriate work for this task.
	 */
	public Set<DataUnit> extractWork(Job job) {
		switch (this.type) {
			case SharedFeatures:
				return new HashSet<DataUnit>(job.getFeatures());
			case SharedSamples: default:
				return new HashSet<DataUnit>(job.getSamples());
		}
	}

	// Getters and setters

	public Collection<Job> getJobs() { return this.jobs; }
	public JobGroupType getType() { return this.type; }
}
