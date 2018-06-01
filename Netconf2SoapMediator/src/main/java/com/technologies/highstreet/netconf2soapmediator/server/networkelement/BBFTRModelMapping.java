/* Copyright (C) 2018 Pierluigi Greto, Daniel Fritzsche */

package com.technologies.highstreet.netconf2soapmediator.server.networkelement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class BBFTRModelMapping {

	static BiMap<String, String> bbftrModelBiMap = HashBiMap.create();

	static {
		bbftrModelBiMap.put("CellConfig.LTE.RAN.RF.DLBandwidth", "//data/fap-service/cell-config/lte/lte-ran/lte-ran-rf/dl-bandwidth"); 
	}

	static String getYangfromTR069(String tr069key ) {
		return bbftrModelBiMap.get(tr069key);
	}

	static String getTR069fromYang(String yangkey ) {
		return bbftrModelBiMap.inverse().get(yangkey);
	}
}
