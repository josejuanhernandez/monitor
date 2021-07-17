package test;

import com.cinepolis.cosmos.monitor.goals.crawler.Form;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Form_ {

	@Test
	public void of_comments() {
		Form form = new Form("", List.of("aa //comment", "//comment"));
		assertThat(form.definitions().size()).isEqualTo(1);
		assertThat(form.definitions().get(0).raw()).isEqualTo("aa");
	}

	@Test
	public void of_macros() {
		Form form = new Form("", List.of("[group] | variable: NA =>", "[group] | variable:XO =>"));
		String format = form.executeMacrosOn("[group]\nvariable:NA\n\n[group]\nvariable:XO\n\n[group]\nvariable:OK\n\n");
		assertThat(format).isEqualTo("[group]\nvariable:OK\n");
	}

	@Test
	public void of_macros_with_spaces() {
		Form form = new Form("", List.of("[status.alert] | name: NA#   | description:   | level: NA#  =>"));
		String clean = form.executeMacrosOn("" +
						"[status.alert]\n" +
						"name:NA#\n" +
						"description:\n" +
						"level:NA#\n" +
						"[status.alert]\n" +
						"name:NA#\n" +
						"description:\n" +
						"level:NA#\n"
				);
		assertThat(clean).isEqualTo("");
	}

	@Test
	public void of_macros_with_wild_cards() {
		Form form = new Form("", List.of("[configuration.attribute] | group: Serial Numbers | name: * | value:   =>    "));
		String beautify = form.executeMacrosOn("[configuration.attribute]\n" +
				"group:Serial Numbers\n" +
				"name:\n" +
				"value:\n");
		assertThat(beautify).isEqualTo("");
	}

	@Test
	public void of_macros_with_options() {
		Form form = new Form("", List.of("[configuration.attribute] | group: (A|B) | name: * | value:   =>    "));
		String beautify = form.executeMacrosOn("[configuration.attribute]\n" +
				"group:A\n" +
				"name:\n" +
				"value:\n");
		assertThat(beautify).isEqualTo("");
	}

}
