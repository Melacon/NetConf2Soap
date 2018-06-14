/* Copyright (C) 2018 Pierluigi Greto */

package com.technologies.highstreet.netconf2soapmediator.server;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
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
		int timeout = 3*1000;
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

		    // add a timeout
		    RequestConfig.Builder requestConfig = RequestConfig.custom();
            requestConfig.setConnectTimeout(timeout);
            requestConfig.setConnectionRequestTimeout(timeout);
            requestConfig.setSocketTimeout(timeout);
            getArticles.setConfig(requestConfig.build());
		    
		    CloseableHttpClient client = HttpClients.custom()
		            .setDefaultAuthSchemeRegistry(authSchemeRegistry)
		            .setDefaultCredentialsProvider(credentialsProvider).build();
		    CloseableHttpResponse response = client.execute(getArticles);
	        int status_code = response.getStatusLine().getStatusCode();
	        if( status_code == 200 || status_code == 201) {
	        	status = true;
	        }
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}catch (SocketTimeoutException e) {
			System.out.println("HTTP request timeout");
			HTTPServlet.setConnActive(true);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return status;
	}
}
