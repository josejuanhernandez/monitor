package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers;

public class SpainScrapper extends AlfaScrapper {

	protected String uri() {
		return "https://yelmocines.es/now-playing.aspx/GetNowPlaying";
	}

	@Override
	protected String json() {
		return "{\"cityKey\":\":zone:\"}";
	}
}
