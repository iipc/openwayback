package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class ApacheHttpConnMan extends BaseHttpConnMan {

	private ThreadSafeClientConnManager connMan;
			
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
	
	@Override
	public void init() {
		
		dnsLookup = new TimedDNSLookup(maxConnections, dnsTimeoutMS);
		
		connMan = new ThreadSafeClientConnManager()
		{
			@Override
			protected ClientConnectionOperator createConnectionOperator(
					SchemeRegistry schreg) {
				return new TimedDNSConnectionOperator(schreg);
			}
		};
		
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
		
		directHttpClient = new DefaultHttpClient(connMan, params);
		directHttpClient.setHttpRequestRetryHandler(retryHandler);
		directHttpClient.setReuseStrategy(new NoConnectionReuseStrategy());
		
		if ((proxyHost == null) || (proxyPort == 0)) {
			return;
		}
		
		BasicHttpParams proxyParams  = new BasicHttpParams();
		proxyParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
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
	
	@Override
	public void close()
	{
		if (connMan != null) {
			connMan.shutdown();
		}
	}

	@Override
	public void loadRobots(ConnectionCallback callback, String url, String userAgent) {
		load(directHttpClient, callback, url, userAgent, false);
	}
	
	protected void load(HttpClient client, ConnectionCallback callback, String url, String userAgent, boolean keepAlive)
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
			Thread.currentThread().interrupt();
			keepAlive = false;
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
	
	@Override
	public void loadProxyLive(ConnectionCallback callback, String url, String userAgent) {
		if (proxyHttpClient == null) {
			return;
		}
		
		load(proxyHttpClient, callback, url, userAgent, true);
	}

	@Override
	public boolean pingProxyLive(String url) {
		if (proxyHttpClient == null) {
			return false;
		}
		
		HttpHead httpHead = null;
	
		try {
			HttpContext context = new BasicHttpContext();
			httpHead = new HttpHead(url);
			httpHead.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, pingConnectTimeoutMS);
			proxyHttpClient.execute(httpHead, context);
			return true;
		} catch (Exception exc) {
			httpHead.abort();
			return false;
		}
	}
	
	@Override
	public void idleCleanup()
	{
        connMan.closeIdleConnections(2 * (readTimeoutMS + connectionTimeoutMS), TimeUnit.MILLISECONDS);
 	}
	
	@Override
	public void appendLogInfo(PrintWriter info)
	{
	   info.println("Connections: " + connMan.getConnectionsInPool());
	   info.println("DNS Active: " + ((ThreadPoolExecutor)dnsLookup.getExecutor()).getActiveCount());
	}
}
