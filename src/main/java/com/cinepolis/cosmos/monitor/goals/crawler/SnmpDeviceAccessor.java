package com.cinepolis.cosmos.monitor.goals.crawler;

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

	public String read(String goal) {
		return pretty(contentOf(goal));
	}

	private String contentOf(String goal) {
		try {
			return isUnreachable() ? unreachableOf(goal) : execute(queriesOf(goal));
		}
		catch (Exception e) {
			return unreachableOf(goal);
		}
	}

	private String unreachableOf(String name) {
		return Form.Unreachable.raw().replace("event", name.toLowerCase());
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

	private Stream<Query> queriesOf(String name) {
		return model.books.stream().map(book -> new Query(book.formOf(name), book.tags));
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
