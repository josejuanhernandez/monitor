package com.cinepolis.cosmos.monitor.goals.scrapper;

import com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model.Cinema;
import com.cinepolis.cosmos.monitor.inventory.Division;

import java.util.List;

import static java.util.Collections.emptyList;

public interface Scrapper {
	Scrapper Null = new NullScrapper();

	List<Cinema> cinemasIn(Division division);
	List<Exhibition> exhibitionsIn(Division division);
	Exhibition update(Exhibition exhibition);

	class NullScrapper implements Scrapper {
		@Override
		public List<Cinema> cinemasIn(Division division) {
			return emptyList();
		}

		@Override
		public List<Exhibition> exhibitionsIn(Division division) {
			return emptyList();
		}

		@Override
		public Exhibition update(Exhibition exhibition) {
			return exhibition;
		}
	}
}
