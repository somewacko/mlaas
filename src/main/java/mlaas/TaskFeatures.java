package mlaas;

import java.util.*;

/**
 * Tasks based on common features.
 */
public class TaskFeatures extends Task {

	public TaskFeatures(Collection<Job> jobs) {
		super(jobs);
	}

	public TaskFeatures(Task task) throws RuntimeException {
		super(task);

		if (!(task instanceof TaskFeatures))
			throw new RuntimeException("Cannot create a copy from a Task of a different subclass.");
	}

	protected Set<DataUnit> extractWork(Job job) {
		return new HashSet<DataUnit>(job.getFeatures());
	}
}
