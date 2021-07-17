package com.cinepolis.cosmos.monitor.goals;

import com.cinepolis.cosmos.monitor.Goal;
import com.cinepolis.cosmos.monitor.goals.scrapper.Exhibition;
import com.cinepolis.cosmos.monitor.goals.scrapper.Website;

import java.util.Iterator;

public class ScrapperGoal implements Goal {
	private final String name;
	private final int delay;
	private final int period;
	private final Website website;

	public ScrapperGoal(String name, int delay, int period) {
		this.name = name;
		this.delay = delay;
		this.period = period;
		this.website = new Website();
		this.website.start();
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
	public Iterator<Task> iterator() {
		return new Iterator<>() {
			private final Iterator<Exhibition> iterator = website.activeExhibitions().iterator();

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public Task next() {
				Exhibition exhibition = iterator.next();
				return () -> website.update(exhibition);
			}
		};
	}

}
