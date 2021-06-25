package com.cinepolis.cosmos.monitor.goals.crawler;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class SnmpClient implements Closeable {
	private final Target<UdpAddress> target;
	private final Function<String, OID> keyMapper;
	private TransportMapping<UdpAddress> transport;
	private Snmp snmp;

	public SnmpClient(String address) {
		this(address, s->s);
	}

	public SnmpClient(String address, Function<String, String> keyMapper) {
		this.target = targetOf(address);
		this.keyMapper = s->new OID(keyMapper.apply(s));
	}

	public SnmpClient start() throws IOException {
		this.transport = new DefaultUdpTransportMapping();
		this.snmp = new Snmp(transport);
		this.transport.listen();
		return this;
	}

	public void stop() throws IOException {
		if (snmp != null) snmp.close();
		snmp = null;
	}

	public Map<String, String> download(List<String> keys) {
		List<String> values = new ArrayList<>();
		PDU pdu = pdu();
		for (String key : keys) {
			pdu.add(new VariableBinding(translate(key)));
			if (pdu.size() % 16 != 0) continue;
			values.addAll(valuesOf(download(pdu)));
			pdu = pdu();
		}
		values.addAll(valuesOf(download(pdu)));
		return IntStream.range(0,keys.size()).boxed().collect(Collectors.toMap(keys::get, values::get));
	}

	private List<String> valuesOf(PDU download) {
		return download.getVariableBindings().stream().map(VariableBinding::toValueString).collect(Collectors.toList());
	}

	private OID translate(String key) {
		return keyMapper.apply(key);
	}

	private PDU download(PDU pdu) {
		try {
			ResponseEvent<UdpAddress> event = snmp.send(pdu, target, transport);
			if (event == null) throw new RuntimeException("GET timed out");
			return event.getResponse();
		} catch (IOException e) {
			e.printStackTrace();
			return pdu;
		}
	}

	private PDU pdu() {
		PDU pdu = new PDU();
		pdu.setType(PDU.GET);
		return pdu;
	}

	private static Target<UdpAddress> targetOf(String address) {
		try {
			UdpAddress udpAddress = new UdpAddress(InetAddress.getByName(address), 161);
			Target<UdpAddress> target = new CommunityTarget<>(udpAddress, new OctetString("public"));
			target.setRetries(2);
			target.setTimeout(1500);
			target.setVersion(SnmpConstants.version2c);
			return target;
		} catch (UnknownHostException e) {
			return null;
		}
	}

	@Override
	public void close() throws IOException {
		stop();
	}

}
