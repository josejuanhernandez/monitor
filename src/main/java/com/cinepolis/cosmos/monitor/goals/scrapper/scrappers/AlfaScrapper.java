package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

import com.cinepolis.cosmos.monitor.goals.scrapper.Exhibition;
import com.cinepolis.cosmos.monitor.goals.scrapper.Scrapper;
import com.cinepolis.cosmos.monitor.inventory.Division;
import com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.jsoup.Connection.Method.POST;

abstract class AlfaScrapper implements Scrapper {

	public Exhibition update(Exhibition exhibition) {
		return exhibition;
	}

	public List<Cinema> cinemasIn(Division division) {
		List<Cinema> cinemas = zone(division).cinemas;
		cinemas.forEach(c->c.set(division));
		return cinemas;
	}

	@Override
	public List<Exhibition> exhibitionsIn(Division division) {
		List<Exhibition> list = new ArrayList<>();
		double timeOffset = timeOffset();
		for (Cinema cinema : cinemasIn(division)) {
			if (cinema.schedules.size() == 0) continue;
			double offset = cinema.timeOffset + timeOffset;
			for (Movie movie : cinema.schedules.get(0))
				for (Format format : movie)
					for (ShowTime show : format)
						list.add(new Exhibition(show.id, movie.title, show.time(), offset, cinema.country(), cinema.theater()));
		}
		return list;
	}

	protected double timeOffset() {
		return timeOffset(Mexico) - timeOffset(ZoneId.systemDefault());
	}

	private static final ZoneId Mexico = ZoneId.of("America/Mexico_City");
	private static final double SecondPerHours = 60 * 60;
	protected static double timeOffset(ZoneId zone) {
		return zone.getRules().getOffset(Instant.now()).getTotalSeconds() / SecondPerHours;
	}


	public Zone zone(Division division) {
		try {
			return asZone(download(division.code));
		} catch (IOException e) {
			return Zone.Null;
		}
	}

	protected Zone asZone(String json) {
		Gson gson = gson();
		JsonObject object = gson.fromJson(json, JsonObject.class);
		return gson.fromJson(object.get("d"), Zone.class);
	}

	private static final int MBytes = 1024 * 1024;

	private String download(String code) throws IOException {
		return Jsoup.connect(uri())
				.maxBodySize(10 * MBytes)
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.ignoreContentType(true)
				.requestBody(json().replace(":zone:", code))
				.method(POST)
				.execute()
				.body();
	}

	private Gson gson() {
		return new GsonBuilder().setFieldNamingStrategy(f -> upper(f.getName())).create();
	}

	private String upper(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	protected abstract String uri();

	protected String json() {
		return "{\"claveCiudad\":\":zone:\",\"esVIP\":false}";
	}

}
