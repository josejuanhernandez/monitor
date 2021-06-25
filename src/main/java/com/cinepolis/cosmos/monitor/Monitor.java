package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.goals.crawler.Logger;
import com.cinepolis.cosmos.monitor.brokers.DatalakeBroker;
import com.cinepolis.cosmos.monitor.brokers.DatamartBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Monitor {
	private final GoalFactory goalFactory;
	private final ArrayBlockingQueue<Goal> processing;
	private final Timer timer;

	public Monitor(GoalFactory goalFactory) {
		this.goalFactory = goalFactory;
		this.processing = new ArrayBlockingQueue<>(goalFactory.size());
		this.timer = new Timer();
	}

	public void run() {
		for (Goal goal : goalFactory)
			timer.schedule(timerTaskOf(goal), goal.delay*1000, goal.period*1000);
	}

	private TimerTask timerTaskOf(Goal goal) {
		return new MonitorTimerTask(goal)
				.add(new DatamartBroker(goal.name))
				.add(new DatalakeBroker(goal.name));
	}

	public class MonitorTimerTask extends TimerTask {
		private final Goal goal;
		private final List<Broker> brokers;

		public MonitorTimerTask(Goal goal) {
			this.goal = goal;
			this.brokers = new ArrayList<>();
		}

		public void run() {
			if (brokers.size() == 0 || processing.contains(goal)) return;
			processing.add(goal);
			brokers.forEach(Broker::start);
			Logger.info("Dump for " + goal + " started");
			dump(brokers);
			brokers.forEach(Broker::terminate);
			Logger.info("Dump for " + goal + " terminated");
			processing.remove(goal);
		}

		private static final int ThreadPoolSize = 1024;
		private void dump(List<Broker> brokers) {
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ThreadPoolSize);
			Logger.info("Thread pool created");
			for (Feeder feeder : goal) {
				executor.submit(() -> dump(feeder, brokers));
			}
			Logger.info("Threads submitted");
			awaitFor(executor);
			Logger.info("Thread pool destroyed");
		}

		private void dump(Feeder feeder, List<Broker> brokers) {
			brokers.forEach(c->c.process(feeder.init()));
		}

		public MonitorTimerTask add(Broker broker) {
			brokers.add(broker);
			return this;
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
