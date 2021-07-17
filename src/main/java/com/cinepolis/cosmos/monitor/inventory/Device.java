package com.cinepolis.cosmos.monitor.inventory;

import com.cinepolis.cosmos.monitor.Inventory;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Device {
	public final String ip;
	public final int screen;
	public final Theater theater;

	public Device(String ip, Theater theater, int screen) {
		this.ip = ip;
		this.screen = screen;
		this.theater = theater;
	}

	@Override
	public String toString() {
		return ip + "\t" + theater.id + "\t" + theater.name + "(" + theater.country + ")" + "\t" + screen;
	}


	public static List<Device> in(String[] split) {
		return in(split[6], split[7], Inventory.theater(split[0]));
	}

	private static List<Device> in(String segment, String offsets, Theater theater) {
		List<Device> devices = new ArrayList<>();
		int screen = 0;
		for (int offset : asList(offsets)) {
			devices.add(new Device(segment + (offset + 20), theater, screen));
			devices.add(new Device(segment + (offset + 40), theater, screen));
			screen++;
		}
		return devices;
	}

	private static List<Integer> asList(String screens) {
		List<Integer> list = new ArrayList<>();
		String[] split = screens.split(",");
		for (String definition : split)
			for (int value : new Range(definition)) list.add(value);
		return list;
	}

	private static class Range implements Iterable<Integer> {
		private final int from;
		private final int to;

		Range(String definition) {
			this(definition.split("\\.\\."));
		}

		public Range(String[] split) {
			this.from = Integer.parseInt(split[0]);
			this.to = Integer.parseInt(split[1]);
		}

		@Override
		public Iterator<Integer> iterator() {
			return new Iterator<>() {
				int i = from;

				@Override
				public boolean hasNext() {
					return i <= to;
				}

				@Override
				public Integer next() {
					return i++;
				}
			};
		}
	}

}
