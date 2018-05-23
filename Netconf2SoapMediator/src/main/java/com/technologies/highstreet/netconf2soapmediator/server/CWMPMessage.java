package com.technologies.highstreet.netconf2soapmediator.server;

import java.util.ArrayList;
import java.util.Map;

public class CWMPMessage {

	private static StringBuilder env, end, header, informResponse, getParamVal, getParamAtt;

	// constructor
	public CWMPMessage() {
		env = new StringBuilder();
		end = new StringBuilder();
		header = new StringBuilder();
		informResponse = new StringBuilder();
		getParamVal = new StringBuilder();
		getParamAtt = new StringBuilder();
		
		//initialize string objects
		envelope();
		header();
		informResponse();
		getParameterValuesBody();
		getParameterAttributesBody();
	}

	void envelope() {
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		env.append("<soapenv:Envelope ");
		env.append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		env.append("xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		env.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		env.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		env.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
	}

	void header() {
		header.append("\t<soapenv:Header>\n");
		header.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">1</cwmp:ID>\n");
		header.append("\t</soapenv:Header>\n");
	}

	void end() {
		// end
		end.append("</soapenv:Envelope>\n");
	}
	
	void informResponse() {
		// body
		informResponse.append("\t<soapenv:Body>\n");
		informResponse.append("\t\t<cwmp:InformResponse>\n");
		informResponse.append("\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n");
		informResponse.append("\t\t</cwmp:InformResponse>\n");
		informResponse.append("\t</soapenv:Body>\n");
	}

	void getParameterValuesBody() {
		// body
		getParamVal.append("\t<soapenv:Body>\n");
		getParamVal.append("\t\t<cwmp:GetParameterValues>\n");
		getParamVal.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[3]\">\n");
		getParamVal.append("\t\t\t\t<string>Device.DeviceInfo.UpTime</string>\n");
		getParamVal.append("\t\t\t\t<string>Device.ManagementServer.PeriodicInformEnable</string>\n");
		getParamVal.append("\t\t\t\t<string>Device.ManagementServer.PeriodicInformInterval</string>\n");
		getParamVal.append("\t\t\t</ParameterNames>\n");
		getParamVal.append("\t\t</cwmp:GetParameterValues>\n");
		getParamVal.append("\t</soapenv:Body>\n");
	}

	void getParameterAttributesBody() {
		// body
		getParamAtt.append("\t<soapenv:Body>\n");
		getParamAtt.append("\t\t<cwmp:GetParameterAttributes>\n");
		getParamAtt.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[3]\">\n");
		getParamAtt.append("\t\t\t\t<string>Device.DeviceInfo.UpTime</string>\n");
		getParamAtt.append("\t\t\t\t<string>Device.ManagementServer.PeriodicInformEnable</string>\n");
		getParamAtt.append("\t\t\t\t<string>Device.ManagementServer.PeriodicInformInterval</string>\n");
		getParamAtt.append("\t\t\t</ParameterNames>\n");
		getParamAtt.append("\t\t</cwmp:GetParameterAttributes>\n");
		getParamAtt.append("\t</soapenv:Body>\n");
	}
	
	StringBuilder getInformResponse() {
		System.out.println("InformResponse msg");
		StringBuilder msg = new StringBuilder();
		msg.append(env);
		msg.append(header);
		msg.append(informResponse);
		msg.append(end);
		return msg;
	}
	
	StringBuilder getParameterValues() {
		System.out.println("GetParameterValues msg");
		StringBuilder msg = new StringBuilder();
		msg.append(env);
		msg.append(header);
		msg.append(getParamVal);
		msg.append(end);
		return msg;
	}
	
	StringBuilder getParameterAttributes() {
		System.out.println("GetParameterAttributes msg");
		StringBuilder msg = new StringBuilder();
		msg.append(env);
		msg.append(header);
		msg.append(getParamAtt);
		msg.append(end);
		return msg;
	}
	
	StringBuilder setParameterValues(Map<Integer, ArrayList<String>> map) {
		System.out.println("SetParameterValues msg");
		StringBuilder msg = new StringBuilder();
		
		msg.append(env);
		msg.append(header);

		// body
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:SetParameterValues>\n");

		msg.append("\t\t\t<ParameterList soap:arrayType=\"cwmp:ParameterValueStruct[" + map.size() + "]\">\n");
		
		System.out.println(map.size());
		
		for (int i = 1; i <= map.size(); i++) {

			msg.append("\t\t\t<ParameterValueStruct>\n");
			msg.append("\t\t\t\t<Name>" + map.get(i).get(0) + "</Name>\n");
			msg.append("\t\t\t\t<Value>" + map.get(i).get(1) + "</Value>\n");
			msg.append("\t\t\t</ParameterValueStruct>\n");
		}
		
		msg.append("\t\t\t</ParameterList>\n");

		msg.append("\t\t\t<ParameterKey>12345</ParameterKey>\n");

		msg.append("\t\t</cwmp:SetParameterValues>\n");
		msg.append("\t</soapenv:Body>\n");

		msg.append(end);
		return msg;
	}

}