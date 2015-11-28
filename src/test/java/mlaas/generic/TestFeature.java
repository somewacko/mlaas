package mlaas.generic;

import mlaas.*;

/**
 * Generic DataFeature class for testing.
 */
public class TestFeature extends DataFeature {

	private Integer id;

	public TestFeature(int id) {
		this.id = id;
	}

	public String getName() {
		return this.id.toString();
	}

	public int getId() {
		return this.id;
	}

	public DataSet getDataSet() {
		return DataSet.None;
	}
}
