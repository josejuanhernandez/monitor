package com.cinepolis.cosmos;

import java.util.HashMap;
import java.util.Map;

@FunctionalInterface
public interface Formula {
	static String map(String s) {
		try {
			return map(variablesIn(s));
		}
		catch (Exception e) {
			return "";
		}
	}

	private static String map(Map<String, String> variables) {
		return Library.formulas.get(variables.get("formula")).calculate(variables);
	}

	static Map<String, String> variablesIn(String s) {
		return variablesIn(s.split("\n"));
	}

	private static Map<String, String> variablesIn(String[] lines) {
		Map<String, String> result = new HashMap<>();
		for (String line : lines) {
			int index = line.indexOf(':');
			if (index < 0) continue;
			result.put(line.substring(0,index),line.substring(index+1));
		}
		return result;
	}

	String calculate(Map<String, String> variables);


}
