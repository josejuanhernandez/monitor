package com.cinepolis.cosmos.monitor.goals.crawler;

import com.cinepolis.cosmos.monitor.Environment;
import com.cinepolis.cosmos.monitor.Feeder;
import com.cinepolis.cosmos.monitor.Goal;

import java.util.Iterator;
import java.util.stream.Stream;

public class DeviceGoal extends Goal {

	public DeviceGoal(String name, int delay, int period) {
		super(name, delay, period);
	}

	@Override
	public Iterator<Feeder> iterator() {
		return deviceAccessors().map(this::source).iterator();
	}

	private static Stream<DeviceAccessor> deviceAccessors() {
		return Environment.Devices.stream()
				.map(SnmpDeviceAccessor::new);
	}

	private Feeder source(DeviceAccessor device) {
		return feederOf(device);
	}

	private Feeder feederOf(DeviceAccessor device) {
		return new Feeder() {
			private String event;

			@Override
			public String name() {
				return device.ip();
			}

			@Override
			public String event() {
				return event;
			}

			@Override
			public Feeder init() {
				this.event = device.read(name);
				return this;
			}
		};
	}




}
