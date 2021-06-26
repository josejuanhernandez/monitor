package com.cinepolis.cosmos.monitor;

import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.cinepolis.cosmos.monitor.Archetype.Network;
import static com.cinepolis.cosmos.monitor.Archetype.Territory;
import static java.lang.Integer.parseInt;
import static java.nio.file.Files.readAllLines;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Inventory {
	public static final List<Device> Devices = loadDevices();
	public static final List<Division> Divisions = loadDivisions();
	public static final ZoneId Mexico = ZoneId.of("America/Mexico_City");

	public static Device find(String ip) {
		return Devices.stream()
				.filter(d -> d.ip.equals(ip))
				.findFirst()
				.orElse(new Device(ip));
	}

	private static List<Device> loadDevices() {
		try {
			return readDevices();
		} catch (IOException e) {
			System.err.println("Devices could not be loaded");
			return emptyList();
		}
	}

	private static List<Device> readDevices() throws IOException {
		return readAllLines(Network.toPath()).stream()
				.map(Inventory::devices)
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private static List<Device> devices(String segment) {
		return devices(segment.split("\t"));
	}

	private static List<Device> devices(String[] split) {
		return devices(
				new Theater(split[4], split[5], split[6], split[7], split[8]),
				split[0], split[1], parseInt(split[2]), parseInt(split[3])
		);
	}

	private static List<Device> devices(Theater theater, String operationSegment, String projectionSegment, int screens, int offset) {
		List<Device> devices = new ArrayList<>();
		if (!operationSegment.isEmpty()) {
			devices.add(new Device(operationSegment + (offset + 1), theater, 0));
		}
		if (!projectionSegment.isEmpty()) {
			//devices.add(new Device(projectionSegment + (offset+6), theater, 0));
			for (int i = 1; i <= screens; i++) {
				devices.add(new Device(projectionSegment + (i + offset + 20), theater, i));
				devices.add(new Device(projectionSegment + (i + offset + 40), theater, i));
			}
		}
		return devices;
	}

	private static List<Division> loadDivisions() {
		try {
			return readAllLines(Territory.toPath()).stream().map(Division::new).collect(toList());
		} catch (IOException e) {
			return emptyList();
		}
	}

	public static class Division {
		public final String code;

		public Division(String code) {
			this.code = code;
		}
	}

	public static class Device {
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
