package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.goals.crawler.*;
import com.cinepolis.cosmos.monitor.inventory.Device;
import spark.Request;
import spark.Spark;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class WebService {
	public static void run() {
		Spark.port(8080);
		Spark.get("/mode/:mode", (req, res) -> Form.debug = req.params(":mode").equals("raw"));
		Spark.get("/forms", (req, res) -> forms());
		Spark.get("/devices", (req, res) -> devices());
		Spark.get("/devices/:device", (req, res) -> deviceIn(req).model().name);
		Spark.get("/devices/:device/:view", (req, res) -> deviceIn(req).read(viewIn(req)));
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

	public static String viewIn(Request req) {
		return req.params(":view");
	}

	private static String devices() {
		return Inventory.Devices.stream()
				.map(Device::toString)
				.collect(joining("\n"));
	}

	private static DeviceAccessor deviceIn(Request req) {
		Device device = Inventory.device(req.params(":device"));
		System.out.println(device);
		return new SnmpDeviceAccessor(device).init();
	}

	public static String forms() {
		try {
			Model.reload();
			return Model.Forms.keySet().stream().sorted().collect(joining("\n"));
		}
		catch (Exception e) {
			e.printStackTrace();
			return "Error loading bases";
		}
	}


}
