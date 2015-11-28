package mlaas;

import mlaas.bpti.*;
import mlaas.generic.*;

import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests for task plans.
 */
public class TestTaskPlan {

	private void printTask(Task task, int level) {

		for (int i = 0; i < level; i++)
			System.out.printf("\t");
		System.out.printf("Task: [");
		for (DataUnit unit : task.getWork())
			System.out.printf("%d,", unit.getId());
		System.out.printf("]");

		if (task.getEndJob() != null) {
			System.out.printf(" -> Job: [");
			for (DataUnit unit : task.extractWork(task.getEndJob()))
				System.out.printf("%d,", unit.getId());
			System.out.printf("]");
		}
		System.out.printf("\n");

		for (Task t : task.getNextTasks())
			printTask(t, level+1);
	}

	private void printTaskPlan(TaskPlan taskPlan) {

		System.out.println("Task plan:");
		for (Task task : taskPlan.getStartingTasks())
			printTask(task, 1);
		System.out.printf("\n");
	}


	private boolean tallyWorkInTask(Task task, Map<DataUnit, Boolean> presentWork) {

		for (DataUnit unit : task.getWork()) {
			if (presentWork.containsKey(unit)) {
				if (!presentWork.get(unit))
					presentWork.put(unit, true);
				else
					return false; // Work is accounted for twice
			}
			else
				return false; // Work is being done unrelated to current job
		}
		return true;
	}


