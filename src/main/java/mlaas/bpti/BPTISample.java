package mlaas.bpti;

import mlaas.DataSample;
import mlaas.DataSet;

/**
 * Samples available in the BPTI data set.
 */
public class BPTISample implements DataSample {

	int index;
	private String filename;

	/**
	 * Constructor for a sample with a given index.
	 * @param index the index for this sample.
	 * @throws RuntimeException if index is out of bounds.
	 */
	BPTISample(int index) {

		if (index < 0 || 4000 < index) {
			throw new RuntimeException("Index out of bounds");
		}
		else {
			this.index = index;
			this.filename = String.format("features-%1$d.txt", this.index);
		}
	}

	@Override
	public String getFilename() {
		return this.filename;
	}

	@Override
	public int getId() { return this.index; }

	@Override
	public DataSet getDataSet() { return DataSet.BPTI; }
}
