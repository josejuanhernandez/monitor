package com.cinepolis.cosmos.parser;

import com.cinepolis.cosmos.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.cinepolis.cosmos.Archetype.Forms;
import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

public abstract class Field {
	public final static Map<String, Map<String, String>> maps = new HashMap<>();

	public final String name;
	public final Definition definition;

	public static Field of(String definition) {
		return of(new Definition(definition));
	}

	public static Field of(Definition definition) {
		return definition.isAttribute() ? new Attribute(definition) : new Constant(definition);
	}

	public Field(Definition definition) {
		this.name = definition.name();
		this.definition = definition;
	}

	public boolean isConstant() {
		return this instanceof Constant;
	}

	public boolean isAttribute() {
		return this instanceof Attribute;
	}

	public boolean isConditional() {
		return definition.isConditional();
	}

	public abstract String format(Object data);

	public Attribute asAttribute() {
		return (Attribute) this;
	}

	public boolean matches(List<String> tags) {
		return definition.matches(tags);
	}

	public static class Constant extends Field {
		private final String value;

		public Constant(Definition definition) {
			super(definition);
			this.value = definition.value();
		}

		@Override
		public String format(Object data) {
			return value;
		}

		@Override
		public String toString() {
			return definition.name() + ": " + value;
		}
	}

	public static class Attribute extends Field {
		public final String value;
		private final Function<String, String> formatter;

		public Attribute(Definition definition) {
			super(definition);
			this.value = definition.value();
			this.formatter = formatterOf(definition.type());
		}

		private Function<String, String> asFunction(Map<String, String> map) {
			return v -> map.getOrDefault(v.trim(), "NA#" + v);
		}

		private Function<String, String> formatterOf(String type) {
			return isEnumerate(type) ? asFunction(mapOf(type)) : v -> v;
		}

		private boolean isEnumerate(String type) {
			return type.startsWith("enumerate");
		}

		private Map<String, String> mapOf(String type) {
			return type.endsWith(".map") ?
					mapOf(fileIn(type)) : mapOf(valuesIn(type));
		}

		private Map<String, String> mapOf(Stream<String> stream) {
			return stream.map(s -> s.split("=")).collect(toMap(s -> s[0], s -> s[1]));
		}

		private Map<String, String> mapOf(File file) {
			return maps.containsKey(file.getName()) ? maps.get(file.getName()) : loadMap(file);
		}

		private File fileIn(String type) {
			return new File(Forms, type.substring(type.indexOf("|") + 1));
		}

		private Stream<String> valuesIn(String type) {
			return stream(type.split("\\|")).skip(1);
		}

		private Map<String, String> loadMap(File file) {
			try {
				Map<String, String> map = loadMap(file.toPath());
				maps.put(file.getName(), map);
				return map;
			} catch (IOException e) {
				Logger.error(e.getMessage());
				return emptyMap();
			}
		}

		private Map<String, String> loadMap(Path path) throws IOException {
			return readAllLines(path).stream()
					.map(s -> s.split("="))
					.collect(toMap(s -> s[0], s -> s[1]));
		}

		@Override
		public String format(Object data) {
			return formatter.apply(data.toString());
		}

		@Override
		public String toString() {
			return definition.name() + ": " + value;
		}

	}

	public static class Definition {
		private final String definition;
		private final String[] conditions;

		public Definition(String definition) {
			this.definition = clean(definition);
			this.conditions = extractConditions();
		}

		private String[] extractConditions() {
			String condition = substring(":", "?");
			return condition.isEmpty() ? new String[0] : condition.split("\\.");
		}

		private String clean(String line) {
			return line.replaceAll(":\\s*",":")
					.replace("1.3.6.1","@1.3.6.1")
					.replace(":.@",":@")
					.trim();
		}

		public boolean isHidden() {
			return definition.length() > 0 && definition.charAt(0) == '~';
		}

		public boolean isField() {
			return definition.indexOf(':') > 0;
		}

		public boolean isConditional() {
			return conditions.length > 0;
		}

		public boolean isConstant() {
			return !isAttribute();
		}

		public boolean isAttribute() {
			return definition.contains("@");
		}

		public String name() {
			String name = substring("", ":");
			return name.charAt(0) == '~' ? name.substring(1) : name;
		}

		public String value() {
			String from = isConditional() ? ":?" : ":";
			return isAttribute() ? substring(from + "@", ":").trim()  : substring(from).trim();
		}


		public String type() {
			return type(substring("::"));
		}

		private static final String[][] aliases = {
				{"boolean", "enumerate|0=No|1=Yes|2=No"}
		};

		private static String type(String value) {
			return stream(aliases)
					.filter(s -> s[0].equals(value))
					.map(s -> s[1])
					.findFirst()
					.orElse(value);
		}

		private String substring(String from, String to) {
			Integer f = indexOf(0, from.toCharArray());
			Integer t = indexOf(f, to.toCharArray());
			return t != null ? definition.substring(f, t-1) : "";
		}

		private String substring(String from) {
			Integer f = indexOf(0, from.toCharArray());
			return f != null ? definition.substring(f) : "";
		}

		private Integer indexOf(Integer pos, char[] chars) {
			if (pos == null) return null;
			for (char c : chars) {
				int i = definition.indexOf(c, pos);
				if (i < 0) return null;
				pos = i + 1;
			}
			return pos;
		}

		public String raw() {
			return definition;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			return this == o || Objects.equals(definition, ((Definition) o).definition);
		}

		@Override
		public int hashCode() {
			return definition.hashCode();
		}

		public boolean matches(String tags) {
			return !isConditional() || matches(asList(tags.split("\\.")));
		}

		public boolean matches(List<String> tags) {
			return !isConditional() || stream(conditions).allMatch(c -> matches(tags, c));
		}

		private static boolean matches(List<String> tags, String condition) {
			return tags.stream().anyMatch(c -> c.equalsIgnoreCase(condition));
		}

		@Override
		public String toString() {
			return definition;
		}
	}
}
