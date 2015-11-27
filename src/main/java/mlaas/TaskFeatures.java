package mlaas;

import java.util.HashSet;
import java.util.Set;

/**
 * Tasks based on common features.
 */
public class TaskFeatures extends Task {

	public TaskFeatures(Set<Job> jobs) {
		super(jobs);
	}

	protected Set<DataUnit> extractWork(Job job) {
		return new HashSet<DataUnit>(job.getFeatures());
	}
}
