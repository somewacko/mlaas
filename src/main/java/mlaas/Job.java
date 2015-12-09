package mlaas;

import java.util.*;

/**
 * A job requested by a user to train a model on a given data set and some subset of available samples and features.
 *
 * Will be grouped together with other jobs later on to share training tasks to make training more efficient.
 */
public class Job {

	static int next_id = 0;

	private int id;
	private DataSet dataSet;
	private Set<DataSample> samples;
	private Set<DataFeature> features;
	private JobStatus status = JobStatus.Waiting;

	private Results results;

	/**
	 * Constructor for a Job with given specifications.
	 * @param dataSet The data set for this job to train on.
	 * @param samples Which samples this job should train on.
	 * @param features Which features this job should train on.
	 */
	public Job(DataSet dataSet, Collection<DataSample> samples, Collection<DataFeature> features) {
		this.dataSet = dataSet;
		this.samples = new HashSet<>(samples);
		this.features = new HashSet<>(features);

		this.id = Job.next_id;
		Job.next_id += 1;
	}

	/**
	 * Sets results from the learning task.
	 * @param results The classification results.
	 */
	public void saveLearningResults(Results results) {
		this.results = results;

		// TODO: Save model and results, return to user.

		this.status = JobStatus.Finished;
	}

	/**
	 * Indicates that there was an error while learning.
	 */
	public void markError() {
		this.status = JobStatus.Error;
	}

	// Getters and Setters

	public DataSet getDataSet() { return this.dataSet; }
	public Set<DataSample> getSamples() { return this.samples; }
	public Set<DataFeature> getFeatures() { return this.features; }
	public JobStatus getStatus() { return this.status; }

	public int getId() {
		return this.id;
	}
}
