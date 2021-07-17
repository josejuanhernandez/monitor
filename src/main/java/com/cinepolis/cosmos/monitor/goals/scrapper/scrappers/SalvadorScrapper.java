package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

public class SalvadorScrapper extends BetaScrapper {

	protected String uri() {
		return "https://www.cinepolis.com.sv/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	protected String tickets() {
		return "https://sv.cineticket-la.com/compra/visSelectTickets.aspx?cinemacode=$vistaId&txtSessionId=$sessionId";
	}

	@Override
	protected String seats() {
		return "https://sv.cineticket-la.com/compra/visSeatingControl.aspx";
	}

}
