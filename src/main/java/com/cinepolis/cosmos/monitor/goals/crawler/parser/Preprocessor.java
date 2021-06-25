package com.cinepolis.cosmos.monitor.goals.crawler.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;

public class Preprocessor {
	private final StringBuilder result;
	private final List<String> lines;
	private StringBuilder block = new StringBuilder();
	private Iterable<Integer> iterable;

	public Preprocessor(List<String> content) {
		this.result = new StringBuilder();
		this.lines = content;
	}

	public List<String> execute() {
		this.lines.stream()
				.map(this::withoutComment)
				.filter(this::isCode)
				.forEach(this::execute);
		return toList(terminateBlock());
	}

	private String withoutComment(String line) {
		int i = line.indexOf("//");
		return i < 0 ? line.trim() : line.substring(0, i).trim();
	}

	private boolean isCode(String line) {
		return !(line.isEmpty() || line.contains("=>"));
	}

	private void execute(String line) {
		if (isBlockStartAt(line)) {
			initBlock(line.split("\\*"));
			return;
		}
		if (isBlockEndAt(line)) terminateBlock();
		target().append(line).append('\n');
	}

	private boolean isBlockEndAt(String line) {
		return line.startsWith("[") && isInsideBlock();
	}

	private StringBuilder target() {
		return isInsideBlock() ? block : result;
	}

	private StringBuilder terminateBlock() {
		if (iterable == null) return result;
		String block = this.block.toString();
		for (int i : iterable) result.append(block.replace("#", i + ""));
		this.block = new StringBuilder();
		this.iterable = null;
		return result;
	}

	private Iterable<Integer> parseIterable(String iterable) {
		List<Integer> list = new ArrayList<>();
		for (String segment : iterable.split(","))
			list.addAll(range(segment.split("\\.\\.")));
		return list;
	}

	private List<Integer> range(String... values) {
		int from = values.length > 1 ? parseInt(values[0]) : 1;
		int to = values.length > 1 ? parseInt(values[1]) : parseInt(values[0]);
		return IntStream.range(from, to + 1).boxed().collect(Collectors.toList());
	}

	private List<String> toList(StringBuilder sb) {
		return split(sb.toString().trim());
	}

	private void initBlock(String[] split) {
		if (isInsideBlock()) terminateBlock();
		block = new StringBuilder();
		block.append(split[0]).append("\n");
		iterable = parseIterable(split[1]);
	}

	private boolean isBlockStartAt(String line) {
		return line.startsWith("[") && line.contains("*");
	}

	private boolean isInsideBlock() {
		return block.length() != 0;
	}

	private static List<String> split(String content) {
		List<String> result = new ArrayList<>();
		while (true) {
			int index = content.indexOf('\n');
			if (index < 0) break;
			result.add(content.substring(0,index));
			content = content.substring(index+1);
		}
		result.add(content);
		return result;
	}
}
