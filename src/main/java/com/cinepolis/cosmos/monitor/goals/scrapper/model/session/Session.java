package com.cinepolis.cosmos.monitor.goals.scrapper.model.session;

import com.cinepolis.cosmos.monitor.goals.scrapper.model.exhibition.Exhibition;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.LocalTime;
import java.util.List;

import static org.jsoup.Connection.Method.POST;

public class Session {
	@SerializedName("session_id")
	public String id;
	public String showId;
	public String cinemaId;
	public String cinema;
	public String city;
	public String region;
	public List<Area> areas;
	public String movie;
	public LocalTime time;
	public int timeZone;

	private static final Session Null = new Session();

	public static Session read(Exhibition exhibition) {
		try {
			Session session = asSession(download(exhibition));
			session.showId = exhibition.id;
			session.cinemaId = exhibition.cinemaId;
			session.cinema = exhibition.cinema;
			session.city = exhibition.city;
			session.movie = exhibition.movie;
			session.time = exhibition.time;
			session.timeZone = exhibition.timeZone;
			return session;
		} catch (IOException e) {
			return Session.Null;
		}
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
}
