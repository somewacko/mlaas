package mlaas;

/**
 * A singular unit describing some dimension of data (i.e. a feature or a sample).
 */
public interface DataUnit {

	/**
	 * A unique ID for this unit. Used for comparisons and task planning.
	 * @return The unique ID for this unit.
	 */
	int getId();

	/**
	 * The data set which this unit is derived from.
	 * @return The associated data set.
	 */
	DataSet getDataSet();
}
