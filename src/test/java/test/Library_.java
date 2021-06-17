package test;

import com.cinepolis.cosmos.Formula;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class Library_ {

	@Test
	public void bf10740() {
		List<String> input = List.of(
				"[status.action]\nformula:bf10740\nlabel:A Maintenance\nnext:23644395\n",
				"[status.action]\nformula:bf10740\nlabel:\nnext:\n",
				"[status.action]\nformula:bf10740\nlabel:A Maintenance\nnext:115596\n",
				"[status.action]\nformula:bf10740\nlabel:A Maintenance\nnext:-115596\n"
		);
		List<String> expected = List.of(
				"",
				"",
				"[status.alert]\nlabel:A Maintenance\nissue:1 day(s) left\n",
				"[status.alert]\nlabel:A Maintenance\nissue:-1 day(s) behind schedule\n"
		);
		for (int i = 0; i < input.size(); i++) {
			assertThat(Formula.map(input.get(i)))
					.isEqualTo(expected.get(i))
					.describedAs(input.get(i));
		}
	}
}
