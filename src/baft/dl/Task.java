package mlaas.dl;
import java.util.*;

public class Task {
	ArrayList<Task> lastTasks = new ArrayList<Task>();
	ArrayList<Task> nextTasks = new ArrayList<Task>();
	ArrayList<Data> data = new ArrayList<Data>();
	ArrayList<Features> features = new ArrayList<Features>();
	Job endJob= new Job();
	Boolean isFinished = false;
	
	public Results createResults()
	{
		return null;
	}
}
