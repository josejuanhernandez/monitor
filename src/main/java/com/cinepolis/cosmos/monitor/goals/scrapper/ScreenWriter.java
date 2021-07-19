package com.cinepolis.cosmos.monitor.goals.scrapper;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static java.time.LocalTime.parse;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.stream.Collectors.toList;
import static org.jsoup.Connection.Method.POST;

public class ScreenWriter {
	private final String segment;
	private final Gson gson;
	private final Map<String,String> cookies;
	private final Map<String, Integer> screens;
	private final static int TIMEOUT = 10_000;

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
			String today = LocalDate.now().format(ISO_LOCAL_DATE);
			for (Schedules.Data data : schedules) {
				if (!data.date.equals(today)) continue;
				result.add(new Session(data.pointOfSale.title, parse(data.time), screens.get(data.screen), data.pointOfSale.seats));
			}
		} catch (IOException ignored) {
		}
		return result;
	}

	private Map<String, String> login() {
		try {
			return edit(loginForm())
					.submit()
					.timeout(TIMEOUT)
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
				.timeout(TIMEOUT)
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

	private String get(String path, String... data) throws IOException {
		Response response = Jsoup.connect(url() + path)
				.method(POST)
				.cookies(cookies)
				.timeout(TIMEOUT)
				.ignoreContentType(true)
				.data(data)
				.execute();
		return response.body();
	}

	public static class Cinema implements Iterable<Cinema.Data> {
		public Screens data;

		@Override @NotNull
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
			@SerializedName("display_name")
			public String displayName;
			@SerializedName("screen_uuid")
			public String screen;
			@SerializedName("start_date")
			public String date;
			@SerializedName("start_time")
			public String time;
			@SerializedName("pos_information")
			public PointOfSale pointOfSale;
		}

		public static class PointOfSale {
			@SerializedName("feature_title")
			public String title;
			@SerializedName("seats_available")
			public int seats;
		}

	}

	public static class Schedule implements Iterable<Session> {
		private final List<Session> sessions = new ArrayList<>();

		public void add(Session session) {
			sessions.add(session);
		}

		@Override @NotNull
		public Iterator<Session> iterator() {
			return sessions.iterator();
		}

		public void update(Exhibition exhibition) {
			if (size() == 0) return;
			Classifier classifier = new Classifier(exhibition);
			String alias = classifier.aliasIn(sessionsAt(exhibition.time));
			Session session = find(exhibition.time, alias);
			exhibition.screen(session.screen).seats(session.seats);
			sessions.remove(session);
		}

		@NotNull
		private List<String> sessionsAt(LocalTime time) {
			return this.sessions.stream()
					.filter(s -> matches(time, s.time))
					.map(s->s.movie)
					.distinct()
					.collect(toList());
		}

		private boolean matches(LocalTime exhibition, LocalTime show) {
			int minutes = (exhibition.toSecondOfDay() - show.toSecondOfDay()) / 60;
			return -20 <= minutes && minutes <= 30;
		}

		@NotNull
		private Session find(LocalTime time, String alias) {
			return this.sessions.stream()
					.filter(s -> matches(time, s.time))
					.filter(s -> s.movie.equals(alias))
					.findFirst()
					.orElse(Session.Null);
		}

		public int size() {
			return sessions.size();
		}
	}

	public static class Session {
		public static final Session Null = new Session("", null, 0, 0);

		public final String movie;
		public final LocalTime time;
		public final int screen;
		public final int seats;

		public Session(String movie, LocalTime time, int screen, int seats) {
			this.movie = movie;
			this.time = time;
			this.screen = screen;
			this.seats = seats;
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
