package com.cinepolis.cosmos.monitor.brokers;

import com.cinepolis.cosmos.monitor.Broker;
import com.cinepolis.cosmos.monitor.Feeder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static com.cinepolis.cosmos.monitor.goals.crawler.Archetype.TrooperDatamart;
import static java.nio.file.StandardOpenOption.CREATE;

public class DatamartBroker implements Broker {
	private final File root;

	public DatamartBroker(String type) {
		this.root = new File(TrooperDatamart, type);
	}

	@Override
	public void start() {
		root.mkdirs();
		nonNull(root.listFiles()).forEach(File::delete);
	}

	private static Stream<File> nonNull(File[] files) {
		return files != null ? Stream.of(files) : Stream.empty();
	}

	@Override
	public void process(Feeder feeder) {
		try {
			collect(new File(root, feeder.name() + ".inl"), feeder.event());
		} catch (IOException ignored) {
		}
	}

	private void collect(File file, String message) throws IOException {
		Files.write(file.toPath(), message.getBytes(), CREATE);
	}

	@Override
	public void terminate() {

	}
}
