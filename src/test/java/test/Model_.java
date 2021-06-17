package test;

import com.cinepolis.cosmos.Model;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class Model_ {

	@Test
	public void name() {
		String[] names = new String[] {
				"projector",
				"projector.christie",
				"Null",
				"lamp",
				"Null",
				"Null",
				"Null",
				"Null"
		};
 		assertThat(goals().size()).isEqualTo(8);
		for (int i = 0; i < names.length; i++) {
			assertThat(goals().get(i).forms[0].name()).isEqualTo(v(names[i], "configuration"));
			assertThat(goals().get(i).forms[1].name()).isEqualTo(v(names[i], "status"));
		}

		for (int i = 0; i < 3; i++) {
			assertThat(goals().get(i).tags.size()).isEqualTo(3);
			assertThat(goals().get(i).tags.get(0)).isEqualTo("projector");
			assertThat(goals().get(i).tags.get(1)).isEqualTo("christie");
			assertThat(goals().get(i).tags.get(2)).isEqualTo("cp2220");
		}

		for (int i = 3; i < 7; i++) {
			assertThat(goals().get(i).tags.size()).isEqualTo(5);
			assertThat(goals().get(i).tags.get(0)).isEqualTo("lamp");
			assertThat(goals().get(i).tags.get(1)).isEqualTo("projector");
			assertThat(goals().get(i).tags.get(2)).isEqualTo("christie");
			assertThat(goals().get(i).tags.get(3)).isEqualTo("cp2220");
			assertThat(goals().get(i).tags.get(4)).isEqualTo("U");
		}

	}

	private String v(String name, String suffix) {
		return name.startsWith("Null") ? name : name+"-"+suffix;
	}

	public List<Model.Goal> goals() {
		return Model.of("CP2220").goals;
	}
}
