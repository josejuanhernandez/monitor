package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model;

import com.google.gson.annotations.SerializedName;


import java.util.Iterator;
import java.util.List;

public class Zone implements Iterable<Cinema> {
	public static final Zone Null = new Zone();
	@SerializedName("Cinemas")
	public List<Cinema> cinemas;

	@Override
	public Iterator<Cinema> iterator() {
		return cinemas.iterator();
	}

}
