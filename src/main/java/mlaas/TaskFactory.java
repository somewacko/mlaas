package mlaas;

import java.util.*;

/**
 * Factory class to create appropriate tasks based on task type.
 */
public class TaskFactory {

	public static Task createTask(Collection<Job> jobs, JobGroupType type) {
		switch (type) {
			case SharedFeatures:
				return new TaskFeatures(jobs);
			case SharedSamples: default:
				return new TaskSamples(jobs);
		}
	}

	public static Task createTask(Task task) throws RuntimeException {
		if (task instanceof TaskFeatures) {
			return new TaskFeatures(task);
		}
		else if (task instanceof TaskSamples) {
			return new TaskSamples(task);
		}
		else {
			throw new RuntimeException("Provided task is neither of common features or samples.");
		}
	}
}
