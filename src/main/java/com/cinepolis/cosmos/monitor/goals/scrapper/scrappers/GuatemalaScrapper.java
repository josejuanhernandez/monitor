package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

public class GuatemalaScrapper extends BetaScrapper {

	protected String uri() {
		return "https://www.cinepolis.com.gt/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	protected String tickets() {
		return "https://gt.cineticket-la.com/compra_gt/visSelectTickets.aspx?cinemacode=$vistaId&txtSessionId=$sessionId";
	}

	@Override
	protected String seats() {
		return "https://gt.cineticket-la.com/compra_gt/visSeatingControl.aspx";
	}
}
