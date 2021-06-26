package com.cinepolis.cosmos.monitor.publishers;

import com.cinepolis.cosmos.monitor.Event;
import com.cinepolis.cosmos.monitor.broker.Publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.cinepolis.cosmos.monitor.Archetype.TrooperDatamart;
import static java.nio.file.StandardOpenOption.CREATE;

public class DatamartPublisher implements Publisher {
	private final File root;

	public DatamartPublisher(String type) {
		this.root = new File(TrooperDatamart, type);
	}

	@Override
	public void start() {

	}

	public void publish(Event event) {
		try {
			write(new File(root, event.id() + ".inl"), event.serialize());
		} catch (IOException ignored) {
		}
	}

	@Override
	public void terminate() {

	}

	private void write(File file, String message) throws IOException {
		Files.write(file.toPath(), message.getBytes(), CREATE);
	}

}
