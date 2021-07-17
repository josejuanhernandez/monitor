package com.cinepolis.cosmos.monitor.datahub;

public interface Publisher {
	void start();
	void publish(Event event);
	void terminate();
}
