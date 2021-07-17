package test;

import com.cinepolis.cosmos.monitor.goals.crawler.Model;
import com.cinepolis.cosmos.monitor.goals.crawler.Query;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class Model_ {

	@Test
	public void name() {
		assertThat(Model.of("CP2220").queries.size()).isEqualTo(7);
		assertThat(Model.of("DP2K-6E").queries.size()).isEqualTo(10);
	}

}
