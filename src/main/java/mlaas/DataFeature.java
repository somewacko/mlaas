package mlaas;

/**
 * Interface for implementing features in different data sets.
 */
public abstract class DataFeature extends DataUnit {

	/**
	 * The name or key corresponding to this feature.
	 * @return The feature's name.
	 */
	abstract public String getName();
}
