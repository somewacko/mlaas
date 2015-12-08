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
		this.taskPlan = new TaskPlan(jobGroup);
	}


	/**
	 * Begins executing tasks on DL4J/Spark.
	 */
	public void run() throws RuntimeException {
		// Call all initial tasks.

		if (this.taskPlan == null)
			throw new RuntimeException("A job group must be assigned first.");

		for (Task task : this.taskPlan.getStartingTasks())
			this.executeTask(task, null);
	}


	/**
	 * Executes a single task on DL4J/Spark.
	 * @param task The task to be executed.
	 * @param model The DNN model from prior tasks. If null, new weights will be initialized.
	 */
	private void executeTask(Task task, DNNModel model) {

		DNNModel newModel = model;

		if (!task.getWork().isEmpty())
			newModel = runSparkTrainingJob(task, model);

		if (!task.getNextTasks().isEmpty()) {
			for (Task nextTask : task.getNextTasks())
				executeTask(nextTask, newModel);
		}
		else {
			Results results = evaluateSparkModel(task.getEndJob(), model);
			task.getEndJob().saveLearningResults(results);
		}
	}

	/**
	 * Launches a Spark/DL4J job given a data file and prior DNN model.
	 * @param task
	 * @param model
	 * @return The new DNNModel
	 */
	private DNNModel runSparkTrainingJob(Task task, DNNModel model) {

		String dataFilename = FeatureManager.formTrainingData(task);

		// TODO: Actually launch job on Spark

		return new DNNModel();
	}

	/**
	 * Evaluates a model on test data for a given job.
	 * @param job
	 * @param model
	 * @return
	 */
	private Results evaluateSparkModel(Job job, DNNModel model) {

		String dataFilename = FeatureManager.formTestingData(job);

		// TODO: Actually launch job on Spark

		Results results = new Results();
		results.model = model;
		results.accuracy = 100; // :D
		return results;
	}
}
