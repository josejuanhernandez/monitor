package com.cinepolis.cosmos.monitor.goals.scrapper.model.exhibition;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;

public class Schedule implements Iterable<Movie> {
	@SerializedName("ShowtimeDate")
	public String date;
	public List<Movie> movies;

	public boolean isToday() {
		return today().equals(date);
	}

	private static final String[] months = new String[] {"enero","febrero","marzo","abril","mayo","junio","julio","agosto","septiembre","octubre","noviembre","diciembre"};
	private String today() {
		LocalDate now = LocalDate.now();
		return now.getDayOfMonth() + " " + months[now.getMonthValue()-1];
	}

	@Override
	public Iterator<Movie> iterator() {
		return movies.iterator();
	}
}
