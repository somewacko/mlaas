package mlaas;

import java.util.stream.IntStream;

import mlaas.DataFeature;
import mlaas.DataSample;
import mlaas.DataSet;
import mlaas.Feature;
import mlaas.Job;
import mlaas.JobGroup;
import mlaas.JobGroupType;
import mlaas.JobQueue;
import mlaas.Samples;
import java.io.IOException;
import java.util.HashMap;
import java.util.*;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import java.lang.IndexOutOfBoundsException;
import mlaas.LearningManager;

import static spark.Spark.*;

/**
 * Main class
 */

public class App {
  //Initialize global job queue
  JobQueue jobQueue = new JobQueue();
  Timer timer = new Timer ();

  TimerTask hourlyTask = new TimerTask () {
    @Override
    public void run () {
      System.out.println("ENTERING SCHEDULE");
      App app = new App();
      app.groupJobs();
        // your code here...
    }
};
  public static void main(String[] args) {
    App app = new App();
    //To add images etc.
    staticFileLocation("/public");
    //Layout for html
    String layout = "templates/layout.vtl";
    int cronTime = 1000*15*60;//1000*min*sec
    //Form Page
    get("/job_form", (request, response) -> {
      HashMap model = new HashMap();
      model.put("template", "templates/job_form.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    //Job Submitted/Submission Failed Page
    get("/submitted", (request, response) -> {
      String dataSet = request.queryParams("dataset");
      String samples = request.queryParams("samples");
      String features = request.queryParams("features");
      //Create and queue job
      boolean flag = app.createAndQueueJob(dataSet, samples, features);
      HashMap model = new HashMap();
      if (flag){
      model.put("template", "templates/job_submitted.vtl");
    }
    else {
      model.put("template", "templates/job_submission_failed.vtl");
    }
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());

    //Group Jobs Page -- will be removed later.
    get("/job_grouping", (request, response) -> {
      long startTime = System.currentTimeMillis();
      app.groupJobs();
      System.out.println(System.currentTimeMillis() - startTime);
      HashMap model = new HashMap();
      model.put("template", "templates/jobs_grouped.vtl");
      return new ModelAndView(model, layout);
    }, new VelocityTemplateEngine());
    app.timer.schedule (app.hourlyTask, 0l, cronTime);
  }


  public boolean createAndQueueJob(String dataSetInput, String sampleInput, String featureInput){
      System.out.println("Entering createJob");
      boolean flag = false;
      //Check if the inputs are not null or empty
      if ((dataSetInput == null) || sampleInput.equalsIgnoreCase("") || featureInput.equalsIgnoreCase("")){
        System.out.println("All inputs not provided");
        return flag;
      }
      else {
        System.out.println("Inputs Provided:");
        DataSet dataSet = DataSet.None;
    	  HashSet<DataSample> dataSample = new HashSet<DataSample>();
    	  HashSet<DataFeature> dataFeature = new HashSet<DataFeature>();
        if(dataSetInput.equalsIgnoreCase("BPTI")){
          dataSet = DataSet.BPTI;
        }
        String delims = "[,]";
			  String[] sampleInputList = sampleInput.split(delims);
			  DataSample sample;
        DataFeature feature;
        //Add samples to DataSample sample
        for (int i = 0;i < sampleInputList.length; i++)
				{
					if(sampleInputList[i].contains("-")){
						String startingIndex = sampleInputList[i].substring(0,sampleInputList[i].indexOf("-"));
						String endingIndex= sampleInputList[i].substring(sampleInputList[i].indexOf("-")+1);
						int start = Integer.parseInt(startingIndex);
						int end	= Integer.parseInt(endingIndex);
						int[] range = IntStream.rangeClosed(start, end).toArray();
						for (int j = 0;j < range.length; j++)
						{
							sample = new Samples(range[j]);
							dataSample.add(sample);
						}
					}
					else{
						sample = new Samples(Integer.parseInt(sampleInputList[i]));
						dataSample.add(sample);
					}
				}
        //Add features to DataFeature feature
        String[] featureInputList = featureInput.split(delims);
        for (int i = 0;i < featureInputList.length; i++)
				{
					if(featureInputList[i].contains("-")){
						String startingIndex = featureInputList[i].substring(0,featureInputList[i].indexOf("-"));
						String endingIndex= featureInputList[i].substring(featureInputList[i].indexOf("-")+1);
						int start = Integer.parseInt(startingIndex);
						int end	= Integer.parseInt(endingIndex);
						int[] range = IntStream.rangeClosed(start, end).toArray();
						for (int j = 0;j < range.length; j++)
						{
							feature = new Feature(range[j]);
							dataFeature.add(feature);
						}
					}
					else{
						feature = new Feature(Integer.parseInt(featureInputList[i]));
						dataFeature.add(feature);
					}
				}
        //Create the job
        Job job = new Job(dataSet, dataSample, dataFeature);
			  //Add job to queue
        jobQueue.addJob(job);
        System.out.println("Job Added to the queue.");
        System.out.println(jobQueue.getJobList());
        flag = true;
    }
    return flag;
  }

  public void groupJobs(){
    System.out.println("Entering groupJobs()");
    while(!jobQueue.getJobList().isEmpty()){
      String type = "";
      JobGroup jobGroup;
      JobGroupType jobGroupType = null;
      ArrayList<Job> jobsToBeGrouped = new ArrayList<Job>();
      Job currentJob;
      Job firstJob = jobQueue.getJobList().get(0);
      jobsToBeGrouped.add(firstJob);
      for(int i = 1 ; i < jobQueue.getJobList().size(); i++){
        currentJob = jobQueue.getJobList().get(i);
        if(currentJob.getSamples().equals(firstJob.getSamples()) && (type.equalsIgnoreCase("") || type.equalsIgnoreCase("SharedFeatures"))){
          if(type.equalsIgnoreCase(""))
          {
            type = "SharedFeatures";
          }
          jobsToBeGrouped.add(currentJob);
        }
        else if(currentJob.getFeatures().equals(firstJob.getFeatures()) && (type.equalsIgnoreCase("") || type.equalsIgnoreCase("SharedSamples"))){
          if(type.equalsIgnoreCase("")){
            type = "SharedSamples";
          }jobsToBeGrouped.add(currentJob);
        }
      }
      jobQueue.removeJobs(jobsToBeGrouped);
      //jobsToBeGrouped contains all the jobs that need to be grouped.
      if(type.equalsIgnoreCase("SharedSamples")){
        jobGroupType  = JobGroupType.SharedSamples;
      }
      else{
        jobGroupType  = JobGroupType.SharedFeatures;
      }
      jobGroup = jobQueue.groupJobs(jobsToBeGrouped, jobGroupType);
			System.out.println(jobGroup);
      System.out.println(jobGroup.getJobs());
      System.out.println(jobGroup.getType());
      LearningManager learningManager = new LearningManager(jobGroup);
      learningManager.run();

      //Send the job group to Learning Manager
      //learningManager(jobGroup) : This method will connect to learning manager.
    }
  }
}
