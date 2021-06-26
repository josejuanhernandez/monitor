package com.cinepolis.cosmos.monitor.goals.scrapper.exhibition;

import com.google.gson.annotations.SerializedName;

import java.util.Iterator;
import java.util.List;

class Cinema implements Iterable<Schedule> {
	public String cityKey;
	public String cityName;
	public int status;
	public int id;
	public String vistaId;
	public String name;
	public boolean isPresale;
	public int timeZoneDifference;

	@SerializedName("Dates")
	public List<Schedule> schedules;

	@Override
	public Iterator<Schedule> iterator() {
		return schedules.iterator();
	}
}
