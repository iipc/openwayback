package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class ApacheHttpConnMan {

	private final static Logger LOGGER = Logger
	.getLogger(ApacheHttpConnMan.class.getName());
	
	private ConnManager connMan;
			
	private DefaultHttpClient directHttpClient;
	private DefaultHttpClient proxyHttpClient;
		
	private TimedDNSLookup dnsLookup;
	
	class TimedDNSConnectionOperator extends DefaultClientConnectionOperator
	{
		public TimedDNSConnectionOperator(SchemeRegistry schemes) {
			super(schemes);
		}
		
		@Override
		protected InetAddress[] resolveHostname(String host)
				throws UnknownHostException {
			return dnsLookup.resolveHostname(host);
		}
	}
	
	class ConnManager extends ThreadSafeClientConnManager
	{
		@Override
		protected ClientConnectionOperator createConnectionOperator(
				SchemeRegistry schreg) {
			if (dnsTimeoutMS > 0) {
				return new TimedDNSConnectionOperator(schreg);
			} else {
				return super.createConnectionOperator(schreg);
			}
		}
		
		public void deleteClosedConnections()
		{
			if (pool != null) {
				pool.deleteClosedConnections();
			}
		}
	};
	
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
	
	public void init() {
		
		if (dnsTimeoutMS > 0) {
			dnsLookup = new TimedDNSLookup(maxConnections, dnsTimeoutMS);
		}
		
		connMan = new ConnManager();
		
		connMan.setDefaultMaxPerRoute(maxPerRouteConnections);
		connMan.setMaxTotal(maxConnections);
		
		HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);
		
		BasicHttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeoutMS);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
		params.setParameter(CoreConnectionPNames.SO_LINGER, 0);
		params.setParameter(CoreConnectionPNames.SO_REUSEADDR, true);
		params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
		params.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
		params.setParameter(ClientPNames.HANDLE_REDIRECTS, true);
		
		directHttpClient = new DefaultHttpClient(connMan, params);
		directHttpClient.setHttpRequestRetryHandler(retryHandler);
		directHttpClient.setReuseStrategy(new NoConnectionReuseStrategy());
		
		// Only follow redirects that look like they are still robots.txt
		directHttpClient.setRedirectStrategy(new DefaultRedirectStrategy()
		{

			@Override
			public boolean isRedirected(HttpRequest request,
					HttpResponse response, HttpContext context)
					throws ProtocolException {
				
				boolean redirect = super.isRedirected(request, response, context);
				
				if (redirect) {
					Header locationHeader = response.getFirstHeader("location");
					if (locationHeader != null) {
						String newLoc = locationHeader.getValue();
						String oldLoc = request.getRequestLine().getUri();
						//context.setAttribute(HAS_REDIRECTED, Boolean.TRUE);
						if (newLoc.endsWith("robots.txt")) {
							LOGGER.info("REDIRECT FOLLOW: " + oldLoc + " => " + newLoc);
							return true;
						} else {
							LOGGER.info("REDIRECT IGNORE: " + oldLoc + " => " + newLoc);
						}
					}
				}
				
				return false;
			}
			
		});
		
		if ((proxyHost == null) || (proxyPort == 0)) {
			return;
		}
		
		BasicHttpParams proxyParams  = new BasicHttpParams();
		proxyParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, pingConnectTimeoutMS);
		proxyParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, pingConnectTimeoutMS);
		proxyParams.setParameter(CoreConnectionPNames.SO_LINGER, 0);
		proxyParams.setParameter(CoreConnectionPNames.SO_REUSEADDR, true);
		proxyParams.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
		proxyParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
		
		HttpHost proxy = new HttpHost(proxyHost, proxyPort);
		proxyParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		
		proxyHttpClient = new DefaultHttpClient(connMan, proxyParams);
		proxyHttpClient.setHttpRequestRetryHandler(retryHandler);
		proxyHttpClient.setReuseStrategy(new DefaultConnectionReuseStrategy());
	}
	
	public void close()
	{
		if (connMan != null) {
			connMan.shutdown();
		}
	}

	public void loadRobots(ConnectionCallback callback, String url, String userAgent) throws InterruptedException {
		load(directHttpClient, callback, url, userAgent, false);
	}
	
	protected void load(HttpClient client, ConnectionCallback callback, String url, String userAgent, boolean keepAlive) throws InterruptedException
	{
		int status = 0;
		HttpGet httpGet = null;
		
		try {
			httpGet = new HttpGet(url);
			HttpContext context = new BasicHttpContext();
			httpGet.setHeader("User-Agent", userAgent);
			
			if (!keepAlive) {
				httpGet.setHeader("Connection", "close");
			}

			HttpResponse response = client.execute(httpGet, context);

			if (response != null) {
				status = response.getStatusLine().getStatusCode();
			}

			if (callback.supportStatus(status)) {
				HttpEntity entity = response.getEntity();
				int numToRead = (int)entity.getContentLength();
				String contentType = null;
				Header header = entity.getContentType();
				if (header != null) {
					contentType = header.getValue();
				}
				String charset = EntityUtils.getContentCharSet(entity);
				InputStream input = entity.getContent();
				callback.doRead(numToRead, contentType, input, charset);
			}

		} catch (InterruptedException ie) {
			callback.handleException(ie);
			keepAlive = false;
			throw ie;
		} catch (IOException e) {
			callback.handleException(e);
			keepAlive = false;
		} catch (Exception other) {
			callback.handleException(other);
			other.printStackTrace();
			keepAlive = false;
		} finally {
			if (!keepAlive) {
				httpGet.abort();
			}
		}
	}
	
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if (colonIdx > 0) {
    		proxyHost = hostPort.substring(0,colonIdx);
    		proxyPort = Integer.valueOf(hostPort.substring(colonIdx+1));   		
    	}
    }
	
	public void loadProxyLive(ConnectionCallback callback, String url, String userAgent) throws InterruptedException {
		if (proxyHttpClient == null) {
			return;
		}
		
		load(proxyHttpClient, callback, url, userAgent, true);
	}

	public boolean pingProxyLive(String url) {
		if (proxyHttpClient == null) {
			return false;
		}
		
		HttpHead httpHead = null;
	
		try {
			HttpContext context = new BasicHttpContext();
			httpHead = new HttpHead(url);
//			httpHead.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, pingConnectTimeoutMS);
//			httpHead.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, pingConnectTimeoutMS);
			proxyHttpClient.execute(httpHead, context);
			return true;
		} catch (Exception exc) {
			httpHead.abort();
			return false;
		}
	}
	
	public void idleCleanup()
	{
        connMan.closeIdleConnections(2 * (readTimeoutMS + connectionTimeoutMS), TimeUnit.MILLISECONDS);
        connMan.closeExpiredConnections();
        connMan.deleteClosedConnections();
 	}
	
	public void appendLogInfo(PrintWriter info)
	{
	   info.println("  Connections: " + connMan.getConnectionsInPool());
	   
	   if (dnsLookup != null) {
		   info.println("  DNS Active: " + ((ThreadPoolExecutor)dnsLookup.getExecutor()).getActiveCount());
	   }
	}
}
