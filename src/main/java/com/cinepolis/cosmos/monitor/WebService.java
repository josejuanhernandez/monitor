package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.goals.crawler.*;
import spark.Request;
import spark.Spark;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class WebService {
	public static void run() {
		Spark.port(8080);
		Spark.get("/mode/:mode", (req, res) -> Debug.rawMode = req.params(":mode").equals("raw"));
		Spark.get("/forms", (req, res) -> forms());
		Spark.get("/devices", (req, res) -> devices());
		Spark.get("/devices/:device", (req, res) -> deviceIn(req).model().name);
		Spark.get("/devices/:device/:goal", (req, res) -> deviceIn(req).read(taskIn(req)));
		Spark.post("/devices/:device/:oids", (req, res) -> get(deviceIn(req), req.body().split("\n")));
	}

	private static String get(DeviceAccessor device, String... oids) {
		try (SnmpClient client = new SnmpClient(device.ip()).start()) {
			return join("\n", client.download(asList(oids)).values());
		}
		catch (Exception e) {
			return device + "\tsnmp client error";
		}
	}

	public static String taskIn(Request req) {
		return req.params(":goal");
	}

	private static String devices() {
		return Inventory.Devices.stream()
				.map(Inventory.Device::toString)
				.collect(joining("\n"));
	}

	private static DeviceAccessor deviceIn(Request req) {
		return new SnmpDeviceAccessor(Inventory.find(req.params(":device"))).init();
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

	public static class Debug {
		public static boolean rawMode = false;
	}
}
