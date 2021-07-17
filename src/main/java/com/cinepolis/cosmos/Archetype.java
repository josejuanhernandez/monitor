package com.cinepolis.cosmos;

import com.cinepolis.cosmos.monitor.Logger;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;

import static java.lang.reflect.Modifier.isStatic;

public class Archetype {
	public static final File Root = new File(".");
	public static final File Configuration = new File(Root, "configuration/monitor");
	public static final File Theaters = new File(Configuration, "theaters.tsv");
	public static final File Divisions = new File(Configuration, "divisions.tsv");
	public static final File Forms = new File(Configuration,"forms");
	public static final File DeviceInitForm = new File(Forms, "device.init.form");
	public static final File DeviceUnknownForm = new File(Forms, "device.unknown.form");
	public static final File DeviceUnreachableForm = new File(Forms, "device.unreachable.form");
	public static final File Datalake = new File(Root, "datalake/events");
	public static final File GoalTank = new File(Datalake, "goal");
	public static final File ConfigurationTank = new File(Datalake, "configuration");
	public static final File StatusTank = new File(Datalake, "status");
	public static final File Exhibition = new File(Datalake, "exhibition");
	public static final File Datamarts = new File(Root, "datamarts");
	public static final File TrooperDatamart = new File(Datamarts,"trooper/assets");

	static {
		Field[] declaredFields = Archetype.class.getDeclaredFields();
		Arrays.stream(declaredFields).forEach(Archetype::check);
	}

	private static void check(Field field) {
		if (!isStatic(field.getModifiers())) return;
		try {
			Object value = field.get(null);
			if (value instanceof File) checkFile((File) value);
		} catch (IllegalAccessException ignored) {
		}
	}

	private static void checkFile(File file) {
		if (file.exists()) return;
		Logger.error(file.getAbsolutePath() + " doesn't exist");
		Logger.error("Execution aborted");
		System.exit(-1);
	}
}
