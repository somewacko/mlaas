package mlaas;

public class Feature extends DataFeature{

	private int feature;
	private int id;

	public Feature(int feature){
		this.feature = feature;
		this.id = feature;
	}
	public int getFeature()
	{
		return this.feature;
	}
	
	
	@Override
	public String getName() {
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

}
