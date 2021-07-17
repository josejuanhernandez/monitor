package com.cinepolis.cosmos.monitor.goals.crawler;

import com.cinepolis.cosmos.monitor.inventory.Device;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class SnmpDeviceAccessor implements DeviceAccessor {
	private final Device device;
	private Model model = Model.Unreachable;

	public SnmpDeviceAccessor(Device device) {
		this.device = device;
	}

	@Override
	public Device device() {
		return device;
	}

	@Override
	public String ip() {
		return device.ip;
	}

	@Override
	public Model model() {
		return model;
	}

	public DeviceAccessor init() {
		try {
			this.model = modelIn(contentOf(initForm()));
		} catch (IOException e) {
			this.model = Model.Unreachable;
		}
		return this;
	}

	private Model modelIn(String content) {
		return modelIn(asMap(content));
	}

	private Model modelIn(Map<String, String> attributes) {
		return attributes.containsKey("model") ?
				Model.of(attributes.get("model")) :
				Model.Unreachable;
	}

	public String read(String view) {
		return pretty(contentOf(view));
	}

	private String contentOf(String view) {
		try {
			return isUnreachable() ? unreachableOf(view) : contentOf(queriesOf(view));
		}
		catch (Exception e) {
			return unreachableOf(view);
		}
	}

	private String unreachableOf(String form) {
		return Form.Unreachable.raw().replace("$event", form.toLowerCase());
	}

	private boolean isUnreachable() {
		return model == Model.Unreachable;
	}

	private String contentOf(Stream<Query> queries) throws IOException {
		try (SnmpClient client = new SnmpClient(this.device.ip, this.model.oidMapper).start()) {
			return contentOf(queries, client);
		}
	}

	private String contentOf(Stream<Query> queries, SnmpClient client) {
		return queries.map(query -> format(query, client)).collect(joining(""));
	}

	private String format(Query query, SnmpClient client) {
		Map<String, String> attributes = client.download(query.keys());
		return query.format(attributes);
	}

	private Map<String, String> asMap(String text) {
		return asMap(text.split("\n"));
	}

	private Map<String, String> asMap(String[] lines) {
		Map<String,String> attributes = new HashMap<>();
		for (String line : lines) {
			if (!isAttribute(line)) continue;
			String[] split = line.split(":");
			attributes.put(split[0],split[1]);
		}
		return attributes;
	}

	private boolean isAttribute(String line) {
		return line.contains(":") && !line.contains("noSuchObject");
	}

	private Stream<Query> initForm() {
		return List.of(new Query(Form.Init)).stream();
	}

	private Stream<Query> queriesOf(String view) {
		return model.queries.stream().filter(q->q.is(view));
	}

	private String pretty(String text) {
		return text
				.replace("$ts", Instant.now().toString())
				.replace("$ip", device.ip)
				.replace("$theater", device.theater.id + "-" + device.theater.name)
				.replace("$city", device.theater.city)
				.replace("$region", device.theater.city)
				.replace("$country", device.theater.country)
				.replace("$screen", String.valueOf(device.screen != 0 ? device.screen : "-"))
				.replace("$model", model.name)
				.replace("\n[", "\n\n[")
				.replaceAll("(^\\.+?):","$1: ");
	}

	@Override
	public String toString() {
		return device + ":" + model;
	}

}
