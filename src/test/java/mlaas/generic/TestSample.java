package mlaas.generic;

import mlaas.*;

/**
 * Generic DataSample class for testing.
 */
public class TestSample extends DataSample {

	private Integer id;

	public TestSample(int id) {
		this.id = id;
	}

	public String getFilename() {
		return this.id.toString();
	}

	public int getId() {
		return this.id;
	}

	public DataSet getDataSet() {
		return DataSet.None;
	}
}
