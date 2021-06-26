package com.cinepolis.cosmos.monitor.adapters;

import com.cinepolis.cosmos.monitor.Event;
import com.cinepolis.cosmos.monitor.broker.Adapter;
import com.cinepolis.cosmos.monitor.goals.scrapper.session.Session;

public class SwitchOnProjectorAdapter implements Adapter {
	@Override
	public void process(Event event) {
		boolean b = event instanceof Session && soldTickets((Session) event) && projectorWithStatusOff((Session) event);
		if (!b) return;

	}

	private boolean projectorWithStatusOff(Session session) {
		return true;
		/*
		SnmpDeviceAccessor deviceAccessor = new SnmpDeviceAccessor(Environment.find(show.projector));
		String status = deviceAccessor.read("status");
		return status.contains("mode: Off");
		 */
	}

	private boolean soldTickets(Session session) {
		return session.sold > 0 && !session.screen.equals("NA");
	}

	private void execute(Object object) {
		System.out.println("Switch on " + ((Session) object).projector);
		System.out.println(object);
		System.out.println();
	}

}
