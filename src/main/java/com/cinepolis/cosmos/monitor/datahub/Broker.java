package com.cinepolis.cosmos.monitor.datahub;

public class Broker {
	public final static Broker instance = new Broker();

	public Session start(String type) {
		return new Session(type);
	}



}
