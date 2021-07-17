package com.cinepolis.cosmos.monitor.inventory;

import java.util.Arrays;

public class Theater {
	public final String id;
	public final String name;
	public final String city;
	public final String region;
	public final String country;
	public final String vistaId;
	public final String segment;
	public final String screens;

	public Theater(String line) {
		this(splitOf(line));
	}

	public Theater(String[] split) {
		this(split[0], split[1], split[2], split[3], split[4], split[5], split[6], split[7]);
	}

	public Theater(String id, String name, String city, String region, String country, String vistaId, String segment, String screens) {
		this.id = id;
		this.name = name;
		this.city = city;
		this.region = region;
		this.country = country;
		this.vistaId = vistaId;
		this.segment = segment != null ? segment : "?.";
		this.screens = screens != null ? screens : "?.";
	}

	public String key() {
		return country + vistaId;
	}

	@Override
	public String toString() {
		return id + '\t' + name + '\t' + city + '\t' + region + '\t' + country + '\t' + segment;
	}

	public String server(int screen) {
		return segment + (screen + 20);
	}

	public String projector(int screen) {
		return segment + (screen + 40);
	}

	private static String[] splitOf(String line) {
		return Arrays.copyOf(line.split("\t"), 8);
	}
}
