package com.cinepolis.cosmos.monitor.goals.crawler;

public class Device {
	public final String ip;
	public final int screen;
	public final Theater theater;

	public Device(String ip, Theater theater, int screen) {
		this.ip = ip;
		this.screen = screen;
		this.theater = theater;
	}

	public Device(String ip) {
		this.ip = ip;
		this.theater = new Theater("-","-","-","-","-");
		this.screen = 0;
	}

	@Override
	public String toString() {
		return ip + "\t" + theater.id + "\t"  + theater.name + "(" + theater.country + ")" +"\t" + screen;
	}

	public static class Theater {
		public final String id;
		public final String name;
		public final String city;
		public final String region;
		public final String country;

		public Theater(String id, String name, String city, String region, String country) {
			this.id = id;
			this.name = name;
			this.city = city;
			this.region = region;
			this.country = country;
		}
	}
}
