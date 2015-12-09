package mlaas;

public class Samples extends DataSample{

	private int sampleNumber;
	private int id;

	public Samples(int i) {
		this.sampleNumber = i;
		this.id = i;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getFilename() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getId() {
		// TODO Auto-generated method stub
		return this.id;
	}

	@Override
	public DataSet getDataSet() {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer getSampleNumber() {
		// TODO Auto-generated method stub
		return sampleNumber;
	}

}
