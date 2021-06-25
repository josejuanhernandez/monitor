package com.cinepolis.cosmos.monitor.goals.scrapper;

import com.cinepolis.cosmos.monitor.GoalFactory;
import com.cinepolis.cosmos.monitor.Monitor;
import com.cinepolis.cosmos.monitor.goals.scrapper.model.exhibition.*;
import com.cinepolis.cosmos.monitor.goals.scrapper.model.show.Show;

import java.io.IOException;
import java.time.LocalTime;

public class Main {

	public static void mainc(String[] args) {
		Zone zone = Zone.read("cdmx-sur");
		for (Cinema cinema : zone) {
			if (!cinema.vistaId.equals("575")) continue;
			for (Schedule schedule : cinema) {
				for (Movie movie : schedule) {
					if (!movie.title.startsWith("Duro")) continue;
					for (Format format : movie) {
						for (ShowTime show : format) {
							System.out.print(show.time + " " + movie.title);
							Exhibition exhibition = new Exhibition(show.id, show.cinemaId, cinema.name, cinema.cityName, movie.title, show.time(), cinema.timeZoneDifference);
							Show show1 = exhibition.asShow();
							System.out.println(" " +show1.screen);
						}
					}
				}
			}
		}
		System.out.println(zone.cinemas.size());
	}

	public static void maina(String[] args) throws IOException {
		Exhibition exhibition = new Exhibition("9326", "575", "", "", "Rapidos y furiosos 9", LocalTime.parse("20:30"), 0);
		Show show = exhibition.asShow();
		System.out.println(show.movie);
		System.out.println(show.time);
		System.out.println(show.seats);
		System.out.println(show.sold);
		System.out.println(show.screen);
	}

	public static void main(String[] args) {
		Monitor monitor = new Monitor(new GoalFactory());
		monitor.run();
	}

}
