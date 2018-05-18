/* Copyright (C) 2018 Pierluigi Greto, Daniel Fritzsche */

package com.technologies.highstreet.netconf2soapmediator.server.networkelement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CoreModelMapping {

	static BiMap<String, String> coreModelBiMap = HashBiMap.create();

	static {
		coreModelBiMap.put("Device.DeviceInfo.Manufacturer", "//data/equipment/manufactured-thing/manufacturer-properties/manufacturer-name"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.ManufacturerOUI", "//data/equipment/manufactured-thing/manufacturer-properties/manufacturer-identifier%" +
																"//data/equipment/uuid%" + 
																"//data/equipment-pac/equipment%"+
																"//data/network-element/uuid%" +
																"//data/network-element/extension/value"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.ModelName", "//data/equipment/manufactured-thing/manufacturer-properties/model-identifier%"+
														  "//data/equipment/manufactured-thing/equipment-type/model-identifier"); 
		coreModelBiMap.put("Device.DeviceInfo.Description", "//data/equipment/manufactured-thing/equipment-type/description");
		coreModelBiMap.put("Device.DeviceInfo.ProductClass", "//data/equipment/manufactured-thing/equipment-type/part-type-identifier"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.SerialNumber", "//data/equipment/manufactured-thing/equipment-instance/serial-number"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.HardwareVersion", "//data/equipment/manufactured-thing/equipment-instance/asset-instance-identifier"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.SoftwareVersion", "cmpac:software-version"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.AdditionalHardwareVersion", "//data/equipment/manufactured-thing/operator-augmented-equipment-type/asset-instance-identifier");
		coreModelBiMap.put("AdditionalSoftwareVersion", "cmpac:additional-software-version");
		coreModelBiMap.put("UpTime", "cmpac:up-time");
		coreModelBiMap.put("Device.DeviceInfo.FirstUseDate", "//data/equipment/manufactured-thing/equipment-instance/manufacture-date");
		coreModelBiMap.put("Device.ManagementServer.ConnectionRequestURL", "cmpac:connection-request-url"); // part of Inform RPC
		coreModelBiMap.put("ConnectionRequestUsername", "cmpac:connection-request-username");
		coreModelBiMap.put("ConnectionRequestPassword", "cmpac:connection-request-password");
		coreModelBiMap.put("LocalTimeZone", "cmpac:local-time-zone");
	}

	static String getYangfromTR069(String tr069key ) {
		return coreModelBiMap.get(tr069key);
	}

	static String getTR069fromYang(String yangkey ) {
		return coreModelBiMap.inverse().get(yangkey);
	}
}
