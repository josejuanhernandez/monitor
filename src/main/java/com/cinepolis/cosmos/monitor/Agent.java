package com.cinepolis.cosmos.monitor;

import com.cinepolis.cosmos.monitor.rules.SwitchOnProjectorRule;

import java.util.ArrayList;
import java.util.List;

public class Agent {
	public static final Agent instance = new Agent();
	private final List<Rule> rules = new ArrayList<>();

	private Agent() {

	}

	public void check(Object object) {
		for (Rule rule : rules) {
			if (rule.check(object)) rule.execute(object);
		}
	}

	public void add(Rule rule) {
		rules.add(rule);
	}

	public interface Rule {
		boolean check(Object object);
		void execute(Object object);
	}

	static {
		Agent.instance.add(new SwitchOnProjectorRule());
	}

}
