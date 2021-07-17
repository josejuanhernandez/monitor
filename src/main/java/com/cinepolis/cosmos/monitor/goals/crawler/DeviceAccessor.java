package com.cinepolis.cosmos.monitor.goals.crawler;

import com.cinepolis.cosmos.monitor.inventory.Device;

public interface DeviceAccessor {
	Device device();
	default String ip() {
		return device().ip;
	}
	DeviceAccessor init();

	Model model();

	String read(String view);

}
