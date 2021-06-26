package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.broker.Broker;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Monitor {
	private final Goal.Factory goalFactory;
	private final ArrayBlockingQueue<Goal> processing;
	private final Timer timer;

	public Monitor(Goal.Factory goalFactory) {
		this.goalFactory = goalFactory;
		this.processing = new ArrayBlockingQueue<>(goalFactory.size());
		this.timer = new Timer();
	}

	public void run() {
		for (Goal goal : goalFactory)
			timer.schedule(timerTaskOf(goal), goal.delay()*1000, goal.period()*1000);
	}

	private TimerTask timerTaskOf(Goal goal) {
		return new MonitorTimerTask(goal);
	}

	private class MonitorTimerTask extends TimerTask {
		private final Goal goal;
		private final Broker.Session session;

		public MonitorTimerTask(Goal goal) {
			this.goal = goal;
			this.session = Broker.instance.start(goal.name());
		}

		public void run() {
			if (processing.contains(goal)) return;
			processing.add(goal);
			goal.start();
			Logger.info("Dump for " + goal + " started");
			execute();
			goal.terminate();
			session.terminate();
			processing.remove(goal);
			Logger.info("Dump for " + goal + " terminated");
		}

		private static final int ThreadPoolSize = 1024;
		private void execute() {
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ThreadPoolSize);
			Logger.info("Thread pool created");
			for (Goal.Task task : goal) executor.submit(() -> execute(task));
			Logger.info("Threads submitted");
			awaitFor(executor);
			Logger.info("Thread pool destroyed");
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

}
