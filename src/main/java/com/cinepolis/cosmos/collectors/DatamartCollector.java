package com.cinepolis.cosmos.collectors;

import com.cinepolis.cosmos.Collector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

import static com.cinepolis.cosmos.Archetype.AssetDatamart;
import static java.nio.file.StandardOpenOption.CREATE;

public class DatamartCollector implements Collector {
	private final File root;

	public DatamartCollector(String type) {
		this.root = new File(AssetDatamart, type);
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
	public void collect(String source, String message) {
		try {
			collect(new File(root, source + ".inl"), message.trim());
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
