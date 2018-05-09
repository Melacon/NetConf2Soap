package com.technologies.highstreet.deviceslib.devices;

import java.util.List;

import com.technologies.highstreet.deviceslib.data.SNMPDeviceType;
import com.technologies.highstreet.deviceslib.data.SNMPKeyValuePair;

public class ExampleSNMPDevice extends BaseSNMPDevice {

	public ExampleSNMPDevice() {
		super(SNMPDeviceType.EXAMPLEDEVICE, null, null);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getBaseOID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSNMPTrapHostConfigureable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean doSetSNMPTrapHostConfig() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<SNMPKeyValuePair> getSNMPTrapHostSetCommands(String ip) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String ConvertValue(final String conversion, String value) throws Exception {

		if(conversion.equals("int-to-boolean"))
			value=this.intToBoolean1True(value);
		return value;
	}

	@Override
	public String getSNMPPingRequestCommand() {
		return null;
	}

	@Override
	public String ConvertValueSnmp2Netconf(String oid, String value) {
		return value;
	}

	@Override
	public String ConvertValueNetconf2Snmp(String oid, String value) {
		return value;
	}

}
