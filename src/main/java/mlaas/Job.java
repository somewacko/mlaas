package mlaas;

import java.util.List;

/**
 * A job requested by a user to train a model on a given data set and some subset of available samples and features.
 *
 * Will be grouped together with other jobs later on to share training tasks to make training more efficient.
 */
public class Job {

	private DataSet dataSet;
	private List<DataSample> samples;
	private List<DataFeature> features;
	private JobStatus status = JobStatus.Waiting;

	/**
	 * Constructor for a Job with given specifications.
	 * @param dataSet The data set for this job to train on.
	 * @param samples Which samples this job should train on.
	 * @param features Which features this job should train on.
	 */
	public Job(DataSet dataSet, List<DataSample> samples, List<DataFeature> features) {
		this.dataSet = dataSet;
		this.samples = samples;
		this.features = features;
	}

	public DataSet getDataSet() { return this.dataSet; }
	public List<DataSample> getSamples() { return this.samples; }
	public List<DataFeature> getFeatures() { return this.features; }
	public JobStatus getStatus() { return this.status; }
}
