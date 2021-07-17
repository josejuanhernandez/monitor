package com.cinepolis.cosmos.monitor.goals.scrapper.scrappers.model;



import java.util.Iterator;
import java.util.List;

public class Movie implements Iterable<Format> {
	public int id;
	public String title;
	public String key;
	public String originalTitle;
	public String rating;
	public String ratingDescription;
	public String runTime;
	public String director;
	public String poster;
	public String gender;
	public int order;
	public List<Format> formats;

	@Override
	public Iterator<Format> iterator() {
		return formats.iterator();
	}
}
