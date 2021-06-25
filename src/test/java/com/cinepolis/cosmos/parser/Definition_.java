package com.cinepolis.cosmos.parser;

import com.cinepolis.cosmos.monitor.goals.crawler.parser.Field;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Definition_ {

	@Test
	public void of_non_fields() {
		Field.Definition definition = new Field.Definition("[general]");
		assertThat(definition.isField()).isFalse();
		assertThat(definition.raw()).isEqualTo("[general]");
	}

	@Test
	public void of_constant_field() {
		Field.Definition definition = new Field.Definition("name:   laude");
		assertThat(definition.isField()).isTrue();
		assertThat(definition.raw()).isEqualTo("name:laude");
		assertThat(definition.isConstant()).isTrue();
		assertThat(definition.isHidden()).isFalse();
		assertThat(definition.name()).isEqualTo("name");
		assertThat(definition.value()).isEqualTo("laude");
	}

	@Test
	public void of_attribute_field() {
		Field.Definition definition = new Field.Definition("name: @1234: boolean");
		assertThat(definition.isField()).isTrue();
		assertThat(definition.raw()).isEqualTo("name:@1234:boolean");
		assertThat(definition.matches("x.y.z")).isTrue();
		assertThat(definition.isHidden()).isFalse();
		assertThat(definition.isConstant()).isFalse();
		assertThat(definition.name()).isEqualTo("name");
		assertThat(definition.value()).isEqualTo("1234");
		assertThat(definition.type()).isEqualTo("enumerate|0=No|1=Yes|2=No");
	}

	@Test
	public void of_implicit_attribute_field() {
		Field.Definition definition = new Field.Definition("name: 1.3.6.1.4.1.25766.1.12.1.1.3.1.3.0: boolean");
		assertThat(definition.isField()).isTrue();
		assertThat(definition.raw()).isEqualTo("name:@1.3.6.1.4.1.25766.1.12.1.1.3.1.3.0:boolean");
		assertThat(definition.isConstant()).isFalse();
		assertThat(definition.name()).isEqualTo("name");
		assertThat(definition.isConditional()).isFalse();
		assertThat(definition.value()).isEqualTo("1.3.6.1.4.1.25766.1.12.1.1.3.1.3.0");
		assertThat(definition.type()).isEqualTo("enumerate|0=No|1=Yes|2=No");
	}

	@Test
	public void of_conditional_constant() {
		Field.Definition definition = new Field.Definition("name: x.y.z? 1234");
		assertThat(definition.isConstant()).isTrue();
		assertThat(definition.isConditional()).isTrue();
		assertThat(definition.name()).isEqualTo("name");
		assertThat(definition.matches("")).isFalse();
		assertThat(definition.matches("a")).isFalse();
		assertThat(definition.matches("x")).isFalse();
		assertThat(definition.matches("x.y")).isFalse();
		assertThat(definition.matches("x.y.z")).isTrue();
		assertThat(definition.matches("x.y.b")).isFalse();
		assertThat(definition.matches("x.a")).isFalse();
		assertThat(definition.value()).isEqualTo("1234");
		assertThat(definition.type()).isEqualTo("");
	}

	@Test
	public void of_conditional_attribute() {
		Field.Definition definition = new Field.Definition("name:x.y? @1234:string");
		assertThat(definition.raw()).isEqualTo("name:x.y? @1234:string");
		assertThat(definition.name()).isEqualTo("name");
		assertThat(definition.isConstant()).isFalse();
		assertThat(definition.isConditional()).isTrue();
		assertThat(definition.matches("x")).isFalse();
		assertThat(definition.matches("x.y")).isTrue();
		assertThat(definition.matches("x.y.a")).isTrue();
		assertThat(definition.matches("x.y.z")).isTrue();
		assertThat(definition.matches("y")).isFalse();
		assertThat(definition.matches("z")).isFalse();
		assertThat(definition.value()).isEqualTo("1234");
		assertThat(definition.type()).isEqualTo("string");
	}

	@Test
	public void of_hidden_field() {
		Field.Definition definition = new Field.Definition("~name:x.y? @1234:string");
		assertThat(definition.isField()).isTrue();
		assertThat(definition.isHidden()).isTrue();
		assertThat(definition.name()).isEqualTo("name");
		assertThat(definition.isConditional()).isTrue();
		assertThat(definition.matches("x.y")).isTrue();
		assertThat(definition.matches("y")).isFalse();
		assertThat(definition.matches("z")).isFalse();
		assertThat(definition.value()).isEqualTo("1234");

	}
}
