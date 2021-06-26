package com.cinepolis.cosmos.monitor.goals.scrapper.exhibition;

import com.google.gson.annotations.SerializedName;

import java.util.Iterator;
import java.util.List;

class Format implements Iterable<ShowTime> {
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
