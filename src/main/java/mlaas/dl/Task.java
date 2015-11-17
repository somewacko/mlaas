package mlaas.dl;
import java.util.*;

public class Task {
	ArrayList<Task> lastTasks = new ArrayList<Task>();
	ArrayList<Task> nextTasks = new ArrayList<Task>();
	ArrayList<DataSample> data = new ArrayList<DataSample>();
	ArrayList<DataFeature> features = new ArrayList<DataFeature>();
	Job endJob= new Job();
	Boolean isFinished = false;
	
	public Results createResults()
	{
		return null;
	}
}
