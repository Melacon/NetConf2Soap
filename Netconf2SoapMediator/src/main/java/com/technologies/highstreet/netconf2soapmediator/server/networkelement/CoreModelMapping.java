/* Copyright (C) 2017-2018 Daniel Fritzsche, Pierluigi Greto */

package com.technologies.highstreet.netconf2soapmediator.server.networkelement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class CoreModelMapping {
	
	static BiMap<String, String> coreModelBiMap = HashBiMap.create();
	
	static {
		coreModelBiMap.put("Manufacturer", "core-model:manufacturer-name");
		coreModelBiMap.put("ManufacturerOUI", "core-model:manufacturer-identifier");
		coreModelBiMap.put("ModelName", "core-model:model-identifier"); 
		coreModelBiMap.put("Description", "core-model:equipment-type/description");
		coreModelBiMap.put("ProductClass", "core-model:part-type-identifier");
		coreModelBiMap.put("SerialNumber", "core-model:serial-number");
		coreModelBiMap.put("HardwareVersion", "core-model:asset-instance-identifier");
		coreModelBiMap.put("SoftwareVersion", "cmpac:software-version");
		coreModelBiMap.put("AdditionalHardwareVersion", "core-model:asset-type-identifier");
		coreModelBiMap.put("AdditionalSoftwareVersion", "cmpac:additional-software-version");
		coreModelBiMap.put("UpTime", "cmpac:up-time");
		coreModelBiMap.put("FirstUseDate", "core-model:manufacture-date");
		coreModelBiMap.put("ConnectionRequestURL", "cmpac:connection-request-url");
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
