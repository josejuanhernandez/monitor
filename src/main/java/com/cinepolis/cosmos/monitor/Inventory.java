package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.Archetype;
import com.cinepolis.cosmos.monitor.inventory.Device;
import com.cinepolis.cosmos.monitor.inventory.Division;
import com.cinepolis.cosmos.monitor.inventory.Theater;

import java.io.IOException;
import java.util.*;

import static java.nio.file.Files.readAllLines;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Inventory {
	public static final List<Division> Divisions = loadDivisions();
	public static final List<Theater> Theaters = loadTheaters();
	public static final Map<String, Theater> TheaterMap = new HashMap<>();
	public static final List<Device> Devices = loadDevices();
	public static final Map<String, Device> DeviceMap = new HashMap<>();

	public static Theater theater(String id) {
		if (TheaterMap.isEmpty()) loadTheaterMap();
		return TheaterMap.get(id);
	}

	public static Device device(String ip) {
		if (DeviceMap.isEmpty()) loadDeviceMap();
		return DeviceMap.get(ip);
	}

	private static List<Device> loadDevices() {
		try {
			return readDevices();
		} catch (IOException e) {
			System.err.println("Devices could not be loaded");
			return emptyList();
		}
	}

	private static List<Theater> loadTheaters() {
		try {
			return readTheaters();
		} catch (IOException e) {
			System.err.println("Theaters could not be loaded");
			return emptyList();
		}
	}

	private static List<Division> loadDivisions() {
		try {
			return readDivisions();
		} catch (IOException e) {
			return emptyList();
		}
	}

	private static List<Device> readDevices() throws IOException {
		return readAllLines(Archetype.Theaters.toPath()).stream()
				.map(s->s.split("\t"))
				.filter(s->s.length > 7)
				.map(Device::in)
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private static List<Theater> readTheaters() throws IOException{
		return readAllLines(Archetype.Theaters.toPath()).stream()
				.filter(l->!l.isEmpty())
				.map(Theater::new)
				.collect(toList());
	}

	private static List<Division> readDivisions() throws IOException {
		return readAllLines(Archetype.Divisions.toPath()).stream()
				.filter(l->!l.isEmpty())
				.map(Division::new)
				.collect(toList());
	}

	private static void loadTheaterMap() {
		Theaters.forEach(Inventory::put);
	}

	public static Theater add(Theater theater) {
		TheaterMap.put(theater.key(), theater);
		return theater;
	}


	private static void loadDeviceMap() {
		Devices.forEach(Inventory::put);
	}

	private static void put(Theater theater) {
		TheaterMap.put(theater.id, theater);
		TheaterMap.put(theater.key(), theater);
	}

	private static void put(Device device) {
		DeviceMap.put(device.ip, device);
	}

}
