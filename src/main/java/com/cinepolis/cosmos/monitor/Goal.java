package com.cinepolis.cosmos.monitor;

import java.util.Iterator;

public abstract class Goal implements Iterable<Feeder> {
	public final String name;
	public final long delay;
	public final long period;

	public Goal(String name, long delay, long period) {
		this.name = name;
		this.delay = delay;
		this.period = period;
	}

	@Override
	public abstract Iterator<Feeder> iterator();


	@Override
	public String toString() {
		return name + " goal";
	}
}
