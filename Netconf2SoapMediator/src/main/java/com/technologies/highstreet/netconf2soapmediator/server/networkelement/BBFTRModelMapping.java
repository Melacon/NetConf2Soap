/* Copyright (C) 2018 Pierluigi Greto, Daniel Fritzsche */

package com.technologies.highstreet.netconf2soapmediator.server.networkelement;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class BBFTRModelMapping {

	static BiMap<String, String> bbftrModelBiMap = HashBiMap.create();

	static {
		bbftrModelBiMap.put("CellConfig.LTE.RAN.RF.DLBandwidth", "//data/fap-service/cell-config/lte/lte-ran/lte-ran-rf/dl-bandwidth");
		bbftrModelBiMap.put("CellConfig.LTE.RAN.RF.ULBandwidth","//data/fap-service/cell-config/lte/lte-ran/lte-ran-rf/ul-bandwidth");
		bbftrModelBiMap.put("CellConfig.LTE.RAN.RF.PhyCellID","//data/fap-service/cell-config/lte/lte-ran/lte-ran-rf/phy-cell-id");
		bbftrModelBiMap.put("CellConfig.LTE.RAN.RF.EARFCNDL","//data/fap-service/cell-config/lte/lte-ran/lte-ran-rf/earfcndl");
		bbftrModelBiMap.put("CellConfig.LTE.RAN.RF.EARFCNUL","//data/fap-service/cell-config/lte/lte-ran/lte-ran-rf/earfcnul");

		//ManagedObjectAttribute
		bbftrModelBiMap.put("CellConfig.LTE.RAN.PHY.PRACH.RootSequenceIndex","//data/fap-service/cell-config/lte/lte-ran/lte-ran-phy/lte-ran-phy-prach/root-sequence-index");

		//ManagedObjectAttribute
		bbftrModelBiMap.put("CellConfig.LTE.RAN.Common.CellIdentity","//data/fap-service/cell-config/lte/lte-ran/lte-ran-common/cell-identity");

		//ManagedObjectAttribute
		bbftrModelBiMap.put("CellConfig.LTE.EPC.PLMNList.{i}.PLMNID","");
//		bbftrModelBiMap.put("CellConfig.LTE.EPC.PLMNList.{i}.IsPrimary","");

		//ManagedObjectAttribute
		bbftrModelBiMap.put("FAPControl.LTE.Gateway.S1SigLinkServerList","//data/fap-service/fap-control/fap-control-lte/fap-control-lte-gateway/s1-sig-link-server-list"); //this is a list, more than one value.

		//ManagedObjectAttribute
		bbftrModelBiMap.put("CellConfig.LTE.EPC.TAC","//data/fap-service/cell-config/lte/lte-epc/tac");

		//ManagedObjectAttribute
		bbftrModelBiMap.put("FAPControl.LTE.OpState","//data/fap-service/fap-control/fap-control-lte/op-state");
		bbftrModelBiMap.put("FAPControl.LTE.AdminState","//data/fap-service/fap-control/fap-control-lte/admin-state");

		//ManagedObjectAttribute
		bbftrModelBiMap.put("REM.LTE.EUTRACarrierARFCNDLList","//data/fap-service/rem/rem-lte/eutra-carrier-arfcndl-list"); //this is a list, more than one value.
		bbftrModelBiMap.put("REM.LTE.ScanTimeout","//data/fap-service/rem/rem-lte/scan-timeout");
		bbftrModelBiMap.put("REM.LTE.ScanStatus","//data/fap-service/rem/rem-lte/scan-status");
		bbftrModelBiMap.put("REM.LTE.LastScanTime","//data/fap-service/rem/rem-lte/last-scan-time");
		bbftrModelBiMap.put("REM.LTE.REMBandList","//data/fap-service/rem/rem-lte/rem-band-list");
		bbftrModelBiMap.put("REM.LTE.ScanOnBoot","//data/fap-service/rem/rem-lte/scan-on-boot");
		
		/**
		 * from here on there is the key i
		 */
		//ManagedObjectAttribute
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.RF.EUTRACarrierARFCN","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.RF.PhyCellID","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.RF.RSRP","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.RF.RSRQ","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.RF.RSSI","");

		//ManagedObjectAttribute
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.DLBandwidth","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.ULBandwidth","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.RSTxPower","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.TAC","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.CellID","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.CellBarred","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.CSGIndication","");
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.CSGIdentity","");

		//ManagedObjectAttribute
//		bbftrModelBiMap.put("REM.LTE.Cell.{i}.BCCH.PLMNList.{i}.PLMNID","");
		
		
		
		
	}

	static String getYangfromTR069(String tr069key ) {
		return bbftrModelBiMap.get(tr069key);
	}

	static String getTR069fromYang(String yangkey ) {
		return bbftrModelBiMap.inverse().get(yangkey);
	}
}

