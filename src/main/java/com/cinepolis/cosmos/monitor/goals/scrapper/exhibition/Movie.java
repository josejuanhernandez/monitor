package com.cinepolis.cosmos.monitor.goals.scrapper.exhibition;

import java.util.Iterator;
import java.util.List;

class Movie implements Iterable<Format> {
	public int id;
	public String title;
	public String key;
	public String originalTitle;
	public String rating;
	public String ratingDescription;
	public String runTime;
	public String director;
	public String poster;
	public List<String> actors;
	public String gender;
	public int order;
	public List<Format> formats;

	@Override
	public Iterator<Format> iterator() {
		return formats.iterator();
	}
}
