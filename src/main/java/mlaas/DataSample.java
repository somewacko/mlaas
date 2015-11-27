package mlaas;

/**
 * Interface for implementing samples in different data sets.
 */
public abstract class DataSample extends DataUnit {

	/**
	 * The filename that corresponds to this sample of data. Note that this currently doesn't return any information
	 * about where the data is stored.
	 * @return The sample's filename.
	 */
	abstract public String getFilename();
}
