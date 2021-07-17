package com.cinepolis.cosmos.monitor.datahub;

public interface Event {
	String source();
	String serialize();
}
