package mlaas;

import java.util.*;

/**
 * Manager for taking a job group, creating a task plan, and executing those tasks in DL4J/Spark accordingly.
 */
public class LearningManager {
	
	private JobGroup jobGroup;
	private TaskPlan taskPlan;


	/**
	 * Constructor for LearningManager with a given JobGroup.
	 * @param jobGroup The group to be managed.
	 */
	public LearningManager(JobGroup jobGroup) {
		this.setJobGroup(jobGroup);
	}


	/**
	 * Setter for jobGroup. Creates a task plan immediately.
	 * @param jobGroup The group to be managed.
	 */
	public void setJobGroup(JobGroup jobGroup) {
		this.jobGroup = jobGroup;

		// Create task plan immediately
		this.taskPlan = new TaskPlan(this.jobGroup);
	}


	/**
	 * Begins executing tasks on DL4J/Spark.
	 */
	public void run() {
		// Call all initial tasks.
	}


	/**
	 * Executes a single task on DL4J/Spark.
	 * @param task The task to be executed.
	 */
	private void executeTask(Task task) {
		// 1. Check if task is ready to be executed.
		// 2. Load relevant data for task
		// 3. Run on DL4J/Spark
		// 4. When finished, call all following tasks.
	}
}
