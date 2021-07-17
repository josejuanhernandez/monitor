package com.cinepolis.cosmos.monitor.goals.scrapper;

import com.cinepolis.cosmos.monitor.Matches;
import com.cinepolis.cosmos.monitor.goals.scrapper.Exhibition;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalTime;
import java.util.*;

import static java.time.LocalTime.parse;
import static java.util.Comparator.comparingInt;
import static org.jsoup.Connection.Method.POST;

public class ScreenWriter {
	private final String segment;
	private final Gson gson;
	private final Map<String,String> cookies;
	private final Map<String, Integer> screens;

	public ScreenWriter(String segment)  {
		this.segment = segment;
		this.gson = new Gson();
		this.cookies = this.login();
		this.screens = screens();
	}

	public boolean isNotLogged() {
		return !cookies.containsKey("tms2_9000");
	}

	private Map<String, Integer> screens() {
		Map<String, Integer> result = new HashMap<>();
		if (isNotLogged()) return result;
		try {
			Cinema cinema = gson.fromJson(get("/core/device_infos/"), Cinema.class);
			for (Cinema.Data data : cinema)
				result.put(data.uuid, Integer.parseInt(data.identifier));
		} catch (IOException ignored) {
		}
		return result;
	}

	public Schedule schedule()  {
		Schedule result = new Schedule();
		if (isNotLogged()) return result;
		try {
			Schedules schedules = gson.fromJson(get("/core/scheduling/schedule"), Schedules.class);
			for (Schedules.Data data : schedules)
				result.add(new Session(data.displayName, parse(data.time), screens.get(data.screen)));
		} catch (IOException ignored) {
		}
		return result;
	}

	private Map<String, String> login() {
		try {
			return edit(loginForm())
					.submit()
					.timeout(30_000)
					.cookies(new HashMap<>())
					.method(POST)
					.execute()
					.cookies();
		} catch (IOException e) {
			return new HashMap<>();
		}
	}

	private Document loginForm() throws IOException {
		return Jsoup.connect(url())
				.method(Connection.Method.GET)
				.timeout(30_000)
				.execute()
				.parse();
	}

	@NotNull
	private FormElement edit(Document document) {
		FormElement form = (FormElement) document.select("form").first();
		Elements elements = form.select("input");
		elements.get(0).val("swmanager");
		elements.get(1).val("simtoqua");
		return form;
	}

	private String url() {
		return "http://" + segment;
	}

	private String get(String data) throws IOException {
		Connection.Response response;
		String screens = url() + data;
		response = Jsoup.connect(screens)
				.method(POST)
				.cookies(cookies)
				.timeout(30_000)
				.ignoreContentType(true)
				.execute();
		return response.body();
	}

	public static class Cinema implements Iterable<Cinema.Data> {
		public Screens data;

		@NotNull
		@Override
		public Iterator<Data> iterator() {
			return data.screens.values().iterator();
		}

		public static class Screens {
			public Map<String, Data> screens;

		}
		public static class Data {
			public String uuid;
			public String identifier;

		}
	}


	public static class Schedules implements Iterable<Schedules.Data> {
		public Map<String, Data> data;

		@NotNull
		@Override
		public Iterator<Data> iterator() {
			return data.values().iterator();
		}

		public static class Data {
			public String uuid;
			@SerializedName("display_name")
			public String displayName;
			@SerializedName("screen_uuid")
			public String screen;
			@SerializedName("start_time")
			public String time;
		}

	}

	public static class Schedule implements Iterable<Session> {
		private final List<Session> sessions = new ArrayList<>();
		private final Matches matches = new Matches();

		public void add(Session session) {
			sessions.add(session);
		}

		@Override @NotNull
		public Iterator<Session> iterator() {
			return sessions.iterator();
		}

		public void update(Exhibition exhibition) {
			sessions.stream()
					.filter(s -> exhibition.time.equals(s.time))
					.max(comparingInt(s -> matches(exhibition, s)))
					.ifPresent(value -> exhibition.screen(value.screen));

		}

		public int matches(Exhibition exhibition, Session session) {
			return matches.between(exhibition.movie, session.movie);
		}
	}

	public static class Session {
		public final String movie;
		public final LocalTime time;
		public final int screen;

		public Session(String movie, LocalTime time, int screen) {
			this.movie = movie;
			this.time = time;
			this.screen = screen;
		}

		@Override
		public String toString() {
			return "Session{" +
					"movie='" + movie + '\'' +
					", time=" + time +
					", screen=" + screen +
					'}';
		}
	}
}
