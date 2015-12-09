package mlaas;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
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
	private void executeTask(Task task, DNNModel model){

		DNNModel newModel = null;

		if (!task.getWork().isEmpty())
			newModel = runSparkTrainingJob(task, model);

		if (!task.getNextTasks().isEmpty()) {
			for (Task nextTask : task.getNextTasks())
				executeTask(nextTask, newModel);
		}
		else {
			Results results = evaluateSparkModel(task.getEndJob(), newModel);
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

		String inputFile = FeatureManager.formTrainingData(task);
		String sparkPath="/usr/local/Cellar/apache-spark/1.5.2/bin/spark-submit";
		if(model == null  || model.isEmpty()){
			//train new model
			//String inputFile = "/local/BigData/Data/inputFile"+task.getId()+".txt ";
			String outputModelConf = "/local/BigData/Models/model"+task.getId()+" ";
			String outputModelWeights = "/local/BigData/Models/weight"+task.getId()+" ";
			//String outputStats = " ";
			String command=sparkPath+" --class edu.jhu.bdslss.baft.BptiSparkTrain DL4JSparkJAR/MLAAS-1.0-SNAPSHOT.jar" +
					"-input_file " + inputFile + " -output_model_conf_file " + outputModelConf +
					" -output_model_weights_file " + outputModelWeights; // + "-output_stats_file " + outputStats;
			runCommandLineSpark(command);
			return new DNNModel(outputModelConf,outputModelWeights);
		}
		else {
			//further fit existing model
			//String inputFile = "/local/BigData/Data/inputFile"+task.getId()+".txt ";
			String outputModelConf = "/local/BigData/Models/model" + task.getId() + " ";
			String outputModelWeights = "/local/BigData/Models/weight" + task.getId() + " ";
			//String outputStats = " ";
			String command = sparkPath+" --class edu.jhu.bdslss.baft.BptiUsedSpark DL4JSparkJAR/MLAAS-1.0-SNAPSHOT.jar" +
					"-input_file " + inputFile + " -input_model_conf_file " + model.getModelPath() +
					"  -input_model_weights_file " + model.getWeightPath() + " -output_model_conf_file " + outputModelConf +
					" -output_model_weights_file " + outputModelWeights; // + "-output_stats_file " + outputStats;
			runCommandLineSpark(command);
			return new DNNModel(outputModelConf,outputModelWeights);
		}
	}

	/**
	 * Evaluates a model on test data for a given job.
	 * @param job
	 * @param model
	 * @return
	 */
	private Results evaluateSparkModel(Job job, DNNModel model) {
		// Actually launch job on Spark
		String inputFile = FeatureManager.formTestingData(job);
		String outputStats = "/local/BigData/Results/stats"+job.getId()+".dat";
		String sparkPath="/usr/local/Cellar/apache-spark/1.5.2/bin/spark-submit";
		String command = sparkPath+" --class edu.jhu.bdslss.baft.BptiSparkTest DL4JSparkJAR/MLAAS-1.0-SNAPSHOT.jar" +
				"-input_file " + inputFile + " -input_model_conf_file " + model.getModelPath() +
				"  -input_model_weights_file " + model.getWeightPath() + " -output_stats_file " + outputStats;
		runCommandLineSpark(command);
		Results result = new Results();
		result.model = model;
		//results.accuracy = new Double(100); // :D
		BufferedReader b = new BufferedReader(new FileReader(new File("/local/BigData/results/stats"+job.getId()+".dat")));
		StringBuffer stringBuffer = new StringBuffer("");
		String line="";
		try {
			while ((line = b.readLine()) != null) {
				stringBuffer.append(line);
			}
			result.stats = stringBuffer.toString();
		}
		catch(Exception e){
			System.out.println("Error in reading results");
			result.stats ="";
		}
		return result;
	}
	private void runCommandLineSpark(String command){
		Process p=null;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			//System.out.println("Success");
		}
		catch(Exception e) {
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = "";
			StringBuffer stringBuffer = new StringBuffer("");
			try {
				while ((line = b.readLine()) != null) {
					stringBuffer.append(line);
				}
				System.out.println( stringBuffer.toString());
			}
			catch(Exception e1){
				System.out.println("Failed to write error in running Spark)";
			}
		}
	}
}
