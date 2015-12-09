package mlaas;

/**
 * Wrapper class for a DNN model from DL4J.
 */
public class DNNModel {
	private String modelPath;
    private String weightPath;
    DNNModel(){
        modelPath="";
        weightPath="";
    }
    DNNModel(String p1,String p2){
        modelPath= p1;
        weightPath = p2;
    }
    public void setPaths(String p1,String p2){
        modelPath= p1;
        weightPath = p2;
    }
    public String getModelPath(){
        return modelPath;
    }
    public boolean isEmpty(){
        return modelPath.equals("");
    }
    public String getWeightPath(){
        return weightPath;
    }
}
