package mlaas;

import java.util.List;

/**
 * A grouping of jobs that have shared tasks that can be run together.
 */
public class JobGroup {

	private List<Job> jobs; 	// The list of jobs in this group
	private JobGroupType type;	// The type of group this is

	/**
	 * Constructor for a JobGroup with a set of jobs with common tasks and a given type.
	 * @param jobs the set of jobs in this job group.
	 * @param type the type of job group that this is.
	 */
	public JobGroup(List<Job> jobs, JobGroupType type) {
		this.jobs = jobs;
		this.type = type;
	};

	public List<Job> getJobs() { return this.jobs; }
	public JobGroupType getType() { return this.type; }
}
