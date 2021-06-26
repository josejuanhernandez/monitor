package com.cinepolis.cosmos.monitor.goals.scrapper.session;

import com.cinepolis.cosmos.monitor.Event;
import com.cinepolis.cosmos.monitor.goals.scrapper.exhibition.Exhibition;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.cinepolis.cosmos.monitor.Archetype.Network;
import static java.nio.file.Files.readAllLines;
import static java.util.stream.Collectors.toMap;
import static org.jsoup.Connection.Method.POST;

public class Session implements Event {

	@SerializedName("session_id")
	public String id;
	public String showId;
	public String cinemaId;
	public String cinema;
	public String city;
	public String region;
	public String country = "Mexico";
	public List<Area> areas;
	public String movie;
	public LocalTime time;
	public int timeZone;
	public int sold;
	public int seats;
	public String screen;
	public String projector;
	public String server;

	private static final Session Null = new Session();
	public static Session read(Exhibition exhibition) {
		try {
			Session session = asSession(download(exhibition));
			session.showId = exhibition.id;
			session.cinemaId = exhibition.cinemaId;
			session.movie = exhibition.movie;
			session.time = exhibition.time;
			session.timeZone = exhibition.timeZone;
			session.add(Show.read(session));
			session.add(segmentOf(session.cinemaId));
			return session;
		} catch (IOException e) {
			return Session.Null;
		}
	}

	private void add(Show show) {
		screen = show.screen;
		seats = show.seats;
		sold = show.sold;
	}

	private void add(String[] segment) {
		projector = withoutScreen() ? "NA" : segment[1] + screenPlus(20);
		server = withoutScreen() ? "NA" : segment[1] + screenPlus(40);
		cinema = segment[5];
		city = segment[6];
		region = segment[7];
		country = segment[8];
	}

	private static Session asSession(String json) {
		return new Gson().fromJson(json, Session.class);
	}

	private static final String uri = "https://sls-api-compra.cinepolis.com/api/tickets";
	private static final String request = "{\"cinema_vista_id\":\":cinemaId:\",\"showtime_vista_id\":\":showId:\",\"reload\":true}";

	private static String download(Exhibition exhibition) throws IOException {
		return Jsoup.connect(uri)
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.ignoreContentType(true)
				.requestBody(request.replace(":cinemaId:", exhibition.cinemaId).replace(":showId:", exhibition.id))
				.method(POST)
				.execute()
				.body();
	}

	@Override
	public String id() {
		return projector;
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
	public String serialize() {
		return Template
				.replace("$ts", Instant.now().toString())
				.replace("$movie", movie)
				.replace("$timeZone", String.valueOf(timeZone))
				.replace("$time", time.toString())
				.replace("$theater", theater())
				.replace("$screen", screen)
				.replace("$city", city)
				.replace("$region", region)
				.replace("$country", country)
				.replace("$seats", String.valueOf(seats))
				.replace("$sold", String.valueOf(sold))
				.replace("$projector", projector)
				.replace("$server", server);
	}

	private String theater() {
		return "1020" + cinemaId + "-" + cinema;
	}

	public static final Map<String, String[]> Segments = loadSegments();

	private static Map<String, String[]> loadSegments() {
		try {
			return readAllLines(Network.toPath()).stream()
					.filter(s->s.contains("\t1020"))
					.map(s->s.split("\t"))
					.collect(toMap(s1 -> s1[4], Function.identity()));
		} catch (IOException e) {
			return new HashMap<>();
		}
	}

	private static String[] segmentOf(String cinemaId) {
		return Segments.get("1020" + cinemaId);
	}

	private boolean withoutScreen() {
		return screen.isEmpty() || screen.equals("NA");
	}

	private int screenPlus(int offset) {
		return offset + Integer.parseInt(screen);
	}

	@Override
	public String toString() {
		return "showId=" + showId + ',' +
				"cinemaId=" + cinemaId + ',' +
				"cinema=" + cinema + ',' +
				"screen=" + screen + ',' +
				"city=" + city + ',' +
				"movie=" + movie + ',' +
				"time=" + time + ',' +
				"timeZone=" + timeZone + ',' +
				"seats=" + seats + ',' +
				"sold=" + sold + ',' +
				"projector=" + projector + ',' +
				"server=" + server;

	}
}
