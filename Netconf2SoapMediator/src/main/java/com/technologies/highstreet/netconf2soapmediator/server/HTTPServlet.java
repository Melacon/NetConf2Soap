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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.technologies.highstreet.netconf2soapmediator.server.networkelement.Netconf2SoapNetworkElement;


public class HTTPServlet extends HttpServlet {

	private static final long serialVersionUID = 5071770086030271370L;
	private static Netconf2SoapNetworkElement networkElement = null;
	private static boolean setParam = true;
	private static CWMPMessage CWMPmsg = new CWMPMessage();

	private static Map<Integer, ArrayList<String>> setParamMap = new HashMap<Integer, ArrayList<String>>();
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

		StringBuilder sb = new StringBuilder(10);
		
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

			setParam=true;
			Netconf2SoapMediator.connActive = true;
			networkElement.setTr069DocumentCFromString(reqBody);
			sb = CWMPmsg.getInformResponse();
		}
		else if (reqBody.contains("cwmp:Inform") && reqBody.contains("<EventCode>2 PERIODIC")) {
			System.out.println("Received Inform msg (PERIODIC REQUEST)");
			Netconf2SoapMediator.connActive = true;
			networkElement.setTr069DocumentCFromString(reqBody);
			sb = CWMPmsg.getInformResponse();
		}
		else if (reqBody.contains("cwmp:Inform") && reqBody.contains("<EventCode>6 CONNECTION REQUEST")) {
			System.out.println("Received Inform msg (CONNECTION REQUEST)");
			Netconf2SoapMediator.connActive = true;
			networkElement.setTr069DocumentCFromString(reqBody);
			sb = CWMPmsg.getInformResponse();
		}
		else if (reqBody.contains("cwmp:Inform") ) {
			System.out.println("Received Inform msg (unknown)");
			networkElement.setTr069DocumentCFromString(reqBody);
			sb = CWMPmsg.getInformResponse();
		}
		else if (reqBody.contains("cwmp:GetParameterValuesResponse")) {
			System.out.println("Received GetParameterValuesResponse msg");
			if (setParam == true) {
				Random rand = new Random(); 
				int value = rand.nextInt(5000) + 1000;
				
				ArrayList<String> list = new ArrayList<String>();
				list.add("Device.ManagementServer.PeriodicInformEnable");
				list.add("true");
				setParamMap.put(1, list);
				
				ArrayList<String> list2 = new ArrayList<String>();
				list2.add("Device.ManagementServer.PeriodicInformInterval");
				list2.add("" + value);
				setParamMap.put(2, list2);
				
				sb = CWMPmsg.setParameterValues(setParamMap);
			} else {
				sb = CWMPmsg.getParameterAttributes();
			}
		}
		else if (reqBody.contains("cwmp:SetParameterValuesResponse")) {
			System.out.println("Received SetParameterValuesResponse msg");
			sb = CWMPmsg.getParameterValues();
			setParam=false;
		}
		else if (reqBody.contains("cwmp:GetParameterAttributesResponse")) {
			System.out.println("Received GetParameterAttributesResponse msg");
			// send empty response, close connection
			//response.getWriter().println(sb2);
			Netconf2SoapMediator.connActive = false;
			return;
		}
		else if (reqBody.equals("")) {
			System.out.println("Received HTTP request: Empty");
			//send GetParameterValues
			sb = CWMPmsg.getParameterValues();
		}
		else {
			System.out.println("Received Unknown msg");
			return;}

		System.out.println("Sending HTTP reply:");
		System.out.println(sb);
		response.getWriter().println(sb);
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
