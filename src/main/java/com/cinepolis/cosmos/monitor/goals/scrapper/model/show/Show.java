package com.cinepolis.cosmos.monitor.goals.scrapper.model.show;

import com.cinepolis.cosmos.monitor.goals.scrapper.model.session.Area;
import com.cinepolis.cosmos.monitor.goals.scrapper.model.session.Session;
import com.cinepolis.cosmos.monitor.goals.scrapper.model.session.Ticket;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.cinepolis.cosmos.monitor.goals.crawler.Archetype.Network;
import static java.nio.file.Files.readAllLines;
import static java.util.stream.Collectors.toMap;
import static org.jsoup.Connection.Method.POST;

public class Show {
	public String showId;
	public String theaterId;
	public String theater;
	public String city;
	public String region;
	public String country = "Mexico";
	public String movie;
	public LocalTime time;
	public String description;
	@SerializedName("number_of_seats")
	public int seats;
	public int sold;
	public String screen;
	public int timeZone;
	public String projector;
	public String server;

	public static final String body = "{\"cinemaVistaId\":\":cinemaId:\", \"showtimeVistaId\":\":showTimeId:\", \"sessionId\":\":sessionId:\", 	\"selectedTickets\":[{\"ticket_type_code\": \":ticketCode:\",\"quantity\": 1,\"area_category_code\": \":areaCode:\",\"price\": :ticketPrice:,\"type\": \":areaCode:-:ticketName:-null\",\"folio\": null}],\"addTickets\": true}";
	public static final String uri = "https://sls-api-compra.cinepolis.com/api/seats";

	public static Show read(Session session) {
		try {
			String json = download(request(session));
			Show show = asShow(json);
			show.showId = session.showId;
			show.theaterId = "1020" + session.cinemaId;
			show.theater = session.cinema;
			show.screen = screenIn(show.description);
			show.movie = session.movie;
			show.time = session.time;
			show.timeZone = session.timeZone;
			show.sold = soldSeatsIn(json);
			return show.withoutScreen() ? show : complete(show);
		} catch (IOException e) {
			return new Show();
		}
	}

	private static Show complete(Show show) {
		String[] segment = Segments.get(show.theaterId);
		show.theater = segment[5];
		show.city = segment[6];
		show.region = segment[7];
		show.country = segment[8];
		show.projector = segment[1] + show.screenPlus(20);
		show.server = segment[1] + show.screenPlus(40);
		return show;
	}

	private boolean withoutScreen() {
		return screen.isEmpty() || screen.equals("NA");
	}

	private int screenPlus(int offset) {
		return offset + Integer.parseInt(screen);
	}

	private static final String target = "\"status\":\"Sold\"";

	private static Show asShow(String json) {
		if (json == null) return new Show();
		try {
			Gson gson = gson();
			JsonObject object = gson.fromJson(json, JsonObject.class);
			Layout layout = gson.fromJson(object.get("seat_layout_data"), Layout.class);
			return layout.areas.get(0);
		}
		catch (Exception e) {
			return new Show();
		}
	}
	private static int soldSeatsIn(String json) {
		return json.replace(target, target + "-").length() - json.length();
	}

	private static String screenIn(String description) {
		try {
			return description.toUpperCase()
					.replace("SALA","")
					.replace("NUME","")
					.replace("NUM-","")
					.replace("NUM","")
					.replace("XE","")
					.replace("F","")
					.replace("VIP", "")
					.trim();
		}
		catch (Exception e) {
			return "NA";
		}
	}

	private static String request(Session session) {
		if (session.areas == null) return "";
		Area area = session.areas.get(0);
		Ticket ticket = area.tickets.get(0);
		return Show.body
				.replace(":cinemaId:", session.cinemaId)
				.replace(":showTimeId:", session.showId)
				.replace(":sessionId:", session.id)
				.replace(":areaCode:", area.code)
				.replace(":ticketName:", ticket.description)
				.replace(":ticketCode:", ticket.code)
				.replace(":ticketPrice:", String.valueOf(ticket.price));
	}

	private static String download(String body) throws IOException {
		if (body.isEmpty()) return "";
		return Jsoup.connect(uri)
				.header("Content-Type", "application/json")
				.header("Accept", "application/json")
				.ignoreContentType(true)
				.requestBody(body)
				.method(POST)
				.execute()
				.body();
	}

	private static Gson gson() {
		return new Gson();
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


	public String theater() {
		return theaterId + "-" + theater;
	}

	@Override
	public String toString() {
		return theaterId + '\t' + screen + '\t' +
				theater + '\t' + city + '\t' + region + '\t' + country + '\t' +
				movie + '\t' + time + '\t' + timeZone + '\t' +
				seats + '\t' + sold + '\t' +
				projector + '\t' + server;
	}
}
