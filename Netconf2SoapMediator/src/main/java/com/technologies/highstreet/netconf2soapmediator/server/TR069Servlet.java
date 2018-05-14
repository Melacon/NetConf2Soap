package com.technologies.highstreet.netconf2soapmediator.server;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.freeacs.tr069.Namespace;


public class TR069Servlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5071770086030271370L;
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		System.out.println(this.getBody(request));
		
		StringBuilder sb = new StringBuilder(10);
		sb.append("<soapenv:Envelope ");
		sb.append("xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" ");
		sb.append("xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" ");
		sb.append("xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" ");
		sb.append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		sb.append("xmlns:cwmp=\"urn:dslforum-org:cwmp-1-0\">\n");
		sb.append("<soap_env:Header>\n" + "  </soap_env:Header>");
		sb.append("\t\t<cwmp:InformResponse>\n\t\t\t<MaxEnvelopes>1</MaxEnvelopes>\n\t\t</cwmp:InformResponse>\n");
		sb.append("</soapenv:Envelope>\n");
		response.getWriter().println(sb);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		System.out.println(this.getBody(request));
		response.getWriter().println("Post Hello World!");
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

}
