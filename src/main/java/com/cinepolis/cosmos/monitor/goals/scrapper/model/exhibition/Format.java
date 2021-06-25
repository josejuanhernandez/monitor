package com.cinepolis.cosmos.monitor.goals.scrapper.model.exhibition;

import com.google.gson.annotations.SerializedName;

import java.util.Iterator;
import java.util.List;

public class Format implements Iterable<ShowTime> {
	public String vistaId;
	public String name;
	public boolean isExperience;
	public String SegobPermission;
	public String language;
	@SerializedName("Showtimes")
	public List<ShowTime> shows;

	@Override
	public Iterator<ShowTime> iterator() {
		return shows.iterator();
	}
}
