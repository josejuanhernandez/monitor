package com.cinepolis.cosmos.monitor.datahub;

import com.cinepolis.cosmos.monitor.datahub.publishers.DatalakePublisher;
import com.cinepolis.cosmos.monitor.datahub.publishers.DatamartPublisher;

import java.util.ArrayList;
import java.util.List;

public class Session {
	private final List<Publisher> publishers;

	public Session(String type) {
		this.publishers = publishersOf(type);
		this.publishers.forEach(Publisher::start);
	}

	public Session put(Event event) {
		publishers.forEach(s -> s.publish(event));
		return this;
	}

	public void terminate() {
		this.publishers.forEach(Publisher::terminate);
	}

	private static List<Publisher> publishersOf(String type) {
		List<Publisher> publishers = new ArrayList<>();
		publishers.add(new DatamartPublisher(type));
		publishers.add(new DatalakePublisher(type));
		return publishers;
	}
}
