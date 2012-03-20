package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.io.InputStream;

abstract public class BaseHttpConnMan {
	
	protected int connectionTimeoutMS;
	protected int readTimeoutMS;
	protected int pingConnectTimeoutMS;
	
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
	
	abstract public void init();
	
	abstract public void loadRobots(ConnectionCallback callback, String url, String userAgent);

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
}
