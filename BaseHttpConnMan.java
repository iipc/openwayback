package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

abstract public class BaseHttpConnMan {
	
	protected int connectionTimeoutMS = 5000;
	protected int readTimeoutMS = 5000;
	protected int pingConnectTimeoutMS = 500;
	protected int dnsTimeoutMS = 0;
	
	protected int maxPerRouteConnections = 4;
	protected int maxConnections = 500;
		
	protected String proxyHost;
	protected int proxyPort;

	interface ConnectionCallback
	{
		boolean supportStatus(int status);

		void doRead(int length, String contentType, InputStream input, String charset) throws IOException, InterruptedException;

		void handleException(Exception exc);
	}
	
	BaseHttpConnMan()
	{
		
	}
	
	BaseHttpConnMan(String proxyHostPort)
	{
		this.setProxyHostPort(proxyHostPort);
	}
	
	public void setConnectionTimeout(int connectionTimeoutMS)
	{
		this.connectionTimeoutMS = connectionTimeoutMS;
	}
	
	public void setPingConnectTimeout(int pingConnectTimeoutMS)
	{
		this.pingConnectTimeoutMS = pingConnectTimeoutMS;
	}
	
	public void setSocketReadTimeoutMS(int readTimeoutMS)
	{
		this.readTimeoutMS = readTimeoutMS;
	}
	
	public void setDNSTimeoutMS(int dnsTimeoutMS)
	{
		this.dnsTimeoutMS = dnsTimeoutMS;
	}
	
	public void setMaxPerRouteConnections(int maxPerRouteConnections) {
		this.maxPerRouteConnections = maxPerRouteConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	abstract public void init();
	
	abstract public void loadRobots(ConnectionCallback callback, String url, String userAgent) throws InterruptedException;
	
	public void loadProxyLive(ConnectionCallback callback, String url, String userAgent) throws InterruptedException {}

	abstract public boolean pingProxyLive(String url);
	
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if (colonIdx > 0) {
    		proxyHost = hostPort.substring(0,colonIdx);
    		proxyPort = Integer.valueOf(hostPort.substring(colonIdx+1));   		
    	}
    }

	public void close() {
		
	}

	public void idleCleanup() {
		
	}

	public void appendLogInfo(PrintWriter info) {
		
	}
}
