package test;

import com.cinepolis.cosmos.monitor.goals.scrapper.Exhibition;
import org.junit.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class Exhibition_ {
	@Test
	public void isActive() {
		assertThat(exhitibitionAt(0).isActiveAt(minute(9, 15))).isTrue();
		assertThat(exhitibitionAt(0).isActiveAt(minute(9, 40))).isTrue();
		assertThat(exhitibitionAt(0).isActiveAt(minute(9, 5))).isFalse();
		assertThat(exhitibitionAt(0).isActiveAt(minute(9, 55))).isFalse();
		assertThat(exhitibitionAt(1).isActiveAt(minute(10, 25))).isTrue();
		assertThat(exhitibitionAt(1).isActiveAt(minute(10, 0))).isFalse();
		assertThat(exhitibitionAt(-1).isActiveAt(minute(9, 15))).isFalse();
		assertThat(exhitibitionAt(-1).isActiveAt(minute(8, 15))).isTrue();
		assertThat(exhitibitionAt(-8).isActiveAt(minute(1, 35))).isTrue();
		assertThat(exhitibitionAt(-9).isActiveAt(minute(0, 35))).isTrue();
		assertThat(exhitibitionAt(-10).isActiveAt(minute(23, 35))).isTrue();
		assertThat(exhitibitionAt(-10).isActiveAt(minute(23, 35))).isTrue();
	}

	private Exhibition exhitibitionAt(int timeOffset) {
		return new Exhibition("id", "movie", time(), timeOffset, "country", null);
	}

	private int minute(int hour, int minute) {
		return LocalTime.of(hour, minute).toSecondOfDay() / 60;
	}

	private LocalTime time() {
		return LocalTime.of(9,30);
	}
}