	private boolean verifyTaskPlan(TaskPlan taskPlan, JobGroup jobGroup) {

		if (taskPlan.getTerminatingTasks().size() != jobGroup.getJobs().size())
			return false;

		// Check task plan by starting from terminating task for a job and bubbling up and checking that:
		//
		// 	1. There isn't any work that appears that the current job doesn't have.
		//  2. All work associated with the current job appears exactly once.

		for (Job job : jobGroup.getJobs()) {

			Task task = taskPlan.getTerminatingTasks().get(job);

			final Set<DataUnit> work = task.extractWork(job);

			Map<DataUnit, Boolean> presentWork = new HashMap<DataUnit, Boolean>(){{
				for (DataUnit unit : work)
					put(unit, false);
			}};

			if (task != null) {

				if (!this.tallyWorkInTask(task, presentWork))
					return false;

				while (task.getLastTask() != null) {
					task = task.getLastTask();

					if (!this.tallyWorkInTask(task, presentWork))
						return false;
				}

				for (Boolean isPresent : presentWork.values()) {
					if (!isPresent)
						return false;
				}
			}
			else
				return false;
		}
		return true;
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

	private void printStats(TaskPlan taskPlan, JobGroup jobGroup) {

		int taskCount = 0;
		for (Task task : taskPlan.getStartingTasks())
			taskCount += this.countTasks(task);
		System.out.printf("Num. tasks: %d\n", taskCount);

		int workCount = 0;
		for (Task task : taskPlan.getStartingTasks())
			workCount += this.countWork(task);
		System.out.printf("Num. work: %d\n", workCount);

		int baseWorkCount = 0;
		for (Job job : jobGroup.getJobs()) {
			if (jobGroup.getType() == JobGroupType.SharedFeatures)
				baseWorkCount += job.getFeatures().size();
			else
				baseWorkCount += job.getSamples().size();
		}
		System.out.printf("Base work: %d\n", baseWorkCount);

		System.out.printf("Doing %5.2f%% of work\n", 100*(workCount/(float)baseWorkCount));
	}

	@Test
	public void testDuplicateJobs() {

		final List<DataFeature> commonFeatures = new ArrayList<DataFeature>(){{
			add(new TestFeature(0));
		}};

		final List<DataSample> samples = new ArrayList<DataSample>(){{
			add(new TestSample(1));
			add(new TestSample(2));
			add(new TestSample(3));
		}};

		List<Job> jobs = new ArrayList<Job>(){{
			add(new Job(DataSet.None, samples, commonFeatures));
			add(new Job(DataSet.None, samples, commonFeatures));
			add(new Job(DataSet.None, samples, commonFeatures));
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		//printTaskPlan(taskPlan);
		//printStats(taskPlan, jobGroup);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}

	@Test
	public void testUnrelatedJobs() {

		final List<DataFeature> commonFeatures = new ArrayList<DataFeature>(){{
			add(new TestFeature(0));
		}};

		final List<DataSample> samples1 = new ArrayList<DataSample>(){{
			add(new TestSample(1));
			add(new TestSample(2));
			add(new TestSample(3));
		}};
		final List<DataSample> samples2 = new ArrayList<DataSample>(){{
			add(new TestSample(4));
			add(new TestSample(5));
			add(new TestSample(6));
		}};
		final List<DataSample> samples3 = new ArrayList<DataSample>(){{
			add(new TestSample(7));
			add(new TestSample(8));
			add(new TestSample(9));
		}};

		List<Job> jobs = new ArrayList<Job>(){{
			add(new Job(DataSet.None, samples1, commonFeatures));
			add(new Job(DataSet.None, samples2, commonFeatures));
			add(new Job(DataSet.None, samples3, commonFeatures));
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		//printTaskPlan(taskPlan);
		//printStats(taskPlan, jobGroup);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}

	@Test
	public void testSimpleJobs() {

		final List<DataFeature> commonFeatures = new ArrayList<DataFeature>(){{
			add(new TestFeature(0));
		}};

		final List<DataSample> samples1 = new ArrayList<DataSample>(){{
			add(new TestSample(1));
			add(new TestSample(2));
			add(new TestSample(3));
		}};
		final List<DataSample> samples2 = new ArrayList<DataSample>(){{
			add(new TestSample(2));
			add(new TestSample(3));
			add(new TestSample(4));
		}};
		final List<DataSample> samples3 = new ArrayList<DataSample>(){{
			add(new TestSample(3));
			add(new TestSample(4));
			add(new TestSample(5));
		}};

		List<Job> jobs = new ArrayList<Job>(){{
			add(new Job(DataSet.None, samples1, commonFeatures));
			add(new Job(DataSet.None, samples2, commonFeatures));
			add(new Job(DataSet.None, samples3, commonFeatures));
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		//printTaskPlan(taskPlan);
		//printStats(taskPlan, jobGroup);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}

	@Test
	public void testComplexJobs() {

		final List<DataFeature> commonFeatures = new ArrayList<DataFeature>(){{
			add(new TestFeature(0));
		}};

		final List<DataSample> samples1 = new ArrayList<DataSample>(){{
			add(new TestSample(1));
			add(new TestSample(2));
		}};
		final List<DataSample> samples2 = new ArrayList<DataSample>(){{
			add(new TestSample(2));
			add(new TestSample(3));
		}};
		final List<DataSample> samples3 = new ArrayList<DataSample>(){{
			add(new TestSample(3));
		}};
		final List<DataSample> samples4 = new ArrayList<DataSample>(){{
			add(new TestSample(3));
			add(new TestSample(4));
		}};
		final List<DataSample> samples5 = new ArrayList<DataSample>(){{
			add(new TestSample(4));
			add(new TestSample(5));
		}};

		List<Job> jobs = new ArrayList<Job>(){{
			add(new Job(DataSet.None, samples1, commonFeatures));
			add(new Job(DataSet.None, samples2, commonFeatures));
			add(new Job(DataSet.None, samples3, commonFeatures));
			add(new Job(DataSet.None, samples4, commonFeatures));
			add(new Job(DataSet.None, samples5, commonFeatures));
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		//printTaskPlan(taskPlan);
		//printStats(taskPlan, jobGroup);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}

	@Test
	public void testMoreComplexCase() {

		final List<DataFeature> commonFeatures = new ArrayList<DataFeature>(){{
			add(new TestFeature(0));
		}};

		final List<DataSample> samples1 = new ArrayList<DataSample>(){{
			add(new TestSample(1));
			add(new TestSample(2));
		}};
		final List<DataSample> samples2 = new ArrayList<DataSample>(){{
			add(new TestSample(4));
			add(new TestSample(5));
		}};
		final List<DataSample> samples3 = new ArrayList<DataSample>(){{
			add(new TestSample(7));
			add(new TestSample(8));
		}};
		final List<DataSample> samples4 = new ArrayList<DataSample>(){{
			add(new TestSample(2));
			add(new TestSample(7));
		}};
		final List<DataSample> samples5 = new ArrayList<DataSample>(){{
			add(new TestSample(4));
			add(new TestSample(2));
		}};
		final List<DataSample> samples6 = new ArrayList<DataSample>(){{
			add(new TestSample(2));
			add(new TestSample(7));
			add(new TestSample(8));
			add(new TestSample(10));
		}};
		final List<DataSample> samples7 = new ArrayList<DataSample>(){{
			add(new TestSample(1));
			add(new TestSample(2));
			add(new TestSample(3));
		}};
		final List<DataSample> samples8 = new ArrayList<DataSample>(){{
			add(new TestSample(1));
			add(new TestSample(4));
			add(new TestSample(8));
			add(new TestSample(9));
		}};

		List<Job> jobs = new ArrayList<Job>(){{
			add(new Job(DataSet.None, samples1, commonFeatures));
			add(new Job(DataSet.None, samples2, commonFeatures));
			add(new Job(DataSet.None, samples3, commonFeatures));
			add(new Job(DataSet.None, samples4, commonFeatures));
			add(new Job(DataSet.None, samples5, commonFeatures));
			add(new Job(DataSet.None, samples6, commonFeatures));
			add(new Job(DataSet.None, samples7, commonFeatures));
			add(new Job(DataSet.None, samples8, commonFeatures));
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		//printTaskPlan(taskPlan);
		//printStats(taskPlan, jobGroup);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}

	@Test
	public void testLargeGroup() {

		final int numJobs = 10;
		final int numWork = 100;

		final Random rand = new Random();

		final List<DataFeature> commonFeatures = new ArrayList<DataFeature>(){{
			add(new TestFeature(0));
		}};

		List<Job> jobs = new ArrayList<Job>(){{
			for (int i = 0; i < numJobs; i++) {
				Set<DataSample> samples = new HashSet<DataSample>(){{
					for (int j = 0; j < numWork; j++) {
						add(new TestSample(rand.nextInt(numWork/2)));
					}
				}};
				add(new Job(DataSet.None, samples, commonFeatures));
			}
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		// printStats(taskPlan, jobGroup);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}
}
