package com.cinepolis.cosmos.monitor.goals.crawler;

import java.util.logging.Level;

public class Logger {
	public static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger("monitor");

	public static void info(String message) {
		logger.log(Level.INFO, message);
	}

	public static void error(String message) {
		logger.log(Level.SEVERE, message);
	}
}
