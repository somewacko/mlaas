package mlaas.stats;

import mlaas.*;
import mlaas.generic.*;

import java.util.*;

/**
 * Class to compute stats of Task Plan algorithm performance.
 */
public class TaskPlanStats {

	public static class Stats {
		int numTasks; 			// How many tasks are in the task plan
		int numWork; 			// How much work gets executed in the task plan
		int numBaseWork; 		// How much work would be executed if each job was run separately
		double percentage; 		// What percentage of work is being done with the task plan compared to without
		double avgSharedWork;   // The average number of shared work per job
		double meanCommon;      // The mean value of number of jobs a work applies to
		double avgWork;         // The average number of work units per job

		public Stats(TaskPlan taskPlan, JobGroup jobGroup) {

			this.numTasks = 0;
			this.numWork = 0;
			for (Task task : taskPlan.getStartingTasks()) {
				this.numTasks += this.countTasks(task);
				this.numWork += this.countWork(task);
			}
			this.numBaseWork = this.countBaseWork(jobGroup);

			this.percentage = 100.0 * (this.numWork/(double)this.numBaseWork);

			this.avgSharedWork = this.computeAvgSharedWork(jobGroup);
			this.meanCommon = this.computeMeanCommon(jobGroup);

			this.avgWork = this.computeAvgWork(jobGroup);
		}

		private int countTasks(Task task) {
			int count = 1;
			for (Task nextTask : task.getNextTasks())
				count += this.countTasks(nextTask);
			return count;
		}

		private int countWork(Task task) {
			int count = task.getWork().size();
			for (Task nextTask : task.getNextTasks())
				count += this.countWork(nextTask);
			return count;
		}

		private int countBaseWork(JobGroup jobGroup) {
			int count = 0;
			for (Job job : jobGroup.getJobs()) {
				if (jobGroup.getType() == JobGroupType.SharedFeatures)
					count += job.getFeatures().size();
				else
					count += job.getSamples().size();
			}
			return count;
		}

		private double computeAvgSharedWork(JobGroup jobGroup) {

			double avgSharedWork = 0;

			for (Job job : jobGroup.getJobs()) {

				Set<DataUnit> work = jobGroup.extractWork(job);

				double count = 0;
				for (DataUnit unit : work) {
					boolean hasWork = false;
					for (Job otherJob : jobGroup.getJobs()) {
						if (otherJob != job && jobGroup.extractWork(otherJob).contains(unit)) {
							hasWork = true;
							break;
						}
					}
					if (hasWork)
						count++;
				}
				avgSharedWork += 100.0 * count / work.size();
			}
			return avgSharedWork / jobGroup.getJobs().size();
		}

		private double computeMeanCommon(JobGroup jobGroup) {

			Set<DataUnit> allWork = jobGroup.allWork();

			double mean = 0;

			for (DataUnit unit : allWork) {

				double count = 0;

				for (Job job : jobGroup.getJobs()) {
					if (jobGroup.extractWork(job).contains(unit))
						count++;
				}
				mean += count;
			}

			return mean / allWork.size();
		}

		private double computeAvgWork(JobGroup jobGroup) {
			double mean = 0;
			for (Job job : jobGroup.getJobs())
				mean += jobGroup.extractWork(job).size();
			return mean / jobGroup.getJobs().size();
		}
	}


	private static Stats measure(final int numJobs, final int numWork, final double variance) {

		final Random rand = new Random();

		final List<DataFeature> commonFeatures = new ArrayList<DataFeature>(){{
			add(new TestFeature(0));
		}};

		List<Job> jobs = new ArrayList<Job>(){{
			for (int i = 0; i < numJobs; i++) {
				Set<DataSample> samples = new HashSet<DataSample>(){{
					for (int j = 0; j < numWork; j++) {
						double gaussian = variance * rand.nextGaussian();
						add(new TestSample( (int)Math.round(gaussian) ));
					}
				}};
				add(new Job(DataSet.None, samples, commonFeatures));
			}
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		return new Stats(taskPlan, jobGroup);
	}


	public static void main(String[] args) {

		int numJobs = 100;
		int numWork = 1000;

		List<Double> variances = Arrays.asList(0.1, 1., 5., 10., 15., 20., 25., 35., 50., 75., 100.);

		for (double variance : variances) {

			long startTime = System.currentTimeMillis();
			Stats stats = TaskPlanStats.measure(numJobs, numWork, variance);
			long totalTime = System.currentTimeMillis()-startTime;

			System.out.printf("#Jobs: %3d, #Work: %4d, Var: %6.1f, Avg. Work: %6.1f\n" +
							"\tWork needed:      %6.2f%%\n" +
							"\tAvg. Shared work: %6.2f%%\n" +
							"\tMean Common:      %6.2f/%d\n" +
							"\tTime:             %3ds,%3dms\n",
					numJobs, numWork, variance, stats.avgWork, stats.percentage,
					stats.avgSharedWork, stats.meanCommon, numJobs, totalTime/1000, totalTime%1000);
		}
	}
}
