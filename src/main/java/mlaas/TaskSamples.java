package mlaas;

import java.util.*;

/**
 * Tasks based on common samples.
 */
public class TaskSamples extends Task {

	public TaskSamples(Collection<Job> jobs) {
		super(jobs);
	}

	public TaskSamples(Task task) throws RuntimeException {
		super(task);

		if (!(task instanceof TaskSamples))
			throw new RuntimeException("Cannot create a copy from a Task of a different subclass.");
	}

	protected Set<DataUnit> extractWork(Job job) {
		return new HashSet<DataUnit>(job.getSamples());
	}
}
