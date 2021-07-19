package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model;

import com.cinepolis.cosmos.monitor.Inventory;
import com.cinepolis.cosmos.monitor.inventory.Division;
import com.cinepolis.cosmos.monitor.inventory.Theater;
import com.google.gson.annotations.SerializedName;


import java.util.Iterator;
import java.util.List;

public class Cinema implements Iterable<Schedule> {
	public String cityKey;
	public String cityName;
	public int status;
	public String vistaId;
	public String name;
	@SerializedName("TimeZoneDifference")
	public int timeOffset;
	@SerializedName("Dates")
	public List<Schedule> schedules;
	private Division division;
	private Theater theater;

	public String country() {
		return division.country;
	}

	@Override
	public Iterator<Schedule> iterator() {
		return schedules.iterator();
	}

	public void set(Division division) {
		this.division = division;
	}

	@Override
	public String toString() {
		return vistaId + "\t" + name + '\t' +
				cityName + '\t' + division.country + "\t" +
				status + '\t' + timeOffset;
	}

	public Theater theater() {
		if (theater == null) theater = Inventory.theater(country() + vistaId);
		if (theater == null) {
			theater = Inventory.add(new Theater("?", name, cityName, "?", division.country, vistaId, null, null));
			System.out.println(theater);
		}
		return theater;
	}
}
