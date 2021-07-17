package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

import com.cinepolis.cosmos.monitor.goals.scrapper.Exhibition;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.FormElement;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

abstract class BetaScrapper extends AlfaScrapper {
	@Override
	public Exhibition update(Exhibition exhibition) {
		return new Browser(exhibition).update().exhibition;
	}

	protected abstract String tickets();

	protected abstract String seats();

	class Browser {
		private final Exhibition exhibition;
		private Map<String, String> cookies = new HashMap<>();

		public Browser(Exhibition exhibition) {
			this.exhibition = exhibition;
		}

		public Browser update() {
			try {
				exhibition.screen(screen());
				Session session = Session.from(jsonIn(selectSeats()));
				exhibition.seats(session.seats());
				exhibition.sold(session.sold());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return this;
		}

		private int screen() throws IOException {
			return screen(exhibition.theater.id, screenIn(selectTickets()));
		}

		private int screen(String id, String screenIn) {
			return 0;
		}

		private String jsonIn(Document document) {
			String html = document.html();
			int from = html.indexOf("m_theatre = ");
			int to = html.indexOf(";",from);
			return html.substring(from+12,to);
		}


		private String screenIn(Document document) {
			return
					document.select("span#visOrderSummary_rptShowtimeList__ctl0_lblScreen").html() +
					document.select("span#visOrderSummary_rptShowtimeList_ctl00_lblScreen").html() +
					document.select("span#visOrderTracker_txtScreenDetails").html();
		}

		private Document selectTickets() throws IOException {
			String url = tickets().replace("$vistaId", exhibition.theater.vistaId).replace("$sessionId", exhibition.showId);
			Document document = get(url).parse();
			return post(formIn(document));
		}

		private FormElement formIn(Document document) {
			return (FormElement) document.select("form").first();
		}

		private Document post(FormElement form) throws IOException {
			select(form.select("select.TicketTypeDropDown"));
			return form.submit().timeout(80_000).cookies(cookies).post();
		}

		private Connection.Response get(String url) throws IOException {
			Connection.Response response = Jsoup.connect(url)
					.method(Connection.Method.GET)
					.timeout(30_000)
					.cookies(cookies)
					.execute();
			this.cookies = response.cookies();
			return response;
		}

		private Document selectSeats() throws IOException {
			return get(seats()).parse();
		}

		private void select(Elements elements) {
			elements.forEach(this::select);
		}

		private void select(Element select) {
			Elements options = select.getElementsByTag("option");
			for (Element option : options) {
				if (option.attr("value").equals("1"))
					option.attr("selected", "selected");
				else
					option.removeAttr("selected");
			}
		}
	}



	static class Session implements Iterable<Area> {

		List<Area> Areas;
		public static Session from(String json) {
			return gson().fromJson(json, Session.class);
		}

		private static Gson gson() {
			return new GsonBuilder()
					.serializeNulls()
					.setFieldNamingStrategy(FieldNamingPolicy.UPPER_CAMEL_CASE)
					.create();
		}

		@Override
		public Iterator<Area> iterator() {
			return Areas.iterator();
		}

		public int seats() {
			return Areas.stream().mapToInt(Area::seats).sum();
		}

		public int sold() {
			return Areas.stream().mapToInt(Area::sold).sum();
		}


	}
	static class Area implements Iterable<Row> {

		List<Row> rows;
		@Override
		public Iterator<Row> iterator() {
			return rows.iterator();
		}

		public int seats() {
			return rows.stream().mapToInt(Row::seats).sum();
		}

		public int sold() {
			return rows.stream().mapToInt(Row::sold).sum();
		}


	}
	static class Row implements Iterable<Seat> {

		List<Seat> seats;
		@Override
		public Iterator<Seat> iterator() {
			return seats.iterator();
		}

		public int seats() {
			return (int) seats.stream()
					.filter(Objects::nonNull)
					.count();
		}

		public int sold() {
			return (int) seats.stream()
					.filter(Objects::nonNull)
					.filter(s->s.status == Seat.SOLD)
					.count();
		}

	}
	static class Seat {
		public static final int EMPTY = 0;
		public static final int SOLD = 1;
		public static final int SPECIAL = 3;
		public static final int SELECTED = 4;

		public int rowIndex;
		public int columnIndex;
		public int priority;
		public String id;
		public int status;
	}




}
