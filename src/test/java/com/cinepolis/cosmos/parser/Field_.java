package com.cinepolis.cosmos.parser;

import com.cinepolis.cosmos.Archetype;
import com.cinepolis.cosmos.monitor.goals.crawler.parser.Field;
import org.junit.Before;
import org.junit.Test;
import org.snmp4j.asn1.BERInputStream;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import sun.misc.Unsafe;

import java.io.File;
import java.io.OutputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class Field_ {

	@Test
	public void should_format_constants() {
		assertThat(Field.of("ts: $ts").name).isEqualTo("ts");
		assertThat(Field.of("name: $ts").format(null)).isEqualTo("$ts");
		assertThat(Field.of("name: hola").format(null)).isEqualTo("hola");
	}

	@Test
	public void should_format_strings() {
		Field field = Field.of("name:1.3.6.1.2.1.1.5.0:string");
		assertThat(field.format("hola")).isEqualTo("hola");
		assertThat(field.format(variable("hola"))).isEqualTo("hola");
	}

	@Test
	public void should_format_integers() {
		Field field = Field.of("runtime: 1.3.6.1.4.1.12612.220.11.2.2.3.0:int");
		assertThat(field.format(variable(4))).isEqualTo("4");
	}

	@Test
	public void should_format_enumerates() {
		Field field = Field.of("mode: 1.3.6.1.4.1.12612.220.11.2.2.18.0: enumerate|1=2D|2=3D");
		assertThat(field.format(1)).isEqualTo("2D");
		assertThat(field.format(2)).isEqualTo("3D");
		assertThat(field.format(0)).isEqualTo("NA#0");
	}

	@Test
	public void should_format_booleans() {
		Field field = Field.of("serviceMode: 1.3.6.1.4.1.12612.220.11.2.2.14.0:boolean");
		assertThat(field.format(1)).isEqualTo("Yes");
		assertThat(field.format(0)).isEqualTo("No");
		assertThat(field.format(2)).isEqualTo("No");
	}

	@Test
	public void should_format_maps() {
		Field field = Field.of("serviceMode: 1.3.6.1.4.1.12612.220.11.2.2.14.0: enumerate|test.map");
		assertThat(field.format(0)).isEqualTo("A");
		assertThat(field.format(1)).isEqualTo("B");
		assertThat(field.format(2)).isEqualTo("C");
		assertThat(field.format(3)).isEqualTo("NA#3");
	}


	@Before
	public void setArchetype() throws ReflectiveOperationException {
		assertThat(Archetype.Configuration.getName()).isNotEmpty();
		setFinalStaticField(Archetype.class.getDeclaredField("Configuration"), new File("test/res"));
		setFinalStaticField(Archetype.class.getDeclaredField("Forms"), new File("src/test/res/bases"));
	}

	private static void setFinalStaticField(java.lang.reflect.Field field, Object value) throws ReflectiveOperationException {
		java.lang.reflect.Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
		unsafeField.setAccessible(true);
		final Unsafe unsafe = (Unsafe) unsafeField.get(null);

		final Object staticFieldBase = unsafe.staticFieldBase(field);
		final long staticFieldOffset = unsafe.staticFieldOffset(field);
		unsafe.putObject(staticFieldBase, staticFieldOffset, value);
	}

	private Variable variable(Object value) {
		return new Variable() {

			@Override
			public int getSyntax() {
				return 0;
			}

			@Override
			public boolean isException() {
				return false;
			}

			@Override
			public int toInt() {
				return (int) value;
			}

			@Override
			public long toLong() {
				return (long) value;
			}

			@Override
			public String getSyntaxString() {
				return null;
			}

			@Override
			public OID toSubIndex(boolean b) {
				return null;
			}

			@Override
			public String toString() {
				return value.toString();
			}

			@Override
			public void fromSubIndex(OID oid, boolean b) {

			}

			@Override
			public boolean isDynamic() {
				return false;
			}

			@Override
			public int getBERLength() {
				return 0;
			}

			@Override
			public int getBERPayloadLength() {
				return 0;
			}

			@Override
			public void decodeBER(BERInputStream berInputStream) {

			}

			@Override
			public void encodeBER(OutputStream outputStream) {

			}

			@Override
			public int compareTo(Variable variable) {
				return 0;
			}

			@Override
			public Object clone() {
				return null;
			}
		};
	}
}
