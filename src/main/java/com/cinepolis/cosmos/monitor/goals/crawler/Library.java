package com.cinepolis.cosmos.monitor.goals.crawler;

import java.util.HashMap;
import java.util.Map;

public class Library {
	static Map<String, Formula> formulas = new HashMap<>();

	static {
		formulas.put("bf10740", Library::bf10740);
		formulas.put("b03013e2", Library::b03013e2);
	}

	private static String bf10740(Map<String, String> v) {
		long days = Long.parseLong(v.get("next")) / (60 * 60 * 24);
		if (days > 14) return "";
		return "[status.alert]" + '\n' +
				"label:" + v.get("label") + '\n' +
				"issue:" + days + " day(s) " + (days < 0 ? "behind schedule" : "left") + '\n';
	}

	private static String b03013e2(Map<String, String> v) {
		long value = Long.parseLong(v.get("value"));
		long max = Long.parseLong(v.get("max"));
		if (value < max) return "";
		return "[status.alert]\n" +
				"label: Usage time\n" +
				"issue: Expired\n";
	}

}
