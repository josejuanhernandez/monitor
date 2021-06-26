package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.goals.scrapper.exhibition.Exhibition;
import com.cinepolis.cosmos.monitor.goals.scrapper.session.Session;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

public class MainX {

	public static void maina(String[] args) {
		List<Exhibition> exhibitions = Exhibition.read(List.of("cdmx-sur"));
		exhibitions.forEach(System.out::println);
	}

	public static void main(String[] args) throws IOException {
		Exhibition exhibition = new Exhibition("12558", "151", "", "", "Rapidos y furiosos 9", LocalTime.parse("20:30"), 0);
		Session session = Session.read(exhibition);
		System.out.println(session);
	}

	public static void mainq(String[] args) {
		Monitor monitor = new Monitor(new Goal.Factory());
		monitor.run();
	}

}
