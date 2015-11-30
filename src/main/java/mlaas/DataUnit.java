package mlaas;

/**
 * A singular unit describing some dimension of data (i.e. a feature or a sample).
 */
public abstract class DataUnit {

	/**
	 * A unique ID for this unit. Used for comparisons and task planning.
	 * @return The unique ID for this unit.
	 */
	abstract public int getId();


	/**
	 * The data set which this unit is derived from.
	 * @return The associated data set.
	 */
	abstract public DataSet getDataSet();


	@Override
	public int hashCode() {
		return this.getId();
	}


	@Override
	public boolean equals(Object obj) {

		if (obj instanceof DataUnit)
			return ((DataUnit) obj).getId() == this.getId();
		else
			return false;
	}
}
