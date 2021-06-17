package com.cinepolis.cosmos;

import com.cinepolis.cosmos.parser.Field;

import java.io.*;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class Query {
	private final Form form;
	private final List<String> states;

	public Query(Form form) {
		this(form, emptyList());
	}

	public Query(Form form, String states) {
		this(form, listOf(states));
	}

	public Query(Form form, List<String> states) {
		this.form = form;
		this.states = states;
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
				.filter(f -> f.matches(states))
				.map(a -> a.asAttribute().value)
				.distinct()
				.collect(toList());
	}

	public String format(Map<String, String> variables) {
		if (form.isEmpty()) return "";
		return form.formatterOf(variables, states).format();
	}

}
