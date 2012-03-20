package org.archive.wayback.accesscontrol.robotstxt;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Logger;

public class JavaHttpConnMan extends BaseHttpConnMan {
	
	private final static Logger LOGGER = Logger
	.getLogger(JavaHttpConnMan.class.getName());
		
	Proxy proxy;

	@Override
	public void init()
	{
		if ((proxyHost != null) && (proxyPort != 0)) {
			LOGGER.info("=== HTTP Proxy through: " + proxyHost + ":" + proxyPort);
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
		}
		
		HttpURLConnection.setFollowRedirects(true);
	}
	
	private Integer httpConnectionCount = 0;
	
	@Override
	public void loadRobots(ConnectionCallback callback, String url, String userAgent)
	{
		int status = 0;
		HttpURLConnection connection = null;
		InputStream input = null;
		URL theURL;
				
		synchronized(httpConnectionCount) {
			httpConnectionCount++;
		}
		
		try {
			theURL = new URL(url);
			connection = (HttpURLConnection)theURL.openConnection();
			connection.setConnectTimeout(connectionTimeoutMS);
			connection.setReadTimeout(readTimeoutMS);
			connection.setRequestProperty("Connection", "close");
			if (userAgent != null) {
				connection.setRequestProperty("User-Agent", userAgent);
			}
			connection.connect();

			status = connection.getResponseCode();
						
			if (callback.supportStatus(status)) {
				String contentType = connection.getContentType();
				int length = connection.getContentLength();
				input = connection.getInputStream();
				String charset = connection.getContentEncoding();
				callback.doRead(length, contentType, input, charset);
			}
			
			//PerformanceLogger.noteElapsed("HttpLoadSuccess", System.currentTimeMillis() - startTime, url + " " + status + ((contents != null) ? " Size: " + contents.length() : " NULL"));
			
		} catch (Exception exc) {
			
			callback.handleException(exc);
			
//			PerformanceLogger.noteElapsed("HttpLoadFail", System.currentTimeMillis() - startTime, 
//					"Exception: " + exc + " url: " + url + " status " + status);			
		} finally {
			synchronized(httpConnectionCount) {
				LOGGER.info("HTTP CONNECTIONS: " + httpConnectionCount--);
			}
			
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (connection != null) {
				connection.disconnect();
			}
		}
	}
	
	@Override
	public boolean pingProxyLive(String url) {
		if (proxy == null) {
			return false;
		}
		
		HttpURLConnection connection = null;
		URL theURL;
		
		synchronized(httpConnectionCount) {
			httpConnectionCount++;
		}
		
		try {
			theURL = new URL(url);
			connection = (HttpURLConnection)theURL.openConnection(proxy);
			connection.setConnectTimeout(pingConnectTimeoutMS);
			connection.setRequestProperty("Connection", "close");
			connection.setRequestMethod("HEAD");
			connection.connect();
//			PerformanceLogger.noteElapsed("PingProxySuccess", System.currentTimeMillis() - startTime, url + " " + connection.getResponseMessage());
		} catch (Exception exc) {
//			PerformanceLogger.noteElapsed("PingProxyFailure", System.currentTimeMillis() - startTime, url + " " + exc);
		} finally {
			synchronized(httpConnectionCount) {
				LOGGER.info("HTTP CONNECTIONS: " + httpConnectionCount--);
			}
			
			if (connection != null) {
				connection.disconnect();
			}
		}
		
		return true;
	}
}
