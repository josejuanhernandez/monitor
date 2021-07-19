package com.cinepolis.cosmos.monitor.goals.scrapper;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.joining;

public class Classifier {
	private final static Comparator Comparator = new Comparator();
	private final static Map<String, Set<String>> aliases = new HashMap<>();
	private final static Map<String, int[]> tokens = new HashMap<>();

	private final Exhibition exhibition;

	public Classifier(Exhibition exhibition) {
		this.exhibition = exhibition;
	}

	private int commonTokens(String alias) {
		return Comparator.compare(exhibition.movie, alias);
	}

	public boolean hasCommonTokens(String s) {
		return commonTokens(s) > 0;
	}

	private static Set<String> aliasesOf(String name) {
		if (!aliases.containsKey(name)) aliases.put(name, new HashSet<>());
		return aliases.get(name);
	}

	public interface Selector {
		String select();
	}

	public String aliasIn(List<String> aliases) {
		if (aliases.size() == 0) return null;
		for (Selector selector : selectorsOf(aliases)) {
			String alias = selector.select();
			if (alias == null) continue;
			aliasesOf(exhibition.movie).add(alias);
			return alias;
		}
		return null;
	}

	@NotNull
	private List<Selector> selectorsOf(List<String> aliases) {
		return List.of(
				() -> wrap(aliases.stream()),
				() -> wrap(aliases.stream().filter(s -> aliasesOf(exhibition.movie).contains(s))),
				() -> aliases.stream().filter(this::hasCommonTokens).max(comparingInt(this::commonTokens)).orElse(null)
		);
	}

	private String wrap(Stream<String> stream) {
		String alias = stream.collect(joining("\n"));
		return alias.isEmpty() || alias.contains("\n") ? null : alias;
	}

	public static class Comparator {
		public int compare(String a, String b) {
			return compare(tokensOf(a), tokensOf(b));
		}

		private int compare(int[] a, int[] b) {
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

	private static boolean isAlpha(char c) {
		return c >= 'A' && c <= 'Z' || c == 'Ñ';
	}
	private static boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

	private static int[] tokensOf(String name) {
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

}
