package test;

import com.cinepolis.cosmos.monitor.goals.crawler.Form;
import com.cinepolis.cosmos.monitor.goals.crawler.Query;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Zone_ {
	@Test
	public void with_comment() {
		Query query = query(form("//comment"), "");
		assertThat(query.format(map())).isEqualTo("");
	}

	public Query query(Form form, String state) {
		return new Query(form, state);
	}

	@Test
	public void with_empty_lines_at_eof() {
		Query query = query(form("variable:   a\n\n\n"), "");
		assertThat(query.format(map())).isEqualTo("variable:a\n");
	}

	@Test
	public void with_literal() {
		Form form = form("[status]");
		String format = query(form, "").format(map());
		assertThat(format).isEqualTo("[status]\n");
	}

	@Test
	public void with_conditional_fields() {
		Form form = form("[status]\nf:1.3.6.1.2:string\ng:condition? 1.3.6.1.3:string");

		Map<String, String> map = map(entry("1.3.6.1.2","aaa"),entry("1.3.6.1.3","bbb"));
		assertThat(query(form, "").keys().size()).isEqualTo(1);
		assertThat(query(form, "").format(map)).isEqualTo("[status]\nf:aaa\n");
		assertThat(query(form, "condition").keys().size()).isEqualTo(2);
		assertThat(query(form, "condition").format(map)).isEqualTo("[status]\nf:aaa\ng:bbb\n");
		assertThat(query(form, "condition").keys().size()).isEqualTo(2);
		assertThat(query(form, "condition").format(map)).isEqualTo("[status]\nf:aaa\ng:bbb\n");
		assertThat(query(form, "x").keys().size()).isEqualTo(1);
		assertThat(query(form, "x").format(map)).isEqualTo("[status]\nf:aaa\n");
	}

	public Form form(String s) {
		return new Form("", s);
	}

	@Test
	public void with_unknown_fields() {
		Form form = form("[status]\nf:1.3.6.1.2:string\ng:condition? 1.3.6.1.3:string");
		Map<String, String> map = map();
		assertThat(query(form, "condition").keys().size()).isEqualTo(2);
		assertThat(query(form, "condition").format(map)).isEqualTo("");
		assertThat(query(form, "").keys().size()).isEqualTo(1);
		assertThat(query(form, "").format(map)).isEqualTo("");
	}

	@Test
	public void with_repetitions() {
		Form form = form("[status]*3\na.#: #");
		String format = query(form, "").format(map());
		assertThat(format).isEqualTo("" +
				"[status]\na.1:1\n" +
				"[status]\na.2:2\n" +
				"[status]\na.3:3\n");
	}

	@Test
	public void with_range_repetitions() {
		Query query = query(form("[status]*4..5,8..11,15..15\na.#: #"), "");
		String format = query.format(map());
		assertThat(format).isEqualTo("" +
				"[status]\na.4:4\n" +
				"[status]\na.5:5\n" +
				"[status]\na.8:8\n" +
				"[status]\na.9:9\n" +
				"[status]\na.10:10\n" +
				"[status]\na.11:11\n" +
				"[status]\na.15:15\n");
	}

	@Test
	public void with_variable() {
		Query query = query(form("variable:1.3.6.1.0.0.0:string"), "");
		String format = query.format(map(entry("1.3.6.1.0.0.0","house")));
		assertThat(format).isEqualTo("variable:house\n");
	}
	@Test
	public void with_variables() {
		Query query = query(form("v1:1.3.6.1.1:string\n\nv2:1.3.6.1.2:string\n\n\n\n"), "");
		String format = query.format(map(entry("1.3.6.1.1","boy"),entry("1.3.6.1.2","girl")));
		assertThat(format).isEqualTo("v1:boy\nv2:girl\n");
	}

	@Test
	public void with_hidden_variable() {
		Query query = query(form("~variable:1.3.6.1.0.0.0:string"), "");
		String format = query.format(map(entry("1.3.6.1.0.0.0","house")));
		assertThat(format).isEqualTo("");
	}

	@Test
	public void of_file_with_patterns() {
		Form form = Form.of(new File("configuration/monitor/bases/device.model.form"));
		Query query = query(form, "");
		assertThat(query.format(map(entry("1.3.6.1.2.1.1.5.0","---")))).isEqualTo("[generic name]\nmodel:---\n");
	}

	private Map<String, String> map(String[]... entries) {
		Map<String, String> result = new HashMap<>();
		for (String[] entry :entries) result.put(entry[0], entry[1]);
		return result;
	}

	private String[] entry(String key, String value) {
		return new String[] {key,value};
	}

}
