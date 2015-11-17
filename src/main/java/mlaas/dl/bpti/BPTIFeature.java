package mlaas.dl.bpti;

/**
 * Features available in the BPTI data set.
 */
public class BPTIFeature implements mlaas.dl.DataFeature {

	private BPTIFeatureType type;

	/**
	 * Constructor for a feature with a given type.
	 * @param type the type of this feature.
	 */
	public BPTIFeature(BPTIFeatureType type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return type.toString();
	}

	@Override
	public int getId() {
		return this.type.ordinal();
	}
}
