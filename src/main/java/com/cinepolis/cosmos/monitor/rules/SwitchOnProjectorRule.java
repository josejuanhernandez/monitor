package com.cinepolis.cosmos.monitor.rules;

import com.cinepolis.cosmos.monitor.Agent;
import com.cinepolis.cosmos.monitor.goals.scrapper.model.show.Show;

public class SwitchOnProjectorRule implements Agent.Rule {
	@Override
	public boolean check(Object object) {
		return object instanceof Show && soldTickets((Show) object) && projectorWithStatusOff((Show) object);

	}

	private boolean projectorWithStatusOff(Show show) {
		return true;
		/*
		SnmpDeviceAccessor deviceAccessor = new SnmpDeviceAccessor(Environment.find(show.projector));
		String status = deviceAccessor.read("status");
		return status.contains("mode: Off");
		 */
	}

	private boolean soldTickets(Show show) {
		return show.sold > 0 && !show.screen.equals("NA");
	}

	@Override
	public void execute(Object object) {
		System.out.println("Switch on " + ((Show) object).projector);
		System.out.println(object);
		System.out.println();
	}

}
