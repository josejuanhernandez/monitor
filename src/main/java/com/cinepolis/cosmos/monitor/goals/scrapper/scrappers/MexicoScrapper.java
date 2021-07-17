package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

import com.cinepolis.cosmos.monitor.goals.scrapper.Exhibition;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.List;

import static org.jsoup.Connection.Method.POST;

public class MexicoScrapper extends AlfaScrapper {

	protected String uri() {
		return "https://cinepolis.com/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	public Exhibition update(Exhibition exhibition) {
		Show show = Show.read(Session.read(exhibition));
		return exhibition.seats(show.seats).sold(show.sold);
	}

	public static class Session {
		@SerializedName("session_id")
		public String id;
		public String showId;
		public String cinemaId;
		public List<Area> areas;

		private static final Session Null = new Session();
		public static Session read(Exhibition exhibition) {
			try {
				Session session = asSession(download(exhibition));
				session.showId = exhibition.showId;
				session.cinemaId = exhibition.theater.vistaId;
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
					.requestBody(request.replace(":cinemaId:", ""+exhibition.theater.vistaId).replace(":showId:", exhibition.showId))
					.method(POST)
					.execute()
					.body();
		}
	}

	public static class Show {
		public String description;
		@SerializedName("number_of_seats")
		public int seats;
		public int sold;

		public static final String uri = "https://sls-api-compra.cinepolis.com/api/seats";
		public static final String request = "{\"cinemaVistaId\":\":cinemaId:\", \"showtimeVistaId\":\":showTimeId:\", \"sessionId\":\":sessionId:\", 	\"selectedTickets\":[{\"ticket_type_code\": \":ticketCode:\",\"quantity\": 1,\"area_category_code\": \":areaCode:\",\"price\": :ticketPrice:,\"type\": \":areaCode:-:ticketName:-null\",\"folio\": null}],\"addTickets\": true}";

		public static Show read(Session session) {
			try {
				String json = download(request(session));
				Show show = asShow(json);
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

	static class Layout {
		public List<Show> areas;

	}

	static class Ticket {
		public String code;
		public String description;
		public int price;
	}

	public static class Area {
		public String code;
		public String name;
		public List<Ticket> tickets;
	}
}
