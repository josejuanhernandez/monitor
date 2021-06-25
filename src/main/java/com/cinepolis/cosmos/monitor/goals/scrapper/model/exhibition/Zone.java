package com.cinepolis.cosmos.monitor.goals.scrapper.model.exhibition;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static org.jsoup.Connection.Method.POST;

public class Zone implements Iterable<Cinema> {
	private static final Zone Null = new Zone();

	private String name;
	@SerializedName("Cinemas")
	public List<Cinema> cinemas;

	@Override
	public Iterator<Cinema> iterator() {
		return cinemas.iterator();
	}

	private static final String uri = "https://cinepolis.com/Cartelera.aspx/GetNowPlayingByCity";
	private static final String body = "{\"claveCiudad\":\":zone:\",\"esVIP\":false}";
	public static Zone read(String name) {
		try {
			return asZone(clean(download(name)));
		} catch (IOException e) {
			return Zone.Null;
		}
	}

	protected static Zone asZone(String json) {
		Gson gson = gson();
		JsonObject object = gson.fromJson(json, JsonObject.class);
		return gson.fromJson(object.get("d"), Zone.class);
	}

	private static String clean(String json) {
		return json.replace("\\/","");
	}

	private static final int MBytes = 1024 * 1024;
	private static String download(String zone) throws IOException {
		return Jsoup.connect(uri)
				.maxBodySize(10 * MBytes)
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.ignoreContentType(true)
				.requestBody(body.replace(":zone:", zone))
				.method(POST)
				.execute()
				.body();
	}

	private static Gson gson() {
		return new GsonBuilder().setFieldNamingStrategy(f -> upper(f.getName())).create();
	}

	private static String upper(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}


}
