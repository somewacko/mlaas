package mlaas;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class JobQueue {
	ArrayList<Job> jobs = new ArrayList<Job>();

	public void addJob(Job job){
		this.jobs.add(job);
	}

	public ArrayList<Job> getJobList(){
		return this.jobs;
	}
	public void removeJobs(ArrayList<Job> jobsToBeRemoved){
		this.jobs.removeAll(jobsToBeRemoved);
	}


	public ArrayList<Job> findSimilarJobs(Job job){
		return null;
	}

	public JobGroup groupJobs(ArrayList<Job> jobs, JobGroupType type){
		JobGroup jobGroup = new JobGroup(jobs, type);
		return jobGroup;
	}
}
