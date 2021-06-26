package com.cinepolis.cosmos.monitor.goals.scrapper.session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;

import java.io.IOException;

import static org.jsoup.Connection.Method.POST;

class Show {
	public String description;
	@SerializedName("number_of_seats")
	public int seats;
	public int sold;
	public String screen;
	public int timeZone;

	public static final String uri = "https://sls-api-compra.cinepolis.com/api/seats";
	public static final String request = "{\"cinemaVistaId\":\":cinemaId:\", \"showtimeVistaId\":\":showTimeId:\", \"sessionId\":\":sessionId:\", 	\"selectedTickets\":[{\"ticket_type_code\": \":ticketCode:\",\"quantity\": 1,\"area_category_code\": \":areaCode:\",\"price\": :ticketPrice:,\"type\": \":areaCode:-:ticketName:-null\",\"folio\": null}],\"addTickets\": true}";

	public static Show read(Session session) {
		try {
			String json = download(request(session));
			Show show = asShow(json);
			show.screen = screenIn(show.description);
			show.timeZone = session.timeZone;
			show.sold = soldSeatsIn(json);
			return show;
		} catch (IOException e) {
			return new Show();
		}
	}

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
	private static final String Sold = "\"status\":\"Sold\"";
	private static int soldSeatsIn(String json) {
		return json.replace(Sold, Sold + "-").length() - json.length();
	}


	private static String request(Session session) {
		if (session.areas == null) return "";
		Area area = session.areas.get(0);
		Ticket ticket = area.tickets.get(0);
		return Show.request
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


}
