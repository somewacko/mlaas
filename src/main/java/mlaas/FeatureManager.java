package mlaas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class responsible for compiling samples and features into a single file for Spark/DL4J to use.
 */

public class FeatureManager {

	public static String formTrainingData(Task task) {
		// Only done for samples for now for testing
		// Empty for now...

		String outputFileName = "inputFile"+task.getId()+".txt";
		String dir = "/local/BigData/Data";

		ArrayList<DataSample> sampleList = new ArrayList<DataSample>();
		ArrayList<DataFeature> featureList = new ArrayList<DataFeature>();
		ArrayList<Integer> samplesFromFile = new ArrayList<Integer>();

		sampleList.addAll(task.getSamples());
		featureList.addAll(task.getFeatures());

		ArrayList<String> toWrite = new ArrayList<String>();
		for (int i =0; i<sampleList.size();i++)
		{
			samplesFromFile.add(((Samples) sampleList.get(i)).getSampleNumber());
		}
		String line = null;
		String fileName = "window_features4000.txt";
		try {
			FileReader fileReader =
					new FileReader(fileName);
			BufferedReader bufferedReader =
					new BufferedReader(fileReader);
			int counter = 0;
			while((line = bufferedReader.readLine()) != null) {
				counter++;
				if(samplesFromFile.contains(counter)){
					toWrite.add(line);
				}
			}
			bufferedReader.close();
			File file = new File(dir,outputFileName);
			FileWriter fileWriter =
					new FileWriter(file);
			BufferedWriter bufferedWriter =
					new BufferedWriter(fileWriter);
			for (int i = 0 ; i<toWrite.size() ; i++)
			{
				bufferedWriter.write(toWrite.get(i));
				bufferedWriter.newLine();
			}
			// Always close files.
			bufferedWriter.close();
		}
		catch(FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		return (dir+"/"+outputFileName);
	}
	public static String formTestingData(Job job) {
		// Empty for now...
		String fileName = "window_features4000.txt";

		String outputFileName = "testFile"+job.getId()+".txt";
		String dir = "/local/BigData/Data";
		ArrayList<DataSample> sampleList = new ArrayList<DataSample>();
		ArrayList<DataFeature> featureList = new ArrayList<DataFeature>();
		ArrayList<Integer> samplesFromFile = new ArrayList<Integer>();

		sampleList.addAll(job.getSamples());
		featureList.addAll(job.getFeatures());

		ArrayList<String> toWrite = new ArrayList<String>();
		for (int i =0; i<sampleList.size();i++)
		{
			samplesFromFile.add(((Samples) sampleList.get(i)).getSampleNumber());
		}
		double precent = (samplesFromFile.size()*.25);
		int intPercent = (int)precent;
		try {
			// FileReader reads text files in the default encoding.
			FileReader fileReader =
					new FileReader(fileName);

			// Always wrap FileReader in BufferedReader.
			BufferedReader bufferedReader =
					new BufferedReader(fileReader);
			for(int i = 0; i < 60800; ++i)
				bufferedReader.readLine();
			for(int i = 60800; i<=60800+intPercent;i++)
				toWrite.add(bufferedReader.readLine());
			bufferedReader.close();
			File file = new File(dir, outputFileName);
			FileWriter fileWriter =
					new FileWriter(file);

			// Always wrap FileWriter in BufferedWriter.
			BufferedWriter bufferedWriter =
					new BufferedWriter(fileWriter);

			// Note that write() does not automatically
			// append a newline character.
			for (int i = 0 ; i<toWrite.size() ; i++)
			{
				bufferedWriter.write(toWrite.get(i));
	            bufferedWriter.newLine();
			}
			// Always close files.
			bufferedWriter.close();
		}
		catch(FileNotFoundException ex) {
			ex.printStackTrace();
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		return (dir+"/"+outputFileName);
	}
}
