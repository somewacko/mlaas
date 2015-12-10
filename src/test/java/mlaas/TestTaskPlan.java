package mlaas;

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

		printTaskPlan(taskPlan);

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

		printTaskPlan(taskPlan);

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

		printTaskPlan(taskPlan);

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

		printTaskPlan(taskPlan);

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

		printTaskPlan(taskPlan);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}

	@Test
	public void testMoreComplexCase2() {

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
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(1));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(1));
				add(new TestSample(2));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(1));
				add(new TestSample(2));
				add(new TestSample(3));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(1));
				add(new TestSample(2));
				add(new TestSample(3));
				add(new TestSample(4));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(1));
				add(new TestSample(2));
				add(new TestSample(3));
				add(new TestSample(4));
				add(new TestSample(5));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(2));
				add(new TestSample(3));
				add(new TestSample(4));
				add(new TestSample(5));
				add(new TestSample(6));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(2));
				add(new TestSample(4));
				add(new TestSample(6));
				add(new TestSample(8));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(3));
				add(new TestSample(5));
				add(new TestSample(7));
				add(new TestSample(9));
				add(new TestSample(10));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(2));
				add(new TestSample(5));
				add(new TestSample(3));
				add(new TestSample(9));
				add(new TestSample(8));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(4));
				add(new TestSample(8));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(5));
				add(new TestSample(4));
				add(new TestSample(3));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(8));
				add(new TestSample(9));
				add(new TestSample(7));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(1));
				add(new TestSample(5));
				add(new TestSample(9));
			}}, commonFeatures));
			add(new Job(DataSet.None, new ArrayList<DataSample>(){{
				add(new TestSample(4));
				add(new TestSample(3));
				add(new TestSample(2));
			}}, commonFeatures));
		}};

		JobGroup jobGroup = new JobGroup(jobs, JobGroupType.SharedSamples);
		TaskPlan taskPlan = new TaskPlan(jobGroup);

		printTaskPlan(taskPlan);

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}

	@Test
	public void testLargeGroup() {

		final int numJobs = 20;
		final int numWork = 1000;
		final double variance = 50;

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

		assertTrue( verifyTaskPlan(taskPlan, jobGroup) );
	}
}
