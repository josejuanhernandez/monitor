package com.cinepolis.cosmos.monitor.datahub.publishers;

import com.cinepolis.cosmos.monitor.datahub.Event;
import com.cinepolis.cosmos.Archetype;
import com.cinepolis.cosmos.monitor.datahub.Publisher;
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

	@Override
	public void start() {
		temp = tempFile();
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
	public synchronized void publish(Event event) {
		write(wrap(event.serialize()));
	}

	private void write(byte[] bytes) {
		try {
			Files.write(temp.toPath(), bytes, CREATE, APPEND);
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
			Files.write(file.toPath(), data(), CREATE);
			Logger.info(this.tank.getName() + "/" + file.getName() + " created. " + file.length() + " bytes");
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
		return new File(Archetype.Datalake, tank);
	}

	private static String date() {
		return Instant.now().toString()
				.replace("-","")
				.replace("T","")
				.replace(":","")
				.substring(0,12);
	}

}
