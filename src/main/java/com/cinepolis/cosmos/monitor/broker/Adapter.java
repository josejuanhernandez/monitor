package com.cinepolis.cosmos.monitor.broker;

import com.cinepolis.cosmos.monitor.Event;

public interface Adapter {

	void process(Event event);

}
