package com.cinepolis.cosmos.monitor.goals.scrapper;

import com.cinepolis.cosmos.monitor.Agent;
import com.cinepolis.cosmos.monitor.Feeder;
import com.cinepolis.cosmos.monitor.Goal;
import com.cinepolis.cosmos.monitor.goals.scrapper.model.exhibition.Exhibition;
import com.cinepolis.cosmos.monitor.goals.scrapper.model.show.Show;

import java.io.InputStream;
import java.time.*;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class ShowGoal extends Goal {
	private static final List<String> Zones = loadZones();
	private static final ZoneId Mexico = ZoneId.of("America/Mexico_City");

	private List<Exhibition> exhibitions;
	private LocalDate expired;
	public ShowGoal(String name, int delay, int period) {
		super(name, delay, period);
	}

	@Override
	public Iterator<Feeder> iterator() {
		return new Iterator<>() {
			private final int now = now();
			private final Iterator<Exhibition> iterator = exhibitions().stream()
					.filter(e->e.isActive(now))
					.iterator();


			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}
			@Override
			public Feeder next() {
				return feederOf(iterator.next());
			}

			private int now() {
				return LocalTime.now().toSecondOfDay() + offset(Mexico).getTotalSeconds() - offset(ZoneId.systemDefault()).getTotalSeconds();
			}

			private ZoneOffset offset(ZoneId ZONE) {
				return ZONE.getRules().getOffset(Instant.now());
			}
		};
	}

	private List<Exhibition> exhibitions() {
		if (requireExhibitionsUpdate()) exhibitions = Exhibition.read(Zones);
		expired = LocalDate.now();
		return exhibitions;
	}

	private boolean requireExhibitionsUpdate() {
		return expired == null || (LocalTime.now().getHour() == 15 && LocalDate.now().isBefore(expired));
	}

	public Feeder feederOf(Exhibition exhibition) {
		return new Feeder() {
			private Show show;
			@Override
			public String name() {
				return show.projector;
			}

			@Override
			public String event() {
				return asEvent(show);
			}

			public Feeder init() {
				this.show = exhibition.asShow();
				System.out.println(event().replace("\n","|"));
				Agent.instance.check(show);
				return this;
			}

		};
	}

	private static final String Template =
			"[show]\n" +
			"ts: $ts\n" +
			"movie: $movie\n" +
			"time: $time\n" +
			"timeZone: $timeZone\n" +
			"theater: $theater\n" +
			"screen: $screen\n" +
			"city: $city\n" +
			"region: $region\n" +
			"country: $country\n" +
			"seats: $seats\n" +
			"sold: $sold\n" +
			"projector: $projector\n" +
			"server: $server";
	private String asEvent(Show show) {
		return Template
				.replace("$ts", Instant.now().toString())
				.replace("$movie", show.movie)
				.replace("$timeZone", String.valueOf(show.timeZone))
				.replace("$time", show.time.toString())
				.replace("$theater", show.theater())
				.replace("$screen", show.screen)
				.replace("$city", show.city)
				.replace("$region", show.region)
				.replace("$country", show.country)
				.replace("$seats", String.valueOf(show.seats))
				.replace("$sold", String.valueOf(show.sold))
				.replace("$projector", show.projector)
				.replace("$server", show.server);
	}

	private static List<String> loadZones() {
		return asList(zones().split("\n"));
	}

	private static String zones() {
		InputStream is = ShowGoal.class.getResourceAsStream("/zones.txt");
		return new Scanner(is, UTF_8).useDelimiter("\\A").next();
	}


}
