/* Copyright (C) 2018 Pierluigi Greto */

package com.technologies.highstreet.netconf2soapmediator.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class HTTPClient {
	
	

	/***
	 * This function sent to device a request to stimulate the device to begin a session 
	 * @param url to the device. ConnectionRequestURL received from the device
	 * @param username of the device
	 * @param password of the device
	 * @return true if the device received and understood the request. false if not
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public boolean sendOpenConnectionToDevice(String url, String username, String password) {
		boolean status = false;
		HttpGet getArticles = new HttpGet(url);
		URL url_obj;
		try {
			url_obj = new URL(url);
			String host = url_obj.getHost();
			int port = url_obj.getPort();
			Registry<AuthSchemeProvider> authSchemeRegistry =
		            RegistryBuilder.<AuthSchemeProvider>create().register(AuthSchemes.DIGEST,new DigestSchemeFactory()).build();
		    CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		    credentialsProvider.setCredentials(
		            new AuthScope(host, port),
		            new UsernamePasswordCredentials(username,password));

		    CloseableHttpClient client = HttpClients.custom()
		            .setDefaultAuthSchemeRegistry(authSchemeRegistry)
		            .setDefaultCredentialsProvider(credentialsProvider).build();
		    CloseableHttpResponse response = client.execute(getArticles);
	        int status_code = response.getStatusLine().getStatusCode();
	        if( status_code == 200 || status_code == 201) {
	        	status = true;
	        } else {
	        	HTTPServlet.setConnActive(false);
	        }
		} catch (MalformedURLException e1) {
			HTTPServlet.setConnActive(false);
			e1.printStackTrace();
		} catch (ClientProtocolException e) {
			HTTPServlet.setConnActive(false);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}
}
