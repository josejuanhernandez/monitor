package com.cinepolis.cosmos.monitor.goals.crawler;

import com.cinepolis.cosmos.monitor.goals.crawler.parser.Field;

import java.io.*;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Query {
	private final Form form;
	private final List<String> tags;

	public Query(Form form) {
		this(form, emptyList());
	}

	public Query(Form form, String tags) {
		this(form, listOf(tags));
	}

	public Query(Form form, List<String> tags) {
		this.form = form;
		this.tags = tags;
	}

	public static OutputStream create(File base) throws FileNotFoundException {
		return new BufferedOutputStream(new FileOutputStream(base));
	}

	private static List<String> listOf(String tags) {
		return asList(tags.split("\\."));
	}

	public List<String> keys() {
		return form.fields()
				.filter(Field::isAttribute)
				.filter(f -> f.matches(tags))
				.map(a -> a.asAttribute().value)
				.distinct()
				.collect(toList());
	}

	public String format(Map<String, String> variables) {
		if (form.isEmpty()) return "";
		return form.formatterOf(variables, tags).format();
	}

	public boolean is(String view) {
		return form.name().startsWith(view);
	}

	@Override
	public String toString() {
		return form.name() + ": " + String.join(",", tags);
	}
}
