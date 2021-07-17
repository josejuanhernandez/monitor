package com.cinepolis.cosmos.monitor.inventory;

public class Division {
	public final String code;
	public final String country;

	public Division(String data) {
		this(data.split("\t"));
	}

	public Division(String[] data) {
		this.code = data[0];
		this.country = data[1];
	}

	@Override
	public String toString() {
		return country + "-" + code;
	}
}
