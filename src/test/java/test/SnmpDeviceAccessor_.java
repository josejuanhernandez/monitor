package test;

import com.cinepolis.cosmos.Device;
import com.cinepolis.cosmos.Model;
import com.cinepolis.cosmos.SnmpDeviceAccessor;

public class SnmpDeviceAccessor_ {
	public static void main(String[] args) {
		SnmpDeviceAccessor accessor = new SnmpDeviceAccessor(new Device("10.203.32.24"));
		System.out.println(accessor.model().name);
		System.out.println(accessor.execute(Model.Task.Configuration));
	}
}
