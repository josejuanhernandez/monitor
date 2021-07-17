package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

import com.cinepolis.cosmos.monitor.Inventory;
import com.cinepolis.cosmos.monitor.goals.scrapper.Exhibition;
import com.cinepolis.cosmos.monitor.inventory.Theater;

import java.io.IOException;
import java.time.LocalTime;

public class CostaRicaScrapper extends BetaScrapper {

	protected String uri() {
		return "https://www.cinepolis.co.cr/Cartelera.aspx/GetNowPlayingByCity";
	}

	@Override
	protected String tickets() {
		return "https://cr.cineticket-la.com/compra_cr/visSelectTickets.aspx?cinemacode=$vistaId&txtSessionId=$sessionId";
	}

	@Override
	protected String seats() {
		return "https://cr.cineticket-la.com/compra_cr/visSeatingControl.aspx";
	}


}
