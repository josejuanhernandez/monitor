package com.cinepolis.cosmos;

import com.cinepolis.cosmos.parser.Field;
import com.cinepolis.cosmos.parser.Preprocessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.Files.readAllLines;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.*;

public class Form {
	public final static Form Null = new Form("Null", "");
	public final static Form Init = Form.of(Archetype.DeviceInitForm);
	public final static Form Unreachable = Form.of(Archetype.DeviceUnreachableForm);
	public final static Form Unknown = Form.of(Archetype.DeviceUnknownForm);
	private final String name;
	private final List<Field.Definition> definitions;
	private final List<Macro> macros;
	private final Map<Integer, Field> fields;

	public static Form of(File file) {
		return new Form(nameOf(file), contentOf(file));
	}

	public static String nameOf(File file) {
		return file.getName().replace(".form","");
	}

	public Form(String name, String content) {
		this(name, asList(content.split("\n")));
	}

	public String name() {
		return name;
	}

	public Form(String name, List<String> lines) {
		this.name = name;
		this.definitions = preprocess(lines).stream()
				.filter(this::isDefinition)
				.map(Field.Definition::new)
				.collect(toList());
		this.macros = lines.stream()
				.filter(this::isMacro)
				.map(this::macro)
				.collect(toList());
		this.fields = fieldsIn(definitions);
	}

	public List<Field.Definition> definitions() {
		return definitions;
	}

	public boolean isEmpty() {
		return definitions.isEmpty();
	}

	public Formatter formatterOf(Map<String, String> variables, List<String> states) {
		return new Formatter(variables, states);
	}

	public String executeMacrosOn(String content) {
		for (Macro macro : macros)
			content = macro.apply(content);
		return cleanEmptyLines(content);
	}

	private Macro macro(String line) {
		return new Macro(line);
	}

	private boolean isDefinition(String line) {
		return !isMacro(line);
	}

	private boolean isMacro(String line) {
		return line.contains("=>");
	}

	private static List<String> preprocess(List<String> content) {
		return new Preprocessor(content).execute();
	}

	private Map<Integer, Field> fieldsIn(List<Field.Definition> definitions) {
			return definitions.stream()
				.filter(Field.Definition::isField)
				.distinct()
				.collect(toMap(Field.Definition::hashCode, Field::of));
	}

	public Field fieldOf(Field.Definition definition) {
		return fields.get(definition.hashCode());
	}

	public Stream<Field> fields() {
		return fields.values().stream();
	}

	private static final Pattern emptyLines = Pattern.compile("\n+");
	private String cleanEmptyLines(String content) {
		content = emptyLines.matcher(content).replaceAll("\n").trim();
		return content.isEmpty() ? "" : content + "\n";
	}

	private static List<String> contentOf(File file) {
		try {
			return readAllLines(file.toPath());
		} catch (IOException e) {
			return emptyList();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	public String raw() {
		return definitions().stream()
				.map(Field.Definition::toString)
				.collect(joining("\n"));
	}

	public static class Macro implements Function<String, String> {
		public final Pattern pattern;
		public final String replace;

		public Macro(String line) {
			String[] split = line.split("=>");
			this.pattern = Pattern.compile(escape(split[0]));
			this.replace = split.length > 1 ? split[1] : "";
		}

		public static String escape(String pattern) {
			return (pattern + "\n")
					.replace("[", "\\[")
					.replace(".", "\\.")
					.replace("*", ".*")
					.replace(" | ", "\n")
					.replaceAll(" *: *", ":")
					.replaceAll(" +\n","\n");
		}

		@Override
		public String apply(String s) {
			return pattern.matcher(s).replaceAll(replace);
		}

		@Override
		public String toString() {
			return pattern.toString();
		}
	}

	public class Formatter  {
		private final Map<String, String> variables;
		private final List<String> states;
		private final List<StringBuilder> blocks = new ArrayList<>();
		private StringBuilder sb;

		public Formatter(Map<String, String> variables, List<String> states) {
			this.variables = variables;
			this.states = states;
		}

		public String format() {
			for (Field.Definition definition : definitions()) {
				if (definition.isHidden()) continue;
				if (definition.isConditional() && !definition.matches(states)) continue;
				append(format(definition));
			}
			String content = content();
			return Debug.rawMode ? content : executeMacrosOn(content);
		}

		private String content() {
			return blocks.stream()
					.filter(this::isContent)
					.map(this::write)
					.collect(joining());
		}

		private String write(StringBuilder sb) {
			return sb.indexOf("formula:") >= 0 ?
					Formula.map(sb.toString()) :
					sb.toString();
		}

		private boolean isContent(StringBuilder sb) {
			return Debug.rawMode || isFirstBlock(sb) || isValid(sb);
		}

		private boolean isValid(StringBuilder sb) {
			return sb.indexOf("noSuch") < 0 &&
					sb.indexOf("issue:Ok") < 0 &&
					sb.indexOf("issue:Normal") < 0 &&
					sb.indexOf("issue:Undefined") < 0 &&
					sb.indexOf("issue:NA#") < 0 &&
					sb.indexOf("value:NA") < 0 &&
					sb.indexOf("value:na") < 0 &&
					sb.indexOf("value:Unknown") < 0 &&
					sb.indexOf("value:unknown") < 0 &&
					sb.indexOf("value:\n") < 0;
		}

		private boolean isFirstBlock(StringBuilder sb) {
			return sb.indexOf("ts:") >= 0;
		}

		private void append(String text) {
			if (sb == null || text.startsWith("[")) blocks.add(sb = new StringBuilder());
			sb.append(text).append('\n');
		}

		private String format(Field.Definition definition) {
			return definition.isField() ? format(fieldOf(definition)) : definition.raw();
		}

		private String format(Field field) {
			return field.name + ":" + calculate(field);
		}

		private static final String Unknown = "noSuchObject";

		private String calculate(Field field) {
			if (field.isAttribute()) return field.format(variables.getOrDefault(field.asAttribute().value, Unknown));
			if (field.isConstant()) return field.format(null);
			return "";
		}

	}
}
