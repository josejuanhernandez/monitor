package com.cinepolis.cosmos.monitor.broker;

import com.cinepolis.cosmos.monitor.Event;

public interface Publisher {
	void start();
	void publish(Event event);
	void terminate();
}

