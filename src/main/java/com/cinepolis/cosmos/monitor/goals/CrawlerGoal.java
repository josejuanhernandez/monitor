package com.cinepolis.cosmos.monitor.goals;

import com.cinepolis.cosmos.monitor.Event;
import com.cinepolis.cosmos.monitor.Inventory;
import com.cinepolis.cosmos.monitor.Goal;
import com.cinepolis.cosmos.monitor.goals.crawler.DeviceAccessor;
import com.cinepolis.cosmos.monitor.goals.crawler.SnmpDeviceAccessor;

import java.util.Iterator;
import java.util.stream.Stream;

public class CrawlerGoal implements Goal {

	private final String name;
	private final int delay;
	private final int period;

	public CrawlerGoal(String name, int delay, int period) {
		this.name = name;
		this.delay = delay;
		this.period = period;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public long delay() {
		return delay;
	}

	@Override
	public long period() {
		return period;
	}

	@Override
	public void start() {

	}

	@Override
	public void terminate() {

	}

	@Override
	public Iterator<Task> iterator() {
		return deviceAccessors().map(this::taskOf).iterator();
	}

	private static Stream<DeviceAccessor> deviceAccessors() {
		return Inventory.Devices.stream()
				.map(SnmpDeviceAccessor::new);
	}

	private Task taskOf(DeviceAccessor device) {
		return () -> eventOf(device);
	}

	private Event eventOf(DeviceAccessor device) {
		String event = device.read(name);
		return new Event() {
			@Override
			public String id() {
				return device.ip();
			}

			@Override
			public String serialize() {
				return event;
			}
		};
	}


}
