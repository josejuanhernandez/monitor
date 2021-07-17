package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

public class ColombiaScrapper extends BetaScrapper {

	protected String uri() {
		return "https://www.cinepolis.com.co/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	protected String tickets() {
		return "https://co.cineticket-la.com/compra/visSelectTickets.aspx?cinemacode=$vistaId&txtSessionId=$sessionId";
	}

	@Override
	protected String seats() {
		return "https://co.cineticket-la.com/compra/visSeatingControl.aspx";
	}
}
