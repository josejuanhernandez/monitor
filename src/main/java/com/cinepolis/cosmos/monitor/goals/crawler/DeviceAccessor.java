package com.cinepolis.cosmos.monitor.goals.crawler;

import com.cinepolis.cosmos.monitor.Inventory;

public interface DeviceAccessor {
	Inventory.Device device();
	default String ip() {
		return device().ip;
	}
	DeviceAccessor init();

	Model model();

	String read(String goal);

}
