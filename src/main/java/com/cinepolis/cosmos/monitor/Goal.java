package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.goals.ScrapperGoal;

import java.util.Iterator;
import java.util.List;

public interface Goal extends Iterable<Goal.Task> {

	String name();
	long delay();
	long period();

	void start();
	void terminate();

	interface Task {
		Event execute();
	}

	class Factory implements Iterable<Goal> {
		private static final int Second = 1;
		private static final int Minute = 60 * Second;
		private static final int Hour = 60 * Minute;
		private static final int Day = 24 * Hour;

		private final List<Goal> goals;

		public Factory() {
			this.goals = List.of(
	//				new DeviceGoal("configuration", Minute, Day),
	//				new DeviceGoal("status", 15 * Minute, 15 * Minute),
					new ScrapperGoal("show", 0, Minute)
			);
		}

		@Override
		public Iterator<Goal> iterator() {
			return goals.iterator();
		}

		public int size() {
			return goals.size();
		}
	}
}
