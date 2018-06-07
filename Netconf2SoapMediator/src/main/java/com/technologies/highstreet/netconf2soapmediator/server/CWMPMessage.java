package com.technologies.highstreet.netconf2soapmediator.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CWMPMessage {

	private static StringBuilder envelope, envelopeEnd, header, informResponse;
	private static Map<Integer, String> getParamValMap = new HashMap<Integer, String>();
	private static Map<Integer, String> getParamAttMap = new HashMap<Integer, String>();
	private static StringBuilder xmlString = new StringBuilder(10);
	
	// constructor
	public CWMPMessage() {
		//initialize objects
		initEnvelope();
		initHeader();
		initInformResponse();
		initParamValMap();
		initParamAttMap();
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
	
	void initParamValMap() {
			
		getParamValMap.put(1, "Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.DLBandwidth");
		getParamValMap.put(2, "Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.ULBandwidth");
		getParamValMap.put(3, "Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.PhyCellID");
		getParamValMap.put(4, "Device.Services.FAPService.1.CellConfig.LTE.RAN.RF.EARFCNDL");

		//ManagedObjectAttribute
		getParamValMap.put(5, "Device.Services.FAPService.1.CellConfig.LTE.RAN.PHY.PRACH.RootSequenceIndex");

		//ManagedObjectAttribute
		getParamValMap.put(6, "Device.Services.FAPService.1.CellConfig.LTE.RAN.Common.CellIdentity");

		//ManagedObjectAttribute
//		getParamValMap.put("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.{i}.PLMNID","");
//		getParamValMap.put("Device.Services.FAPService.1.CellConfig.LTE.EPC.PLMNList.{i}.IsPrimary","");

		//ManagedObjectAttribute
		getParamValMap.put(7, "Device.Services.FAPService.1.FAPControl.LTE.Gateway.S1SigLinkServerList"); //this is a list, more than one value.

		//ManagedObjectAttribute
		getParamValMap.put(8, "Device.Services.FAPService.1.CellConfig.LTE.EPC.TAC");

		//ManagedObjectAttribute
		getParamValMap.put(9, "Device.Services.FAPService.1.FAPControl.LTE.OpState");
		getParamValMap.put(10, "Device.Services.FAPService.1.FAPControl.LTE.AdminState");

		//ManagedObjectAttribute
		getParamValMap.put(11, "Device.Services.FAPService.1.REM.LTE.EUTRACarrierARFCNDLList"); //this is a list, more than one value.
		getParamValMap.put(12, "Device.Services.FAPService.1.REM.LTE.ScanTimeout");
		getParamValMap.put(13, "Device.Services.FAPService.1.REM.LTE.ScanStatus");
		getParamValMap.put(14, "Device.Services.FAPService.1.REM.LTE.LastScanTime");
		getParamValMap.put(15, "Device.Services.FAPService.1.REM.LTE.REMBandList");
		getParamValMap.put(16, "Device.Services.FAPService.1.REM.LTE.ScanOnBoot");
		
		
//		getParamValMap.put(3, "Device.ManagementServer.PeriodicInformInterval");
	}

	void initParamAttMap() {
		getParamAttMap.put(0, "Device.DeviceInfo.UpTime");
		getParamAttMap.put(1, "Device.ManagementServer.PeriodicInformEnable");
		getParamAttMap.put(2, "Device.ManagementServer.PeriodicInformInterval");
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

	StringBuilder getParameterValues() {
		System.out.println("GetParameterValues msg");
		StringBuilder msg = new StringBuilder();
		
		msg.append(envelope);
		msg.append(header);
		// body
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:GetParameterValues>\n");
		msg.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[" + getParamValMap.size() + "]\">\n");

		for (int i = 1; i <= getParamValMap.size(); i++) {

			msg.append("\t\t\t\t<string>" + getParamValMap.get(i) + "</string>\n");
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
		msg.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[" + getParamAttMap.size() + "]\">\n");

		for (int i = 0; i < getParamAttMap.size(); i++) {

			msg.append("\t\t\t\t<string>" + getParamAttMap.get(i) + "</string>\n");
		}

		msg.append("\t\t\t</ParameterNames>\n");
		msg.append("\t\t</cwmp:GetParameterAttributes>\n");
		msg.append("\t</soapenv:Body>\n");
		// end body
		msg.append(envelopeEnd);
		return msg;
	}

	StringBuilder setParameterValues(Map<Integer, ArrayList<String>> map) {
		System.out.println("SetParameterValues msg");
		StringBuilder msg = new StringBuilder();

		msg.append(envelope);
		msg.append(header);
		// body
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:SetParameterValues>\n");
		msg.append("\t\t\t<ParameterList soap:arrayType=\"cwmp:ParameterValueStruct[" + map.size() + "]\">\n");

		for (int i = 0; i < map.size(); i++) {
			msg.append("\t\t\t<ParameterValueStruct>\n");
			msg.append("\t\t\t\t<Name>" + map.get(i).get(0) + "</Name>\n");
			msg.append("\t\t\t\t<Value>" + map.get(i).get(1) + "</Value>\n");
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

	final public static String getXmlString() {
		return xmlString.toString();
	}
}