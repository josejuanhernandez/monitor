package com.cinepolis.cosmos.monitor.goals.crawler;

public interface DeviceAccessor {
	Device device();
	default String ip() {
		return device().ip;
	}
	DeviceAccessor init();

	Model model();

	String read(String goal);

}
