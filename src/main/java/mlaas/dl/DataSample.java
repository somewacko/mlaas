package mlaas.dl;

/**
 * Interface for implementing samples in different data sets.
 */
public interface DataSample {

	/**
	 * The filename that corresponds to this sample of data. Note that this currently doesn't return any information
	 * about where the data is stored.
	 * @return The sample's filename.
	 */
	String getFilename();

	/**
	 * A unique ID for this sample. Used for comparisons and task planning.
	 * @return The unique ID for this sample.
	 */
	int getId();
}
