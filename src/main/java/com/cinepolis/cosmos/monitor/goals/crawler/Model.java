package com.cinepolis.cosmos.monitor.goals.crawler;


import com.cinepolis.cosmos.monitor.Archetype;

import java.io.File;
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class Model {
	public final static Map<String, Form> Forms = loadForms();
	public final static Model Unreachable = new Model("device.unreachable");
	public final static Model Unknown = new Model("device.unknown");
	public final String name;
	public final int lamps;
	public final List<Book> books;
	public final Function<String, String> keyMapper;

	private Model(String name) {
		this(name, 0, s->s);
	}

	private Model(String name, int lamps) {
		this(name, lamps, s->s);
	}

	private Model(String name, int lamps, Function<String, String> keyMapper) {
		this.name = name;
		this.lamps = lamps;
		this.books = isProjector(name) ? booksOf(name, lamps) : booksOf(name);
		this.keyMapper = keyMapper;
	}

	@Override
	public String toString() {
		return name;
	}

	private static List<Book> booksOf(String name, int lamps) {
		List<Book> list = booksOf(name);
		for (char suffix : suffixesOf(lamps))
			list.addAll(booksOf("lamp" + "." + name + "." + suffix));
		return list;
	}

	private static List<Book> booksOf(String name) {
		List<String> tags = tagsOf(name);
		List<Book> list = new ArrayList<>();
		int index = name.indexOf('.');
		while (index > 0) {
			list.add(new Book(name.substring(0, index), tags));
			index = name.indexOf('.', index+1);
		}
		list.add(new Book(name, tags));
		return list;
	}

	private static List<String> tagsOf(String name) {
		return asList(name.split("\\."));
	}

	private static final String Codes = "ABCD";

	private static final char[] Unique = "U".toCharArray();
	private static char[] suffixesOf(int lamps) {
		return lamps == 1 ? Unique : Codes.substring(0, lamps).toCharArray();
	}

	private static boolean isProjector(String name) {
		return name.startsWith("projector.");
	}

	public static Model of(String code) {
		return models.getOrDefault(code, Unknown);
	}

	private static final Map<String, Model> models = new HashMap<>();

	static {
		models.put("Solaria One", new Model("projector.christie.solaria", 1, s -> s.replace("._.", "7")));
		models.put("CP2208", new Model("projector.christie.cp2208", 1, s->s.replace("._.",".7.")));
		models.put("CP2210", new Model("projector.christie.cp2210", 1, s->s.replace("._.", ".4.")));
		models.put("CP2215", new Model("projector.christie.cp2215", 1, s->s.replace("._.", ".4.")));
		models.put("CP2220", new Model("projector.christie.cp2220", 1, s -> s.replace("._.", ".2.")));
		models.put("2220", models.get("CP2220"));
		models.put("CP2230", new Model("projector.christie.cp2230", 1, s -> s.replace("._.", ".3.")));
		models.put("CP2230U", models.get("CP2230"));
		models.put("CP4220", new Model("projector.christie.cp4220", 1, s -> s.replace("._.", ".5.")));
		models.put("CP4230", new Model("projector.christie.cp4230", 1, s -> s.replace("._.", ".6.")));
		models.put("CP4260", new Model("projector.christie.cp4260", 1, s -> s.replace("._.", ".8.")));
		models.put("DP2K-10S", new Model("projector.barco.dp2k.10s", 1));
		models.put("DP2K-10SLP", new Model("projector.barco.dp2k.10s.lp", 1));
		models.put("DP2K-17BLP", new Model("projector.barco.dp2k.17b.lp", 1));
		models.put("DP2K-19B", new Model("projector.barco.dp2k.19b", 1));
		models.put("DP2K-20C", new Model("projector.barco.dp2k.20C", 1));
		models.put("DP2K-23B", new Model("projector.barco.dp2k.23b", 1));
		models.put("DP2K-23BLP", new Model("projector.barco.dp2k.23b.lp", 1));
		models.put("DP2K-32B", new Model("projector.barco.dp2k.32b", 1));
		models.put("DP2K-36BLP", new Model("projector.barco.dp2k.36b.lp", 1));
		models.put("DP2K-6E", new Model("projector.barco.dp2k.6e", 2));
		models.put("DP2K-8S", new Model("projector.barco.dp2k.8s", 1));
		models.put("DP2K-8SLP", new Model("projector.barco.dp2k.8s.lp", 1));
		models.put("DP4K-17BLP", new Model("projector.barco.dp4k.17b.lp", 1));
		models.put("DP4K-23B", new Model("projector.barco.dp4k.23b", 1));
		models.put("DP4K-32B", new Model("projector.barco.dp4k.32b", 1));
		models.put("DP4K-36BLP", new Model("projector.barco.dp4k.36b.lp", 1));
		models.put("SP4K-15C", new Model("projector.barco.sp4k-15c", 1));
		models.put("DCP2000", new Model("imb.doremi.dcp-2000"));
		models.put("IMS1000", new Model("imb.doremi.ims-1000"));
		models.put("IMS2000", new Model("imb.doremi.ims-2000"));
		models.put("IMS3000", new Model("imb.doremi.ims-3000"));
		models.put("ShowVault", new Model("imb.doremi.show-vault"));
		models.put("SR-1000", new Model("imb.gdc.sr-1000"));
		models.put("SX-3000", new Model("imb.gdc.sx-3000"));
		models.put("SX-4000", new Model("imb.gdc.sx-4000"));
		//put("iDRAC", new Model("server",.RAC"));
		//put("iDRAC6", new Model("server",.RAC6"));
		//put("iDRAC7", new Model("server",.RAC7"));
		//put("iDRAC8", new Model("server",.RAC8"));
	}

	private static Map<String, Form> loadForms() {
		Map<String, Form> map = new HashMap<>();
		for (File file : formFiles()) {
			Form form = Form.of(file);
			map.put(form.name(), form);
		}
		return map;
	}

	private static File[] formFiles() {
		return Archetype.Forms.listFiles(f->f.getName().endsWith(".form"));
	}

	public static void reload() {
		Forms.clear();
		Forms.putAll(Model.loadForms());
	}

	public enum BookType {
		Status, Configuration
	}

	public static class Book {
		public final Form[] forms;
		public final List<String> tags;

		public Book(String name, List<String> tags) {
			this.forms = get(name);
			this.tags = tags;
		}

		private Form[] get(String name) {
			return Arrays.stream(BookType.values())
					.map(t -> get(name, t)).toArray(Form[]::new);
		}

		private Form get(String name, BookType type) {
			return getKey(name + "-" + type.name());
		}

		private Form getKey(String key) {
			return Forms.getOrDefault(key, Form.Null);
		}

		public Form formOf(String name) {
			return forms[BookType.valueOf(name).ordinal()];
		}

		@Override
		public String toString() {
			return Arrays.toString(forms);
		}
	}

}
