package com.cinepolis.cosmos.monitor.datahub.publishers;

import com.cinepolis.cosmos.monitor.datahub.Event;
import com.cinepolis.cosmos.monitor.datahub.Publisher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static com.cinepolis.cosmos.Archetype.TrooperDatamart;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class DatamartPublisher implements Publisher {
	private final File root;

	public DatamartPublisher(String name) {
		this.root = new File(TrooperDatamart, name);
	}

	@Override
	public void start() {
		root.mkdirs();
		nonNull(root.listFiles()).forEach(File::delete);
	}

	private static Stream<File> nonNull(File[] files) {
		return files != null ? Stream.of(files) : Stream.empty();
	}

	public void publish(Event event) {
		try {
			write(fileFor(event), event.serialize() + "\n\n");
		} catch (IOException ignored) {
		}
	}

	@Override
	public void terminate() {

	}

	public File fileFor(Event event) {
		return new File(root, event.source() + ".inl");
	}

	private static void write(File file, String message) throws IOException {
		Files.write(file.toPath(), message.getBytes(), CREATE, APPEND);
	}

}
