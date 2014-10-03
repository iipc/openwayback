package org.archive.wayback.liveweb;


import java.net.SocketTimeoutException;

import junit.framework.TestCase;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.util.ByteOp;

public class URLtoARCCacherTest extends TestCase {
	public void testSocketTimeout() throws Exception {
	    MultiThreadedHttpConnectionManager connectionManager = null;
	    HostConfiguration hostConfiguration = null;
	    HttpClient http = null;
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
    	http = new HttpClient(connectionManager);
    	http.setHostConfiguration(hostConfiguration);
		HttpMethod method = null;
//		String urlString = "http://wayback.archive-it.org:6100/one";
		String urlString = "http://hello.com/one";
		int socketTimeoutMS = 10;
		int connectTimeoutMS = 100;
    	connectionManager.getParams().setSoTimeout(socketTimeoutMS);
    	connectionManager.getParams().setConnectionTimeout(connectTimeoutMS);
		try {
			method = new GetMethod(urlString);
		} catch(IllegalArgumentException e) {
			throw new LiveDocumentNotAvailableException("Url:" + urlString +
					"does not look like an URL?");
		}
	    try {
	    	int status = http.executeMethod(method);
	    	System.out.println("Got response code: " + status);
	    	ByteOp.copyStream(method.getResponseBodyAsStream(), System.out);
	    } catch (SocketTimeoutException e) {
	    	// OK
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }

	}
}
