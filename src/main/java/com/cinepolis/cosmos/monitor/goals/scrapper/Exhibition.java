package com.cinepolis.cosmos.monitor.goals.scrapper;

import com.cinepolis.cosmos.monitor.datahub.Event;
import com.cinepolis.cosmos.monitor.inventory.Theater;

import java.time.Instant;
import java.time.LocalTime;

public class Exhibition implements Event {
	public final String showId;
	public final String movie;
	public final LocalTime time;
	public final double timeOffset;
	public final String country;
	public final Theater theater;
	private int seats;
	private int sold;
	private int screen;
	private String projector;
	private String server;

	public Exhibition(String showId, String movie, LocalTime time, double timeOffset, String country, Theater theater) {
		this.showId = showId;
		this.movie = movie;
		this.time = time;
		this.timeOffset = timeOffset;
		this.country = country;
		this.theater = theater;
	}

	@Override
	public String toString() {
		return theater.id + "\t" + theater.vistaId + "\t" + showId + '\t' + movie + '\t' + time + '\t' + screen;
	}

	@Override
	public String source() {
		return projector;
	}

	private static final String Template =
			"[exhibition]\n" +
			"ts: $ts\n" +
			"movie: $movie\n" +
			"time: $time\n" +
			"timeOffset: $timeOffset\n" +
			"theater: $theater\n" +
			"screen: $screen\n" +
			"city: $city\n" +
			"region: $region\n" +
			"country: $country\n" +
			"seats: $seats\n" +
			"sold: $sold\n" +
			"projector: $projector\n" +
			"server: $server";

	@Override
	public String serialize() {
		return Template
				.replace("$ts", Instant.now().toString())
				.replace("$movie", movie)
				.replace("$timeOffset", String.valueOf(timeOffset))
				.replace("$time", time.toString())
				.replace("$theater", theater.id + "-" + theater.name)
				.replace("$city", theater.city)
				.replace("$region", theater.region)
				.replace("$country", theater.country)
				.replace("$screen", String.valueOf(screen))
				.replace("$seats", String.valueOf(seats))
				.replace("$sold", String.valueOf(sold))
				.replace("$server", server)
				.replace("$projector", projector);
	}

	private final static int SecondsPerMinute = 60;
	private final static int MinutesPerHour = 60;
	private final static int MinutesPerDay = MinutesPerHour * 24;
	public int minuteOfDay() {
		int result = time.toSecondOfDay() / SecondsPerMinute - (int) timeOffset * MinutesPerHour;
		if (result < 0) return result + MinutesPerDay;
		if (result > MinutesPerDay) return result - MinutesPerDay;
		return result;
	}

	public boolean hasTheater() {
		return theater != null;
	}

	public boolean isActiveAt(int minuteOfDay) {
		return Math.abs(minuteOfDay() - minuteOfDay) < 20;
	}

	public Exhibition seats(int seats) {
		this.seats = seats;
		return this;
	}

	public Exhibition sold(int sold) {
		this.sold = sold;
		return this;
	}

	public Exhibition screen(int screen) {
		try {
			this.screen = screen;
			this.projector = theater.projector(screen);
			this.server = theater.server(screen);
		}
		catch (NumberFormatException ignored) {
		}
		return this;
	}
}
