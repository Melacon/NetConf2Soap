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

	private static final long serialVersionUID = 5071770086030271370L;
	private static Netconf2SoapNetworkElement networkElement = null;
	private static boolean connActive = false;
	private static boolean setParam = false;
	private static boolean initSetParam = true;
	private static boolean initSetIPsecParam = true;
	private static boolean initSetFAPParam = true;
	private static boolean FAP = true;
	private static CWMPMessage CWMPmsg = new CWMPMessage();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		System.out.println(HTTPServlet.getBody(request));
		response.getWriter().println("Get Hello World!");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {

		java.util.Date date = new java.util.Date();
		System.out.println(date + " Received msg from device");

		final String reqBody = HTTPServlet.getBody(request);
		StringBuilder sb = new StringBuilder(10);
		
		Netconf2SoapNetworkElement.printStringDocument(reqBody);

		if (reqBody.contains("CWMP fault")) {
			System.out.println("Received Fault msg");
			System.out.println("Sending empty reply:");
			setConnActive(false);
		}
		else if (reqBody.contains("cwmp:Inform")) {
			sb = handleInform(reqBody);
		}
		else if (reqBody.contains("cwmp:GetParameterValuesResponse")) {
			sb = handleGetParameterValuesResponse(reqBody);
		}
		else if (reqBody.contains("cwmp:SetParameterValuesResponse")) {
			sb = handleSetParameterValuesResponse(reqBody);
		}
		else if (reqBody.contains("cwmp:GetParameterAttributesResponse")) {
			sb = handleGetParameterAttributesResponse(reqBody);
			System.out.println("Sending empty reply:");
		}
		else if (reqBody.equals("")) {
			sb = handleEmptyResponse(reqBody);
		}
		else {
			System.out.println("Received Unknown msg");
		}

		date = new java.util.Date();
		System.out.println(date + " Sending HTTP reply:");
		System.out.println(sb);
		response.getWriter().print(sb);
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

	public static  StringBuilder handleEmptyResponse(String reqBody) {
		System.out.println("Received HTTP request: Empty");

		// an empty response means, that the device is ready to accept GET/SET messages

		StringBuilder sb = new StringBuilder(10);

		if (getInitSetParam() == true) {
			// when the device connects, SET specific device parameters to initialize the device
			CWMPMessage.initSetParamList();
			sb = CWMPmsg.setParameterValues(CWMPMessage.getSetParamList());
			CWMPMessage.clearSetParamList();
			setInitSetParam(false);
			return sb;
		}

		if (getSetParam() == true) {
			// send parameters that have been modified via NETCONF
			sb = CWMPmsg.setParameterValues(CWMPMessage.getSetParamList());
			CWMPMessage.clearSetParamList();
			setSetParam(false);
		} else {
			// start: get Device parameters
			sb = CWMPmsg.getParameterValues(false);
			FAP = true;
		}

		return sb;
	}

	public static  StringBuilder handleSetParameterValuesResponse(String reqBody) {
		System.out.println("Received SetParameterValuesResponse msg");

		StringBuilder sb = new StringBuilder(10);

		// get Device parameters
		sb = CWMPmsg.getParameterValues(false);
		FAP = true;

		return sb;
	}

	public static  StringBuilder handleGetParameterValuesResponse(String reqBody) {
		System.out.println("Received GetParameterValuesResponse msg");

		// parameters contained in the inform message are pushed to the local xml
		networkElement.setTr069DocumentCFromString(reqBody);
				
		StringBuilder sb = new StringBuilder(10);

		if (getInitSetIPsecParam() == true) {
			// when the device connects, SET specific FAP parameters to initialize the RF part
			CWMPMessage.initSetIPsecParamList();
			sb = CWMPmsg.setParameterValues(CWMPMessage.getSetParamList());
			CWMPMessage.clearSetParamList();
			setInitSetIPsecParam(false);
			return sb;
		}

		if (getInitSetFAPParam() == true) {
			// when the device connects, SET specific IPsec parameter
			CWMPMessage.initSetFAPParamList();
			sb = CWMPmsg.setParameterValues(CWMPMessage.getSetParamList());
			CWMPMessage.clearSetParamList();
			setInitSetFAPParam(false);
			return sb;
		}
		
		if (FAP == true) {
			// get FAP parameters
			sb = CWMPmsg.getParameterValues(true);
			FAP = false;
		} else {
			sb = CWMPmsg.getParameterAttributes();
			FAP = true;
		}

		try {	
			Thread.sleep(3 * 1000); // milliseconds
		} catch (Exception e) {
			System.out.println("Error in sleep" + e);
		}
		
		return sb;
	}

	public static  StringBuilder handleGetParameterAttributesResponse(String reqBody) {
		System.out.println("Received GetParameterAttributesResponse msg");

		StringBuilder sb = new StringBuilder(10);
		// get Device parameters
		//sb = CWMPmsg.getParameterValues(false);

		setConnActive(false);
		
		try {	
			Thread.sleep(3 * 1000); // milliseconds
		} catch (Exception e) {
			System.out.println("Error in sleep" + e);
		}
		
		return sb;
	}

	public static  StringBuilder handleInform(String reqBody) {
		if (reqBody.contains("<EventCode>0 BOOTSTRAP")) {
			setInitSetParam(true);
			setInitSetIPsecParam(true);
			setInitSetFAPParam(true);
			System.out.println("Received Inform msg with event code: 0 (BOOTSTRAP)");
		}
		else if (reqBody.contains("<EventCode>1 BOOT")) {
			setInitSetParam(true);
			if (reqBody.contains("<EventCode>2 PERIODIC")) {
				System.out.println("Received Inform msg with event code: 1 (BOOT PERIODIC REQUEST)");
			} else {
				System.out.println("Received Inform msg with event code: 1 (BOOT)");
			}
		}
		else if (reqBody.contains("<EventCode>2 PERIODIC")) {
			System.out.println("Received Inform msg with event code: 2 (PERIODIC REQUEST)");
		}
		else if (reqBody.contains("<EventCode>6 CONNECTION REQUEST")) {
			System.out.println("Received Inform msg with event code: 6 (CONNECTION REQUEST)");
		}
		else if (reqBody.contains("<EventCode>4 VALUE CHANGE")) {
			System.out.println("Received Inform msg with event code: 4 (VALUE CHANGE)");
		}
		else {
			System.out.println("Received Inform msg (unknown event code)");
		}

		// parameters contained in the inform message are pushed to the local xml
		networkElement.setTr069DocumentCFromString(reqBody);

		StringBuilder sb = new StringBuilder(10);
		sb = CWMPmsg.getInformResponse();

		return sb;
	}

	public static Netconf2SoapNetworkElement getNetworkElement() {
		return networkElement;
	}

	public static void setNetworkElement(Netconf2SoapNetworkElement networkElement) {
		HTTPServlet.networkElement = networkElement;
	}

	public static boolean getSetParam() {
		return setParam;
	}

	public static void setSetParam(boolean boolo) {
		setParam = boolo;
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		String function2 = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println(function + " " + function2 + " " + boolo);
	}

	public static boolean getConnActive() {
		return connActive;
	}

	public static void setConnActive(boolean boolo) {
		connActive = boolo;
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		String function2 = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println(function + " " + function2 + " " + boolo);
	}
	
	public static boolean getInitSetParam() {
		return initSetParam;
	}

	public static void setInitSetParam(boolean init) {
		initSetParam = init;
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		String function2 = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println(function + " " + function2 + " " + init);
	}
	
	public static boolean getInitSetIPsecParam() {
		return initSetIPsecParam;
	}

	public static void setInitSetIPsecParam(boolean init) {
		initSetIPsecParam = init;
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		String function2 = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println(function + " " + function2 + " " + init);
	}
	
	public static boolean getInitSetFAPParam() {
		return initSetFAPParam;
	}

	public static void setInitSetFAPParam(boolean init) {
		initSetFAPParam = init;
		String function = Thread.currentThread().getStackTrace()[1].getMethodName();
		String function2 = Thread.currentThread().getStackTrace()[2].getMethodName();
		System.out.println(function + " " + function2 + " " + init);
	}
}
