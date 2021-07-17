package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

import com.cinepolis.cosmos.monitor.goals.scrapper.Scrapper;

public class HondurasScrapper extends BetaScrapper {

	protected String uri() {
		return "https://www.cinepolis.com.hn/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	protected String tickets() {
		return "https://hn.cineticket-la.com/compra/visSelectTickets.aspx?cinemacode=$vistaId&txtSessionId=$sessionId";
	}

	@Override
	protected String seats() {
		return "https://hn.cineticket-la.com/compra/visSeatingControl.aspx";
	}

}
