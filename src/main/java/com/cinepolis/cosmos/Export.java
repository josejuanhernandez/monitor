package com.cinepolis.cosmos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class Export {
	public static void main(String[] args) throws IOException {
		List<Task> tasks = List.of(
				new Task("projector.barco.dp2k.6e", "10.120.13.46"),
				new Task("projector.barco.dp2k.10s", "10.117.6.49"),
				new Task("projector.barco.dp2k.10slp", "10.116.53.48"),
				new Task("projector.barco.dp2k.17blp", "10.109.33.42"),
				new Task("projector.barco.dp2k.19b", "10.116.4.43"),
				new Task("projector.barco.dp2k.20c", "10.98.3.52"),
				new Task("projector.barco.dp2k.23b", "10.98.3.51"),
				new Task("projector.barco.dp2k.23blp", "10.103.5.57"),
				new Task("projector.barco.dp2k.32b", "10.117.25.42"),
				new Task("projector.barco.dp2k.36blp", "10.103.5.42"),
				new Task("projector.barco.dp2k.8s", "10.115.15.47"),
				new Task("projector.barco.dp2k.8slp", "10.99.19.42"),
				new Task("projector.barco.dp4k.17blp", "10.116.53.50"),
				new Task("projector.barco.dp4k.23b", "10.109.15.54"),
				new Task("projector.barco.dp4k.32b", "10.116.6.41"),
				new Task("projector.barco.dp4k.36blp", "10.103.5.41"),
				new Task("projector.barco.sp4k-15c", "10.115.4.41"),
				new Task("projector.christie.cp2210", "10.199.13.41"),
				new Task("projector.christie.cp2220", "10.199.13.46"),
				new Task("projector.christie.cp2230", "10.199.4.42"),
				new Task("projector.christie.solaria", "10.199.15.52"),
				new Task("imb.doremi.dcp-2000", "10.199.5.26"),
				new Task("imb.doremi.ims-1000", "10.124.1.24"),
				new Task("imb.doremi.ims-2000", "10.124.1.24"),
				new Task("imb.doremi.ims-3000", "10.108.3.24"),
				new Task("imb.doremi.show-vault", "10.103.5.22"),
				new Task("imb.gdc.sr-1000", "10.201.53.22"),
				new Task("imb.gdc.sx-3000", "10.203.32.24"),
				new Task("imb.gdc.sx-4000", "10.201.17.25"));

		execute(tasks, "configuration");
		execute(tasks, "status");
	}

	private static void execute(List<Task> devices, String type) throws IOException {
		String configuration = devices.parallelStream().map(t -> t.export(type)).collect(joining("\n----------------------------------------------------\n\n"));
		Files.write(Path.of(type +".txt"),configuration.getBytes(), StandardOpenOption.CREATE);
	}

	private static class Task {
		private final String name;
		private final String ip;

		public Task(String name, String ip) {
			this.name = name;
			this.ip = ip;
		}

		public String export(String task) {
			try {
				return new String(urlOf(task).openStream().readAllBytes());
			} catch (IOException e) {
				return name + "\n" + "ERROR" + "\n";
			}
		}

		private URL urlOf(String task) throws MalformedURLException {
			return new URL("http://localhost:8080/devices/" + ip + "/" + task);
		}
	}
}
