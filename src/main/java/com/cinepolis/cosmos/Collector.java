package com.cinepolis.cosmos;


public interface Collector {
	void start();
	void collect(String source, String message);
	void terminate();
}
