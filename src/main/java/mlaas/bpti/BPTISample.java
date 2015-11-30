package mlaas.bpti;

import mlaas.DataSample;
import mlaas.DataSet;

/**
 * Samples available in the BPTI data set.
 */
public class BPTISample extends DataSample {

	int index;
	private String filename;

	/**
	 * Constructor for a sample with a given index.
	 * @param index the index for this sample.
	 * @throws RuntimeException if index is out of bounds.
	 */
	public BPTISample(int index) throws RuntimeException {

		if (index < 0 || 4000 < index) {
			throw new RuntimeException("Index out of bounds");
		}
		else {
			this.index = index;
			this.filename = String.format("features-%1$d.txt", this.index);
		}
	}

	public String getFilename() {
		return this.filename;
	}

	public int getId() {
		return this.index;
	}

	public DataSet getDataSet() {
		return DataSet.BPTI;
	}
}
