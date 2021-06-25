package com.cinepolis.cosmos.monitor;

public interface Feeder {
	Feeder init();
	String name();
	String event();
}
