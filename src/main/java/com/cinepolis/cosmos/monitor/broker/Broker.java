package com.cinepolis.cosmos.monitor.broker;

import com.cinepolis.cosmos.monitor.Event;
import com.cinepolis.cosmos.monitor.publishers.DatalakePublisher;
import com.cinepolis.cosmos.monitor.publishers.DatamartPublisher;
import com.cinepolis.cosmos.monitor.adapters.SwitchOnProjectorAdapter;

import java.util.ArrayList;
import java.util.List;

public class Broker {
	public static Broker instance = new Broker();
	public static final List<Adapter> Adapters = List.of(
			new SwitchOnProjectorAdapter()
	);

	private Broker() {
	}

	public Session start(String type) {
		return new Session(type);
	}

	public static class Session {
		private final List<Publisher> publishers;

		public Session(String type) {
			this.publishers = publishersOf(type);
			this.publishers.forEach(Publisher::start);
		}

		public Session put(Event event) {
			publishers.forEach(s->s.publish(event));
			Adapters.forEach(s->s.process(event));
			return this;
		}

		public void terminate() {
			this.publishers.forEach(Publisher::terminate);
		}
	}

	private static List<Publisher> publishersOf(String type) {
		List<Publisher> publishers = new ArrayList<>();
		publishers.add(new DatamartPublisher(type));
		publishers.add(new DatalakePublisher(type));
		return publishers;
	}
}
