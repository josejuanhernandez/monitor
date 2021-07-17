package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model;

import com.google.gson.annotations.SerializedName;


import java.util.Iterator;
import java.util.List;


public class Schedule implements Iterable<Movie> {
	@SerializedName("ShowtimeDate")
	public String date;
	public List<Movie> movies;

	@Override
	public Iterator<Movie> iterator() {
		return movies.iterator();
	}
}
