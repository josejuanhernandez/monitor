package com.cinepolis.cosmos.monitor;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;

public class Matches {
	private final Map<String, int[]> tokens = new HashMap<>();

	public int between(String a, String b) {
		return between(tokensOf(a), tokensOf(b));
	}

	private int between(int[] a, int[] b) {
		int result = 0;
		int i = 0;
		int j = 0;
		while (i < a.length && j < b.length) {
			if (a[i] < b[j]) i++;
			else if (a[i] > b[j]) j++;
			else if (a[i++] == b[j++]) result++;
		}
		return result;
	}

	private int[] tokensOf(String name) {
		int[] result = tokens.get(name);
		if (result == null) tokens.put(name, result = tokenize(name));
		return result;
	}

	private static int[] tokenize(String name) {
		return stream(split(name))
				.mapToInt(String::hashCode)
				.sorted()
				.toArray();
	}

	@NotNull
	private static String[] split(String name) {
		StringBuilder token = new StringBuilder();
		boolean wasAlpha = false;
		boolean wasNumber = false;
		for (char c : name.toUpperCase().toCharArray()) {
			boolean isAlpha = isAlpha(c);
			boolean isNumber = isNumber(c);
			if (token.length() > 0 && (isAlpha && !wasAlpha || isNumber && !wasNumber)) token.append(' ');
			if (isAlpha || isNumber) token.append(c);
			wasAlpha = isAlpha;
			wasNumber = isNumber;
		}
		return token.toString().split(" ");
	}

	public static void main(String[] args) {
		System.out.println(Arrays.toString(split("Fast &    Furious9")));
	}

	private static boolean isAlpha(char c) {
		return c >= 'A' && c <= 'Z' || c == 'Ñ';
	}
	private static boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

}
