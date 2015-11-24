package mlaas.dl;

/**
 * Interface for implementing features in different data sets.
 */
public interface DataFeature {

	/**
	 * The name corresponding to this feature.
	 * @return The feature's name.
	 */
	String getName();

	/**
	 * A unique ID for this feature. Used for comparisons and task planning.
	 * @return The unique ID for this feature.
	 */
	int getId();

	/**
	 * The data set which this feature is derived from.
	 * @return The associated data set.
	 */
	DataSet getDataSet();
}
