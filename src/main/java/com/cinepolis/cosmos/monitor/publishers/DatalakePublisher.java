package com.cinepolis.cosmos.monitor.publishers;

import com.cinepolis.cosmos.monitor.Event;
import com.cinepolis.cosmos.monitor.Archetype;
import com.cinepolis.cosmos.monitor.broker.Publisher;
import com.cinepolis.cosmos.monitor.Logger;
import org.xerial.snappy.Snappy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class DatalakePublisher implements Publisher {
	private final File tank;
	private File temp;

	public DatalakePublisher(String tank) {
		this.tank = targetFolder(tank);
		this.tank.mkdirs();
	}

	private static File tempFile()  {
		try {
			return File.createTempFile("events", "");
		} catch (IOException e) {
			Logger.error(e.getMessage());
			return null;
		}
	}

	@Override
	public void start() {
		temp = tempFile();
	}

	@Override
	public synchronized void publish(Event event) {
		try {
			Files.write(temp.toPath(), wrap(event.serialize()), CREATE, APPEND);
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
	}

	private byte[] wrap(String message) {
		return (message + "\n\n\n").getBytes();
	}

	@Override
	public void terminate() {
		try {
			File file = new File(this.tank, date() + ".zim");
			Logger.info("Compressing " + file.getAbsolutePath());
			Files.write(file.toPath(), data(), CREATE);
			Logger.info("Compressed " + file.getAbsolutePath());
		} catch (IOException e) {
			Logger.error(e.getMessage());
		}
		finally {
			temp.delete();
		}
	}

	private byte[] data() throws IOException {
		return Snappy.compress(Files.readAllBytes(temp.toPath()));
	}

	private File targetFolder(String tank) {
		return new File(Archetype.Datalake, "events/" + tank);
	}

	private static String date() {
		return Instant.now().toString()
				.replace("-","")
				.replace("T","")
				.replace(":","")
				.substring(0,12);
	}

}
