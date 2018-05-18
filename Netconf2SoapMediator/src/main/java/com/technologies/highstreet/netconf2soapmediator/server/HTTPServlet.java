/* Copyright (C) 2018 Daniel Fritzsche, Pierluigi Greto */

package com.technologies.highstreet.netconf2soapmediator.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SoapNetworkElement;


public class HTTPServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5071770086030271370L;
	private static Netconf2SoapNetworkElement networkElement = null;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		System.out.println(HTTPServlet.getBody(request));
		response.getWriter().println("Get Hello World!");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		//System.out.println("Received HTTP request:");

		final String reqBody = HTTPServlet.getBody(request);
		//System.out.println(reqBody);

		StringBuilder sb  = generateString();

		if (reqBody.contains("Fault")) {
			System.out.println("Received Fault msg");
			return;
		}
		else if (reqBody.contains("cwmp:Inform") && reqBody.contains("<EventCode>1 BOOT")) {
			if (reqBody.contains("<EventCode>2 PERIODIC")) {
				System.out.println("Received Inform msg (BOOT PERIODIC REQUEST)");
			} else {
				System.out.println("Received Inform msg (BOOT)");
			}

			Netconf2SoapMediator.connActive = true;
			System.out.println("Netconf2SoapMediator.connActive=" + Netconf2SoapMediator.connActive );
			networkElement.setTr069DocumentCFromString(reqBody);
			sendInform(sb);
		}
		else if (reqBody.contains("cwmp:Inform") && reqBody.contains("<EventCode>2 PERIODIC")) {
			System.out.println("Received Inform msg (PERIODIC REQUEST)");
			Netconf2SoapMediator.connActive = true;
			System.out.println("Netconf2SoapMediator.connActive=" + Netconf2SoapMediator.connActive );
			networkElement.setTr069DocumentCFromString(reqBody);
			sendInform(sb);
		}
		else if (reqBody.contains("cwmp:Inform") && reqBody.contains("<EventCode>6 CONNECTION REQUEST")) {
			System.out.println("Received Inform msg (CONNECTION REQUEST)");
			Netconf2SoapMediator.connActive = true;
			networkElement.setTr069DocumentCFromString(reqBody);
			sendInform(sb);
		}
		else if (reqBody.contains("cwmp:Inform") ) {
			System.out.println("Received Inform msg (unknown)");
			networkElement.setTr069DocumentCFromString(reqBody);
			sendInform(sb);
		}
		else if (reqBody.contains("cwmp:GetParameterValuesResponse")) {
			System.out.println("Received GetParameterValuesResponse msg");
			sendGetParameterAttributes(sb);
		}
		else if (reqBody.contains("cwmp:GetParameterAttributesResponse")) {
			System.out.println("Received GetParameterAttributesResponse msg");
			// send empty response, close connection
			StringBuilder sb2 = new StringBuilder(10);
			System.out.println("Sending Empty msg");
			System.out.println(sb2);
			//response.getWriter().println(sb2);
			Netconf2SoapMediator.connActive = false;
			System.out.println("Netconf2SoapMediator.connActive=" + Netconf2SoapMediator.connActive );
			return;
		}
		else if (reqBody.equals("")) {
			System.out.println("Received HTTP request: Empty");
			//send GetParameterValues
			sendGetParameterValues(sb);
		}
		else {
			System.out.println("Received Unknown msg");
			return;}

		// end
		sb.append("</soapenv:Envelope>\n");
		System.out.println("Sending HTTP reply:");
		//System.out.println(sb);
		response.getWriter().println(sb);
	}

	static StringBuilder sendInform(StringBuilder sb) {
		System.out.println("Sending InformResponse msg");
		// body
		sb.append("\t<soapenv:Body>\n");
		sb.append("\t\t<cwmp:InformResponse>\n");
		sb.append("\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n");
		sb.append("\t\t</cwmp:InformResponse>\n");
		sb.append("\t</soapenv:Body>\n");

		return sb;
	}

	static StringBuilder generateString() {
		StringBuilder sb = new StringBuilder(10);
		// Soap envelope
		//sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
		sb.append("<soapenv:Envelope ");
		sb.append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		sb.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		sb.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		// header
		sb.append("\t<soapenv:Header>\n");
		sb.append("\t\t<cwmp:ID soapenv:mustUnderstand=\"1\">1</cwmp:ID>\n");
		sb.append("\t</soapenv:Header>\n");

		return sb;
	}

	static StringBuilder sendGetParameterValues(StringBuilder sb) {
		System.out.println("Sending GetParameterValues msg");
		// body
		sb.append("\t<soapenv:Body>\n");
		sb.append("\t\t<cwmp:GetParameterValues>\n");
		sb.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[2]\">\n");
		sb.append("\t\t\t\t<string>Device.DeviceInfo.UpTime</string>\n");
		sb.append("\t\t\t\t<string>Device.DeviceInfo.DeviceLog</string>\n");
		sb.append("\t\t\t</ParameterNames>\n");
		sb.append("\t\t</cwmp:GetParameterValues>\n");
		sb.append("\t</soapenv:Body>\n");

		return sb;
	}

	static StringBuilder sendGetParameterAttributes(StringBuilder sb) {
		System.out.println("Sending GetParameterAttributes msg");
		// body
		sb.append("\t<soapenv:Body>\n");
		sb.append("\t\t<cwmp:GetParameterAttributes>\n");
		sb.append("\t\t\t<ParameterNames soap:arrayType=\"xsd:string[2]\">\n");
		sb.append("\t\t\t\t<string>Device.DeviceInfo.UpTime</string>\n");
		sb.append("\t\t\t\t<string>Device.DeviceInfo.DeviceLog</string>\n");
		sb.append("\t\t\t</ParameterNames>\n");
		sb.append("\t\t</cwmp:GetParameterAttributes>\n");
		sb.append("\t</soapenv:Body>\n");

		return sb;
	}

	public static String getBody(HttpServletRequest request) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			InputStream inputStream = request.getInputStream();
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

	public static Netconf2SoapNetworkElement getNetworkElement() {
		return networkElement;
	}

	public static void setNetworkElement(Netconf2SoapNetworkElement networkElement) {
		HTTPServlet.networkElement = networkElement;
	}

}
