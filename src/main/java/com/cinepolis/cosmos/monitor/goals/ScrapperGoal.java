package com.cinepolis.cosmos.monitor.goals;

import com.cinepolis.cosmos.monitor.Goal;
import com.cinepolis.cosmos.monitor.goals.scrapper.exhibition.Exhibition;
import com.cinepolis.cosmos.monitor.goals.scrapper.session.Session;

import java.time.*;
import java.util.Iterator;
import java.util.List;

import static com.cinepolis.cosmos.monitor.Inventory.*;

public class ScrapperGoal implements Goal {
	private final String name;
	private final int delay;
	private final int period;
	private List<Exhibition> exhibitions;
	private LocalDate expired;

	public ScrapperGoal(String name, int delay, int period) {
		this.name = name;
		this.delay = delay;
		this.period = period;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public long delay() {
		return delay;
	}

	@Override
	public long period() {
		return period;
	}

	@Override
	public void start() {

	}

	@Override
	public void terminate() {

	}

	@Override
	public Iterator<Task> iterator() {
		return new Iterator<>() {
			private final int now = now();
			private final Iterator<Exhibition> iterator = exhibitions().stream()
					.filter(e->e.isActive(now))
					.iterator();


			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
			@Override
			public Task next() {
				return () -> asSession(iterator.next());
			}

		};
	}

	private static Session asSession(Exhibition exhibition) {
		return Session.read(exhibition);
	}

	private static int now() {
		return LocalTime.now().toSecondOfDay() + offset(Mexico).getTotalSeconds() - offset(ZoneId.systemDefault()).getTotalSeconds();
	}

	private static ZoneOffset offset(ZoneId ZONE) {
		return ZONE.getRules().getOffset(Instant.now());
	}

	private List<Exhibition> exhibitions() {
		if (requireExhibitionsUpdate()) exhibitions = Exhibition.read(Divisions);
		expired = LocalDate.now();
		return exhibitions;
	}

	private boolean requireExhibitionsUpdate() {
		return expired == null || (LocalTime.now().getHour() == 15 && LocalDate.now().isBefore(expired));
	}

}
