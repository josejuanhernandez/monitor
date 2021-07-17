package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

public class PanamaScrapper extends BetaScrapper {

	protected String uri() {
		return "https://www.cinepolis.com.pa/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	protected String tickets() {
		return "https://pa.cineticket-la.com/compra/visSelectTickets.aspx?cinemacode=$vistaId&txtSessionId=$sessionId";
	}

	@Override
	protected String seats() {
		return "https://pa.cineticket-la.com/compra/visSeatingControl.aspx";
	}
}
