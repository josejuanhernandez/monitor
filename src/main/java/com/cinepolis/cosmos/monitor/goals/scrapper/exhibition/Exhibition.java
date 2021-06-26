package com.cinepolis.cosmos.monitor.goals.scrapper.exhibition;

import com.cinepolis.cosmos.monitor.Inventory.Division;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class Exhibition {
	public final String id;
	public final String cinemaId;
	public final String cinema;
	public final String city;
	public final String movie;
	public final LocalTime time;
	public final int timeZone;

	public Exhibition(String id, String cinemaId, String cinema, String city, String movie, LocalTime time, int timeZone) {
		this.id = id;
		this.cinemaId = cinemaId;
		this.cinema = cinema;
		this.city = city;
		this.movie = movie;
		this.time = time;
		this.timeZone = timeZone;
	}

	public static List<Exhibition> read(List<Division> divisions) {
		return divisions.parallelStream()
				.map(Zone::read)
				.filter(Exhibition::isValidZone)
				.flatMap(Exhibition::read)
				.collect(toList());
	}

	private static boolean isValidZone(Zone z) {
		return z != null && z.cinemas != null;
	}

	private static Stream<Exhibition> read(Zone zone) {
		return zone.cinemas.stream().flatMap(Exhibition::read);

	}

	private static Stream<Exhibition> read(Cinema cinema) {
		for (Schedule schedule : cinema)
			if (schedule.isToday())
				return exhibitionsIn(cinema, schedule).stream();
		return Stream.empty();
	}

	private static List<Exhibition> exhibitionsIn(Cinema cinema, Schedule schedule) {
		List<Exhibition> exhibitions = new ArrayList<>();
		for (Movie movie : schedule)
			for (Format format : movie)
				for (ShowTime show : format)
					exhibitions.add(new Exhibition(show.id, show.cinemaId, cinema.name, cinema.cityName, movie.title, show.time(), cinema.timeZoneDifference));
		return exhibitions;
	}


	private static final int Minute = 60;
	private static final int Hour = 60 * Minute;
	public boolean isActive(int now) {
		return Math.abs(time.toSecondOfDay() - timeZone * Hour - now) < 30 * Minute;
	}

	@Override
	public String toString() {
		return "showId=" + id + ',' + "cinemaId=" + cinemaId + ',' +
				"cinema=" + cinema + ',' + "city=" + city + ',' +
				"movie=" + movie + ',' +
				"time=" + time + ',' + "timeZone=" + timeZone;
	}
}
