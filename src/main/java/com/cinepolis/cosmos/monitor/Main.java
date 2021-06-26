package com.cinepolis.cosmos.monitor;

import java.io.IOException;

import static java.util.Arrays.stream;

public class Main {
	private static final Monitor monitor = new Monitor(new Goal.Factory());

	public static void main(String[] args) throws IOException, InterruptedException {
		WebService.run();
		if (isDebug(args)) return;
		monitor.run();
	}

	private static boolean isDebug(String[] args) {
		return stream(args).anyMatch(s -> s.contains("--debug"));
	}



}
