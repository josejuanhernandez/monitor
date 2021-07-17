package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.datahub.Event;
import com.cinepolis.cosmos.monitor.goals.CrawlerGoal;
import com.cinepolis.cosmos.monitor.goals.ScrapperGoal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

import static com.cinepolis.cosmos.Archetype.GoalTank;

public interface Goal extends Iterable<Goal.Task> {

	String name();
	long delay();
	long period();

	default void start() {
		Logger.info(name() + " goal started");
		write(event("start"));
	}

	default void terminate() {
		write(event("terminate"));
		Logger.info(name() + " goal terminated");
	}


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
//					new CrawlerGoal("configuration", 0, Day),
//					new CrawlerGoal("status", 15 * Minute, 15 * Minute),
					new ScrapperGoal("exhibition", 5*Minute, Minute)
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

	private static String today() {
		LocalDate now = LocalDate.now();
		return String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
	}

	private void write(byte[] bytes) {
		try {
			Path path = new File(GoalTank, today() + ".inl").toPath();
			Files.write(path, bytes, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		} catch (IOException ignored) {
		}
	}

	private byte[] event(String action) {
		return String.format("[goal]\nts: %s\nname: %s\naction: %s\n\n", Instant.now(), name(), action).getBytes();
	}

}
