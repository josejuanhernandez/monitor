package com.cinepolis.cosmos.monitor.goals.crawler;


import com.cinepolis.cosmos.Archetype;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class Model {
	public final static Map<String, Form> Forms = loadForms();
	public final static Model Unreachable = new Model("device.unreachable");
	public final static Model Unknown = new Model("device.unknown");
	public final String name;
	public final List<Query> queries;
	public final Function<String, String> oidMapper;

	private Model(String name) {
		this(name,s->s);
	}

	private Model(String name, Function<String, String> oidMapper) {
		this.name = name;
		this.queries = queriesOf(name);
		this.oidMapper = oidMapper;
	}

	@Override
	public String toString() {
		return name;
	}

		private final static Set<String> DoubleLampModels = Set.of("projector.barco.dp2k.6e");
	private static List<Query> queriesOf(String name) {
		List<Query> list = new ArrayList<>();
		List<String> tags = tagsOf(name);
		list.addAll(queriesOf(tags, "configuration"));
		list.addAll(queriesOf(tags, "status"));
		list.addAll(projectorQueriesOf(name));
		return list;
	}

	@NotNull
	private static List<Query> projectorQueriesOf(String name) {
		if (!isProjector(name)) return Collections.emptyList();

		List<Query> queries = new ArrayList<>();
		if (DoubleLampModels.contains(name)) {
			queries.addAll(queriesOf(lampOf(name, 'A')));
			queries.addAll(queriesOf(lampOf(name, 'B')));
		}
		else {
			queries.addAll(queriesOf(lampOf(name, 'U')));
		}
		return queries;
	}

	@NotNull
	private static String lampOf(String name, char suffix) {
		return name.replace("projector.", "lamp.") + '.' + suffix;
	}

	private static List<String> tagsOf(String name) {
		return asList(name.split("\\."));
	}

	private static List<Query> queriesOf(List<String> tags, String view) {
		List<Query> list = new ArrayList<>();
		StringBuilder name = new StringBuilder();
		for (String tag : tags) {
			name.append(tag);
			Form form = Forms.get(view + "." + name);
			if (form == null) continue;
			list.add(new Query(form, tags));
			name.append('.');
		}
		return list;
	}

	private static boolean isProjector(String name) {
		return name.startsWith("projector.");
	}

	public static Model of(String code) {
		return models.getOrDefault(code, Unknown);
	}

	private static final Map<String, Model> models = new HashMap<>();
	static {
		models.put("Solaria One", new Model("projector.christie.solaria", s -> s.replace("._.", "7")));
		models.put("CP2208", new Model("projector.christie.cp2208", s->s.replace("._.",".7.")));
		models.put("CP2210", new Model("projector.christie.cp2210", s->s.replace("._.", ".4.")));
		models.put("CP2215", new Model("projector.christie.cp2215", s->s.replace("._.", ".4.")));
		models.put("CP2220", new Model("projector.christie.cp2220", s -> s.replace("._.", ".2.")));
		models.put("2220", models.get("CP2220"));
		models.put("CP2230", new Model("projector.christie.cp2230", s -> s.replace("._.", ".3.")));
		models.put("CP2230U", models.get("CP2230"));
		models.put("CP4220", new Model("projector.christie.cp4220", s -> s.replace("._.", ".5.")));
		models.put("CP4230", new Model("projector.christie.cp4230", s -> s.replace("._.", ".6.")));
		models.put("CP4260", new Model("projector.christie.cp4260", s -> s.replace("._.", ".8.")));
		models.put("DP2K-10S", new Model("projector.barco.dp2k.10s"));
		models.put("DP2K-10SLP", new Model("projector.barco.dp2k.10s.lp"));
		models.put("DP2K-17BLP", new Model("projector.barco.dp2k.17b.lp"));
		models.put("DP2K-19B", new Model("projector.barco.dp2k.19b"));
		models.put("DP2K-20C", new Model("projector.barco.dp2k.20C"));
		models.put("DP2K-23B", new Model("projector.barco.dp2k.23b"));
		models.put("DP2K-23BLP", new Model("projector.barco.dp2k.23b.lp"));
		models.put("DP2K-32B", new Model("projector.barco.dp2k.32b"));
		models.put("DP2K-36BLP", new Model("projector.barco.dp2k.36b.lp"));
		models.put("DP2K-6E", new Model("projector.barco.dp2k.6e"));
		models.put("DP2K-8S", new Model("projector.barco.dp2k.8s"));
		models.put("DP2K-8SLP", new Model("projector.barco.dp2k.8s.lp"));
		models.put("DP4K-17BLP", new Model("projector.barco.dp4k.17b.lp"));
		models.put("DP4K-23B", new Model("projector.barco.dp4k.23b"));
		models.put("DP4K-32B", new Model("projector.barco.dp4k.32b"));
		models.put("DP4K-36BLP", new Model("projector.barco.dp4k.36b.lp"));
		models.put("SP4K-15C", new Model("projector.barco.sp4k-15c"));
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


}
