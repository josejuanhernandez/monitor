package com.cinepolis.cosmos.monitor.goals.scrapper.exhibition;

import com.google.gson.annotations.SerializedName;

import java.time.LocalTime;

class ShowTime {
	@SerializedName("VistaCinemaId")
	public String cinemaId;
	@SerializedName("ShowtimeId")
	public String id;
	public String time;

	public LocalTime time() {
		return LocalTime.parse(time);
	}
}
