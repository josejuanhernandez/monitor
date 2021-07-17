package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

public class ChileScrapper extends BetaScrapper {

	protected String uri() {
		return "https://cinehoyts.cl/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	protected String tickets() {
		return "https://inetvis.cinehoyts.cl/Compra/visSelectTickets.aspx?cinemacode=$vistaId&txtSessionId=$sessionId";
	}

	@Override
	protected String seats() {
		return "https://inetvis.cinehoyts.cl/Compra/visSeatingControl.aspx";
	}
}
