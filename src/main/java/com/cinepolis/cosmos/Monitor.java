package com.cinepolis.cosmos;

import com.cinepolis.cosmos.Model.Task;
import com.cinepolis.cosmos.collectors.DatalakeCollector;
import com.cinepolis.cosmos.collectors.DatamartCollector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.cinepolis.cosmos.Model.Task.Configuration;
import static com.cinepolis.cosmos.Model.Task.Status;


public class Monitor {
	private static final int ThreadPoolSize = 4096;
	private final List<DeviceAccessor> devices;
	private final Timer timer;

	public Monitor(List<DeviceAccessor> devices) {
		this.devices = devices;
		this.timer = new Timer();
	}

	private static final int Minutes = 1000 * 60;
	private static final int Day = 24 * 60 * Minutes;
	public void run() {
		timer.schedule(timerTask(of(Configuration)), Minutes, Day);
		timer.schedule(timerTask(of(Status)),15*Minutes, 15*Minutes);
	}

	public void runAlt() {
		timer.schedule(sweep(),0, Day);
	}

	private TimerTask sweep() {
		return new TimerTask() {
			@Override
			public void run() {
				ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ThreadPoolSize);
				StringBuffer sb = new StringBuffer();
				for (DeviceAccessor device : devices)
					executor.submit(() -> sb.append(lineOf(device)));
				awaitFor(executor);
				try {
					Files.write(Path.of("devices.tsv"), sb.toString().getBytes(), StandardOpenOption.CREATE);
				} catch (IOException ignored) {
				}
			}
		};
	}

	private String lineOf(DeviceAccessor device) {
		return device.init().toString() + "\n";
	}

	private TimerTask timerTask(MonitorTask task) {
		return new TimerTask() {
			@Override
			public void run() {
				task.run();
			}
		};
	}

	public MonitorTask of(Task task) {
		return new MonitorTask(task)
				.add(new DatalakeCollector(task.name))
				.add(new DatamartCollector(task.name));
	}

	public class MonitorTask {
		private final Task task;
		private final List<Collector> collectors;

		public MonitorTask(Task task) {
			this.task = task;
			this.collectors = new ArrayList<>();
		}

		public void run() {
			if (collectors.size() == 0) return;

			Logger.info("Dump for " + task + " started");
			collectors.forEach(Collector::start);
			dump(collectors);
			collectors.forEach(Collector::terminate);
			Logger.info("Dump for " + task + " terminated");
		}

		private void dump(List<Collector> collectors) {
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(ThreadPoolSize);
			Logger.info("Thread pool begin");
			for (DeviceAccessor device: devices)
				executor.submit(() -> dump(collectors, device));
			Logger.info("Threads submitted");
			awaitFor(executor);
			Logger.info("Thread pool end");
		}

		private void dump(List<Collector> collectors, DeviceAccessor device) {
			collectors.forEach(c -> c.collect(device.ip(), read(device)));
		}

		private String read(DeviceAccessor device) {
			return device.init().execute(task);
		}

		public MonitorTask add(Collector collector) {
			collectors.add(collector);
			return this;
		}

	}

	private void awaitFor(ThreadPoolExecutor executor) {
		try {
			executor.shutdown();
			executor.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			Logger.error(e.getMessage());
		}
	}

}
