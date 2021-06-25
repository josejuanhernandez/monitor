package com.cinepolis.cosmos.monitor;


public interface Broker {
	void start();
	void process(Feeder feeder);
	void terminate();

}

