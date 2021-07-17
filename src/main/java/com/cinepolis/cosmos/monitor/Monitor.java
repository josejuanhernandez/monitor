package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.datahub.Broker;
import com.cinepolis.cosmos.monitor.datahub.Session;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Monitor {
	private final Iterable<Goal> goals;
	private final BlockingQueue<Goal> blockingQueue;
	private final Timer timer;

	public Monitor(Iterable<Goal> goals) {
		this.goals = goals;
		this.blockingQueue = new ArrayBlockingQueue<>(count(goals)+1);
		this.timer = new Timer();
	}

	public void run() {
		for (Goal goal : goals)
			timer.schedule(timerTaskOf(goal), goal.delay()*1000, goal.period()*1000);
	}

	private TimerTask timerTaskOf(Goal goal) {
		return new InternalTimerTask(goal);
	}

	private class InternalTimerTask extends TimerTask {
		private final Goal goal;
		private Session session;

		public InternalTimerTask(Goal goal) {
			this.goal = goal;
		}

		public void run() {
			if (blockingQueue.contains(goal)) return;
			try {
				blockingQueue.add(goal);
				execute();
			}
			finally {
				blockingQueue.remove(goal);
			}
		}

		private static final int ThreadPoolSize = 1024;
		private void execute() {
			goal.start();
			session = Broker.instance.start(goal.name());
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ThreadPoolSize);
			for (Goal.Task task : goal) executor.submit(() -> execute(task));
			awaitFor(executor);
			session.terminate();
			goal.terminate();
		}

		private void execute(Goal.Task task) {
			session.put(task.execute());
		}


	}
	private void awaitFor(ThreadPoolExecutor executor) {
		try {
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Logger.error(e.getMessage());
		}
	}

	private int count(Iterable<Goal> goals) {
		int count = 0;
		for (Goal goal : goals) count++;
		return count;
	}

}
