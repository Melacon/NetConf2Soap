/* Copyright (C) 2018 Pierluigi Greto, Daniel Fritzsche */

package com.technologies.highstreet.netconf2soapmediator.server.networkelement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CoreModelMapping {

	static BiMap<String, String> coreModelBiMap = HashBiMap.create();

	static {
		coreModelBiMap.put("Device.DeviceInfo.Manufacturer", "//data/equipment/connector/manufactured-thing/manufacturer-properties/manufacturer-name"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.ManufacturerOUI", "//data/equipment/connector/manufactured-thing/manufacturer-properties/manufacturer-identifier"); // part of Inform RPC
		coreModelBiMap.put("ModelName", "//data/equipment/connector/manufactured-thing/manufacturer-properties/model-identifier"); 
		coreModelBiMap.put("Description", "core-model:equipment-type/description");
		coreModelBiMap.put("Device.DeviceInfo.ProductClass", "//data/equipment/connector/manufactured-thing/manufacturer-properties/part-type-identifier"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.SerialNumber", "//data/equipment/connector/manufactured-thing/manufacturer-properties/serial-number"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.HardwareVersion", "//data/equipment/connector/manufactured-thing/manufacturer-properties/asset-instance-identifier"); // part of Inform RPC
		coreModelBiMap.put("Device.DeviceInfo.SoftwareVersion", "cmpac:software-version"); // part of Inform RPC
		coreModelBiMap.put("AdditionalHardwareVersion", "core-model:asset-type-identifier");
		coreModelBiMap.put("AdditionalSoftwareVersion", "cmpac:additional-software-version");
		coreModelBiMap.put("UpTime", "cmpac:up-time");
		coreModelBiMap.put("FirstUseDate", "//data/equipment/connector/manufactured-thing/manufacturer-properties/manufacture-date");
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
