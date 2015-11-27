package mlaas;

import java.util.HashSet;
import java.util.Set;

/**
 * Tasks based on common samples.
 */
public class TaskSamples extends Task {

	public TaskSamples(Set<Job> jobs) {
		super(jobs);
	}

	protected Set<DataUnit> extractWork(Job job) {
		return new HashSet<DataUnit>(job.getSamples());
	}
}
