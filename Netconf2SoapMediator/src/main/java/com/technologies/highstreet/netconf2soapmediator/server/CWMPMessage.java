package com.technologies.highstreet.netconf2soapmediator.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CWMPMessage {

	private static StringBuilder env, end, header, informResponse;
	private static Map<Integer, String> getParamValMap = new HashMap<Integer, String>();
	private static Map<Integer, String> getParamAttMap = new HashMap<Integer, String>();
	private static StringBuilder xmlString = new StringBuilder(10);
	
	// constructor
	public CWMPMessage() {
		env = new StringBuilder();
		end = new StringBuilder();
		header = new StringBuilder();
		informResponse = new StringBuilder();

		//initialize objects
		envelope();
		header();
		informResponse();
		initParamValMap();
		initParamAttMap();
		end();
		createString();
	}

	void createString()  {
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
		getParamValMap.put(1, "Device.DeviceInfo.UpTime");
		getParamValMap.put(2, "Device.ManagementServer.PeriodicInformEnable");
		getParamValMap.put(3, "Device.ManagementServer.PeriodicInformInterval");
	}

	void initParamAttMap() {
		getParamAttMap.put(1, "Device.DeviceInfo.UpTime");
		getParamAttMap.put(2, "Device.ManagementServer.PeriodicInformEnable");
		getParamAttMap.put(3, "Device.ManagementServer.PeriodicInformInterval");
	}
	void envelope() {
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
		end.append("</soapenv:Envelope>\n");
	}

	void informResponse() {
		informResponse.append("\t<soapenv:Body>\n");
		informResponse.append("\t\t<cwmp:InformResponse>\n");
		informResponse.append("\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n");
		informResponse.append("\t\t</cwmp:InformResponse>\n");
		informResponse.append("\t</soapenv:Body>\n");
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
		msg.append(end);
		return msg;
	}

	StringBuilder getParameterAttributes() {
		System.out.println("GetParameterAttributes msg");
		StringBuilder msg = new StringBuilder();
		
		msg.append(env);
		msg.append(header);
		// body
		msg.append("\t<soapenv:Body>\n");
		msg.append("\t\t<cwmp:GetParameterAttributes>\n");
		msg.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[" + getParamAttMap.size() + "]\">\n");

		for (int i = 1; i <= getParamAttMap.size(); i++) {

			msg.append("\t\t\t\t<string>" + getParamAttMap.get(i) + "</string>\n");
		}

		msg.append("\t\t\t</ParameterNames>\n");
		msg.append("\t\t</cwmp:GetParameterAttributes>\n");
		msg.append("\t</soapenv:Body>\n");
		// end body
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
		// end body
		msg.append(end);

		return msg;
	}

	final public static String getXmlString() {
		return xmlString.toString();
	}
}