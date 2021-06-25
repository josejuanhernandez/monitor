package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.goals.crawler.Device;
import com.cinepolis.cosmos.monitor.goals.crawler.DeviceGoal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.cinepolis.cosmos.monitor.goals.crawler.Archetype.Network;
import static java.lang.Integer.parseInt;
import static java.nio.file.Files.readAllLines;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Environment {
	public static final List<Device> Devices = loadDevices();

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
				.map(Environment::devices)
				.flatMap(Collection::stream)
				.collect(toList());
	}

	private static List<Device> devices(String segment) {
		return devices(segment.split("\t"));
	}

	private static List<Device> devices(String[] split) {
		return devices(
				new Device.Theater(split[4], split[5], split[6], split[7], split[8]),
				split[0], split[1], parseInt(split[2]), parseInt(split[3])
		);
	}

	private static List<Device> devices(Device.Theater theater, String operationSegment, String projectionSegment, int screens, int offset) {
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

}
