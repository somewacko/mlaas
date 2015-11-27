package mlaas;

/**
 * Interface for implementing features in different data sets.
 */
public interface DataFeature extends DataUnit {

	/**
	 * The name or key corresponding to this feature.
	 * @return The feature's name.
	 */
	String getName();
}
