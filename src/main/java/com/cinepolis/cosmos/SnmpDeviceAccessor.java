package com.cinepolis.cosmos;

import com.cinepolis.cosmos.Model.Task;

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
			this.model = modelIn(parse(execute(queriesForInit())));
		} catch (IOException e) {
			this.model = Model.Unreachable;
		}
		return this;
	}

	private Model modelIn(Map<String, String> attributes) {
		return attributes.containsKey("model") ?
				Model.of(attributes.get("model")) :
				Model.Unreachable;
	}

	public String execute(Task task) {
		return pretty(contentOf(task));
	}

	private String contentOf(Task task) {
		try {
			return isUnreachable() ? unreachableOf(task) : execute(queriesOf(task));
		}
		catch (Exception e) {
			return unreachableOf(task);
		}
	}

	private String unreachableOf(Task task) {
		return Form.Unreachable.raw().replace("event", task.name.toLowerCase());
	}

	private boolean isUnreachable() {
		return model == Model.Unreachable;
	}

	private String execute(Stream<Query> queries) throws IOException {
		try (SnmpClient client = new SnmpClient(this.device.ip, this.model.keyMapper).start()) {
			return execute(queries, client);
		}
	}

	private String execute(Stream<Query> queries, SnmpClient client) {
		return queries.map(query -> format(query, client)).collect(joining(""));
	}

	private String format(Query query, SnmpClient client) {
		Map<String, String> attributes = client.download(query.keys());
		return query.format(attributes);
	}

	private Map<String, String> parse(String text) {
		return parse(text.split("\n"));
	}

	private Map<String, String> parse(String[] lines) {
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

	private Stream<Query> queriesForInit() {
		return List.of(new Query(Form.Init)).stream();
	}

	private Stream<Query> queriesOf(Task task) {
		return model.goals.stream().map(g -> new Query(g.formOf(task), g.tags));
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
				.replace(":",": ");
	}

	@Override
	public String toString() {
		return device + "\t" + model;
	}

}
