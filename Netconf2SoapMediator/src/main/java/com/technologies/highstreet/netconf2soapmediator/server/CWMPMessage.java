package com.technologies.highstreet.netconf2soapmediator.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class CWMPMessage {

	private static StringBuilder envelope, envelopeEnd, header, informResponse;
	private static ArrayList<String> getParamValList = new ArrayList<String>();
	private static ArrayList<String> getFAPParamValList = new ArrayList<String>();
	private static ArrayList<String> getParamAttList = new ArrayList<String>();
	private static StringBuilder xmlString = new StringBuilder(10);
	private static ArrayList<ArrayList<String>> setParamList = new ArrayList<ArrayList<String>>();
	
	// constructor
	public CWMPMessage() {
		//initialize objects
		initEnvelope();
		initHeader();
		initInformResponse();
		initGetParamValList();
		initGetFAPParamValList();
		initGetParamAttList();
		initEnvelopeEnd();
		
		getParametersFromFile();
	}

	void getParametersFromFile()  {
		try {
			BufferedReader file = new BufferedReader(new FileReader("./xmlTR069Examples/GetParameterValuesResponse_example.xml"));
			String stringBuffer;
			while ((stringBuffer=file.readLine()) != null) {
				xmlString.append(stringBuffer);
			}
			//System.out.println(xmlString.toString());
			file.close();
		}
		catch (Exception fx) {
			System.out.println("Exception " + fx.toString());
		} 
	}
	
	void initGetFAPParamValList() {
		
		getFAPParamValList.add("Device.Services.FAPService.1.REM");
		getFAPParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.CellRestriction");
		
		// FAPSevice.REM
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.REMBandList");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.EUTRACarrierARFCNDLList"); //this is a list, more than one value.
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.ScanTimeout");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.ScanStatus");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.LastScanTime");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.ScanOnBoot");
		
		// FAPSevice.REM.Cell
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.EUTRACarrierARFCN");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.PhyCellID");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.RSRP");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.RSRQ");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.RSSI");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.DLBandwidth");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.ULBandwidth");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.RSTxPower");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.TAC");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CellID");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CellBarred");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CSGIndication");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CSGIdentity");
//		getFAPParamValList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.PLMNList.PLMNID");
	}
	
	void initGetParamValList() {
			
		// DeviceInfo
		getParamValList.add("Device.DeviceInfo.Manufacturer");
		getParamValList.add("Device.DeviceInfo.ManufacturerOUI");
		getParamValList.add("Device.DeviceInfo.Description");
		getParamValList.add("Device.DeviceInfo.ProductClass");
		getParamValList.add("Device.DeviceInfo.SerialNumber");
		getParamValList.add("Device.DeviceInfo.HardwareVersion");
		getParamValList.add("Device.DeviceInfo.SoftwareVersion");
		getParamValList.add("Device.DeviceInfo.UpTime");
		getParamValList.add("Device.DeviceInfo.FirstUseDate");
		getParamValList.add("Device.IPsec.Enable");
		
		// ManagementServer
		getParamValList.add("Device.ManagementServer.PeriodicInformEnable");
		getParamValList.add("Device.ManagementServer.PeriodicInformInterval");
		
		// FAPService
//		getParamValList.add("Device.Services.FAPService.1.REM.LTE.REMPLMNList");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.ULBandwidth");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.DLBandwidth");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.PhyCellID");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.EARFCNUL");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.EARFCNDL");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.PHY.PRACH.RootSequenceIndex");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.Common.CellIdentity");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.1.PLMNID");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.1.IsPrimary");
		getParamValList.add("Device.Services.FAPService.1.FAPControl.LTE.Gateway.S1SigLinkServerList"); //this is a list, more than one value.
		getParamValList.add("Device.Time.LocalTimeZone");
		getParamValList.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.TAC");
		getParamValList.add("Device.Services.FAPService.1.FAPControl.LTE.OpState");
		getParamValList.add("Device.Services.FAPService.1.FAPControl.LTE.AdminState");	
	}

	void initGetParamAttList() {
		// DeviceInfo
//		getParamAttList.add("Device.DeviceInfo.Manufacturer");
//		getParamAttList.add("Device.DeviceInfo.ManufacturerOUI");
//		getParamAttList.add("Device.DeviceInfo.Description");
//		getParamAttList.add("Device.DeviceInfo.ProductClass");
//		getParamAttList.add("Device.DeviceInfo.SerialNumber");
//		getParamAttList.add("Device.DeviceInfo.HardwareVersion");
//		getParamAttList.add("Device.DeviceInfo.SoftwareVersion");
//		getParamAttList.add("Device.DeviceInfo.UpTime");
//		getParamAttList.add("Device.DeviceInfo.FirstUseDate");
		
		// ManagementServer
		getParamAttList.add("Device.ManagementServer.PeriodicInformEnable");
		getParamAttList.add("Device.ManagementServer.PeriodicInformInterval");
		
		// FAPService
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.REMPLMNList");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.ULBandwidth");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.DLBandwidth");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.PhyCellID");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.EARFCNUL");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.EARFCNDL");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.PHY.PRACH.RootSequenceIndex");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.Common.CellIdentity");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.1.PLMNID");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.1.IsPrimary");
//		getParamAttList.add("Device.Services.FAPService.1.FAPControl.LTE.Gateway.S1SigLinkServerList"); //this is a list, more than one value.
//		getParamAttList.add("Device.Time.LocalTimeZone");
//		getParamAttList.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.TAC");
//		getParamAttList.add("Device.Services.FAPService.1.FAPControl.LTE.OpState");
//		getParamAttList.add("Device.Services.FAPService.1.FAPControl.LTE.AdminState");	

		// FAPSevice.REM
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.REMBandList");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.EUTRACarrierARFCNDLList"); //this is a list, more than one value.
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.ScanTimeout");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.ScanStatus");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.LastScanTime");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.ScanOnBoot");
		
		// FAPSevice.REM.Cell
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.EUTRACarrierARFCN");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.PhyCellID");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.RSRP");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.RSRQ");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.RF.RSSI");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.DLBandwidth");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.ULBandwidth");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.RSTxPower");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.TAC");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CellID");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CellBarred");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CSGIndication");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.CSGIdentity");
//		getParamAttList.add("Device.Services.FAPService.1.REM.LTE.Cell.1.BCCH.PLMNList.PLMNID");
	}
	void initEnvelope() {
		envelope = new StringBuilder();
		
		envelope.append("<soapenv:Envelope ");
		envelope.append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		envelope.append("xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		envelope.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		envelope.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		envelope.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
	}

	void initHeader() {
		header = new StringBuilder();
		
		header.append("\t<soapenv:Header>\n");
		header.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">1</cwmp:ID>\n");
		header.append("\t</soapenv:Header>\n");
	}

	void initEnvelopeEnd() {
		envelopeEnd = new StringBuilder();
		
		envelopeEnd.append("</soapenv:Envelope>\n");
	}

	void initInformResponse() {
		informResponse = new StringBuilder();
		
		informResponse.append("\t<soapenv:Body>\n");
		informResponse.append("\t\t<cwmp:InformResponse>\n");
		informResponse.append("\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n");
		informResponse.append("\t\t</cwmp:InformResponse>\n");
		informResponse.append("\t</soapenv:Body>\n");
	}

	StringBuilder getInformResponse() {
		System.out.println("InformResponse msg");
		StringBuilder msg = new StringBuilder();
		
		msg.append(envelope);
		msg.append(header);
		msg.append(informResponse);
		msg.append(envelopeEnd);
		return msg;
	}

	StringBuilder getParameterValues(boolean FAP) {
		System.out.println("GetParameterValues msg");
		
		ArrayList<String> map = new ArrayList<String>();
		if (FAP == true) {
			map = getFAPParamValList; 
		} else {
			map = getParamValList; 
		}
		
		StringBuilder msg = new StringBuilder();
		msg.append(envelope);
		msg.append(header);
		// body
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:GetParameterValues>\n");
		msg.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[" + map.size() + "]\">\n");

		for (int i = 0; i < map.size(); i++) {

			msg.append("\t\t\t\t<string>" + map.get(i) + "</string>\n");
		}

		msg.append("\t\t\t</ParameterNames>\n");
		msg.append("\t\t</cwmp:GetParameterValues>\n");
		msg.append("\t</soapenv:Body>\n");
		// end body
		msg.append(envelopeEnd);
		return msg;
	}

	StringBuilder getParameterAttributes() {
		System.out.println("GetParameterAttributes msg");
		StringBuilder msg = new StringBuilder();
		
		msg.append(envelope);
		msg.append(header);
		// body
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:GetParameterAttributes>\n");
		msg.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[" + getParamAttList.size() + "]\">\n");

		for (int i = 0; i < getParamAttList.size(); i++) {

			msg.append("\t\t\t\t<string>" + getParamAttList.get(i) + "</string>\n");
		}

		msg.append("\t\t\t</ParameterNames>\n");
		msg.append("\t\t</cwmp:GetParameterAttributes>\n");
		msg.append("\t</soapenv:Body>\n");
		// end body
		msg.append(envelopeEnd);
		return msg;
	}

	StringBuilder setParameterValues(ArrayList<ArrayList<String>> list) {
		System.out.println("SetParameterValues msg");
		StringBuilder msg = new StringBuilder();

		msg.append(envelope);
		msg.append(header);
		// body
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:SetParameterValues>\n");
		msg.append("\t\t\t<ParameterList soap:arrayType=\"cwmp:ParameterValueStruct[" + list.size() + "]\">\n");

		for (int i = 0; i < list.size(); i++) {
			msg.append("\t\t\t<ParameterValueStruct>\n");
			msg.append("\t\t\t\t<Name>" + list.get(i).get(0) + "</Name>\n");
			msg.append("\t\t\t\t<Value xsi:type=\"xsd:" + list.get(i).get(1) + "</Value>\n");
			msg.append("\t\t\t</ParameterValueStruct>\n");
		}

		msg.append("\t\t\t</ParameterList>\n");
		msg.append("\t\t\t<ParameterKey>12345</ParameterKey>\n");
		msg.append("\t\t</cwmp:SetParameterValues>\n");
		msg.append("\t</soapenv:Body>\n");
		// end body
		msg.append(envelopeEnd);

		return msg;
	}

	void initSetParamList() {
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		System.out.println(function + "Creating SET message to initialize the device");
		ArrayList<String> list = new ArrayList<String>();

//		list = new ArrayList<String>();
//		list.add("Device.Services.FAPService.1.FAPControl.LTE.AdminState");
//		list.add("boolean" + "\">" + "false");
//		setParamList.add(list);
		
		list = new ArrayList<String>();
		list.add("Device.ManagementServer.PeriodicInformEnable");
		list.add("boolean" + "\">" + "true");
		getSetParamList().add(list);

		list = new ArrayList<String>();
		list.add("Device.ManagementServer.PeriodicInformInterval");
		list.add("unsignedInt" + "\">" + "30");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.IPsec.Enable");
		list.add("boolean" + "\">" + "false");
		setParamList.add(list);
	}
	
	void initSetIPsecParamList() {
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		System.out.println(function + "Creating SET message to initialize the device");
		ArrayList<String> list = new ArrayList<String>();

		list = new ArrayList<String>();
		list.add("Device.IPsec.Enable");
		list.add("boolean" + "\">" + "false");
		setParamList.add(list);
	}
	
	void initSetFAPParamList() {
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		System.out.println(function + "Creating SET message to initialize the device");
		ArrayList<String> list = new ArrayList<String>();

		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.PhyCellID");
		list.add("string" + "\">" + "210");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.DLBandwidth");
		list.add("string" + "\">" + "100");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.ULBandwidth");
		list.add("string" + "\">" + "100");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.PHY.PRACH.RootSequenceIndex");
		list.add("string" + "\">" + "738,0,837,12");
		getSetParamList().add(list);

		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.EARFCNUL");
		list.add("string" + "\">" + "18700");
		getSetParamList().add(list);

		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.EARFCNDL");
		list.add("string" + "\">" + "700");
		getSetParamList().add(list);

		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.TAC");
		list.add("unsignedInt" + "\">" + "1");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.1.PLMNID");
		list.add("string" + "\">" + "311181");
		getSetParamList().add(list);

		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.1.IsPrimary");
		list.add("boolean" + "\">" + "true");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.1.Enable");
		list.add("boolean" + "\">" + "true");
		getSetParamList().add(list);

		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.FAPControl.LTE.Gateway.S1SigLinkServerList");
		list.add("string" + "\">" + "10.9.50.66");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.FAPControl.LTE.Gateway.S1SigLinkPort");
		list.add("unsignedInt" + "\">" + "36412");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Time.LocalTimeZone");
		list.add("string" + "\">" + "EST-5EDT,M3.2.0/2,M11.1.0/2");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.CellConfig.LTE.RAN.Common.CellIdentity");
		list.add("unsignedInt" + "\">" + "53760");
		getSetParamList().add(list);
	
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.REM.LTE.EUTRACarrierARFCNDLList");
		list.add("string" + "\">" + "700");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.REM.LTE.ScanTimeout");
		list.add("unsignedInt" + "\">" + "300");
		getSetParamList().add(list);
		
		list = new ArrayList<String>();
		list.add("Device.Services.FAPService.1.REM.LTE.ScanOnBoot");
		list.add("boolean" + "\">" + "1");
		getSetParamList().add(list);
	}
	
	final public static String getXmlString() {
		return xmlString.toString();
	}

	public static ArrayList<ArrayList<String>> getSetParamList() {
		return setParamList;
	}

	public static void setSetParamList(ArrayList<ArrayList<String>> setParamList) {
		CWMPMessage.setParamList = setParamList;
	}
}