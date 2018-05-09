package com.technologies.highstreet.deviceslib.devices;

import java.util.List;

import com.technologies.highstreet.deviceslib.data.SNMPKeyValuePair;
import com.technologies.highstreet.deviceslib.data.SNMPDeviceType;

public class TestSimulatorSNMPDevice extends BaseSNMPDevice {

	public TestSimulatorSNMPDevice() {
		super(SNMPDeviceType.SIMULATOR, new String[]{}, null);
		
	}
	
	@Override
	public String getBaseOID() {
		return ".1.0";
	}

	@Override
	public boolean isSNMPTrapHostConfigureable() {
		return false;
	}

	@Override
	public boolean doSetSNMPTrapHostConfig() {
		return false;
	}

	@Override
	public List<SNMPKeyValuePair> getSNMPTrapHostSetCommands(String ip) {
		return null;
	}

	@Override
	public String ConvertValue(String conversion, String value) throws Exception{
		// TODO Auto-generated method stub
		return null;
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
