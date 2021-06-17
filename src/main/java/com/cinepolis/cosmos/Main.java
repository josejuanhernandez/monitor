package com.cinepolis.cosmos;

import com.cinepolis.cosmos.Device.Theater;
import com.cinepolis.cosmos.Model.Task;
import spark.Request;
import spark.Spark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.cinepolis.cosmos.Archetype.Network;
import static com.cinepolis.cosmos.Model.Task.Configuration;
import static com.cinepolis.cosmos.Model.Task.Status;
import static java.lang.Integer.parseInt;
import static java.lang.String.join;
import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class Main {
	public static final List<Device> Devices = loadDevices();
	private static final Monitor monitor = new Monitor(deviceAccessors());

	public static void main(String[] args) throws IOException, InterruptedException {
		runWebService();
		if (isDebug(args)) return;
		monitor.run();
	}

	private static boolean isDebug(String[] args) {
		return stream(args).anyMatch(s -> s.contains("--debug"));
	}

	private static void runWebService() {
		Spark.port(8080);
		Spark.get("/mode/:mode", (req, res) -> Debug.rawMode = req.params(":mode").equals("raw"));
		Spark.get("/forms", (req, res) -> forms());
		Spark.get("/devices", (req, res) -> devices());
		Spark.get("/devices/:device", (req, res) -> deviceIn(req).model().name);
		Spark.post("/devices/:device/:oids", (req, res) -> get(deviceIn(req), req.body().split("\n")));
		Spark.get("/devices/:device/:task", (req, res) -> deviceIn(req).execute(taskIn(req)));
		Spark.get("/dump/:task", (req, res) -> dump(taskIn(req)));
	}

	private static String get(DeviceAccessor device, String... oids) {
		try (SnmpClient client = new SnmpClient(device.ip()).start()) {
			return join("\n", client.download(asList(oids)).values());
		}
		catch (Exception e) {
			return device + "\tsnmp client error";
		}
	}

	public static Task taskIn(Request req) {
		String task = req.params(":task");
		return task != null && task.equalsIgnoreCase("configuration") ? Configuration : Status;
	}

	private static String dump(Task task) {
		new Thread(monitor.of(task)::run).start();
		return "dumping...";
	}

	private static List<DeviceAccessor> deviceAccessors() {
		return Devices.stream()
				.map(SnmpDeviceAccessor::new)
				.collect(toList());
	}

	private static String devices() {
		return Devices.stream()
				.map(Device::toString)
				.collect(joining("\n"));
	}

	private static DeviceAccessor deviceIn(Request req) {
		return new SnmpDeviceAccessor(find(req.params(":device"))).init();
	}

	private static Device find(String ip) {
		return Devices.stream()
				.filter(d -> d.ip.equals(ip))
				.findFirst()
				.orElse(new Device(ip));
	}


	public static List<Device> loadDevices() {
		try {
			return readDevices();
		} catch (IOException e) {
			System.err.println("Devices could not be loaded");
			return emptyList();
		}
	}

	private static List<Device> readDevices() throws IOException {
		return readAllLines(Network.toPath()).stream()
				.map(Main::devices)
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

	public static String forms() {
		try {
			Model.reload();
			return join("\n", Model.Forms.keySet());
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Error loading bases";
		}
	}


}
