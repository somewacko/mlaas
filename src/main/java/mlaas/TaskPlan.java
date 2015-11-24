package mlaas;

import  java.util.List;

/**
 * A logical plan for how tasks should be carried out such that the number of training passes is minimized.
 */
public class TaskPlan {

	private List<Task> tasks;

	/**
	 * Constructor for TaskPlan with a given JobGroup.
	 * @param jobGroup The job group to create a task plan from.
 	 */
	public TaskPlan(JobGroup jobGroup) {
		// Create tasks from group...
		this.generateTaskPlan();
	}

	/**
	 * Constructor for TaskPlan with a given set of tasks.
	 * @param tasks The tasks to create a task plan from.
	 */
	public TaskPlan(List<Task> tasks) {
		this.tasks = tasks;

		// Immediately generate the actual plan for the tasks
		this.generateTaskPlan();
	}


	/**
	 * Generates the logical plan based on the current tasks.
	 */
	private void generateTaskPlan() {
		// ...
	}
}
