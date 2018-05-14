/* Copyright (C) 2018 Pierluigi Greto, Daniel Fritzsche */

package com.technologies.highstreet.netconf2soapmediator.server.networkelement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CoreModelMapping {

	static BiMap<String, String> coreModelBiMap = HashBiMap.create();

	static {
		coreModelBiMap.put("Manufacturer", "core-model:manufacturer-name"); // part of Inform RPC
		coreModelBiMap.put("ManufacturerOUI", "core-model:manufacturer-identifier"); // part of Inform RPC
		coreModelBiMap.put("ModelName", "core-model:model-identifier"); 
		coreModelBiMap.put("Description", "core-model:equipment-type/description");
		coreModelBiMap.put("ProductClass", "core-model:part-type-identifier"); // part of Inform RPC
		coreModelBiMap.put("SerialNumber", "core-model:serial-number"); // part of Inform RPC
		coreModelBiMap.put("HardwareVersion", "core-model:asset-instance-identifier"); // part of Inform RPC
		coreModelBiMap.put("SoftwareVersion", "cmpac:software-version"); // part of Inform RPC
		coreModelBiMap.put("AdditionalHardwareVersion", "core-model:asset-type-identifier");
		coreModelBiMap.put("AdditionalSoftwareVersion", "cmpac:additional-software-version");
		coreModelBiMap.put("UpTime", "cmpac:up-time");
		coreModelBiMap.put("FirstUseDate", "core-model:manufacture-date");
		coreModelBiMap.put("ConnectionRequestURL", "cmpac:connection-request-url"); // part of Inform RPC
		coreModelBiMap.put("ConnectionRequestUsername", "cmpac:connection-request-username");
		coreModelBiMap.put("ConnectionRequestPassword", "cmpac:connection-request-password");
		coreModelBiMap.put("LocalTimeZone", "cmpac:local-time-zone");
	}

	static String getYangfromTR069(String tr069key ) {
		return coreModelBiMap.get(tr069key);
	}

	static String getTR069fromYang(String tr069key ) {
		return coreModelBiMap.inverse().get(tr069key);
	}
}
