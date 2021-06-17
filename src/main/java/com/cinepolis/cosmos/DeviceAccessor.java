package com.cinepolis.cosmos;

import com.cinepolis.cosmos.Model.Task;

public interface DeviceAccessor {
	Device device();
	default String ip() {
		return device().ip;
	}
	DeviceAccessor init();

	Model model();

	String execute(Task task);

}
