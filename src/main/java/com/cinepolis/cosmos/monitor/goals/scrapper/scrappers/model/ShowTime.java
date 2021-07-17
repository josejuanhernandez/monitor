package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalTime;

public class ShowTime {
	@SerializedName("VistaCinemaId")
	public String cinemaId;
	@SerializedName("ShowtimeId")
	public String id;
	public String time;

	public LocalTime time() {
		return LocalTime.parse(time);
	}
}
