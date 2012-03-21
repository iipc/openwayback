package org.archive.wayback.accesscontrol.robotstxt;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
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
	
	private int maxPerRoute = 2;
	private int maxConnections = 100;
		
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
		
		connMan.setDefaultMaxPerRoute(maxPerRoute);
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
	
	@Override
	public void close()
	{
		if (connMan != null) {
			connMan.shutdown();
		}
	}

	@Override
	public void loadRobots(ConnectionCallback callback, String url, String userAgent) {
	
		int status = 0;
		HttpGet httpGet = null;
		
		try {
			httpGet = new HttpGet(url);
			HttpContext context = new BasicHttpContext();
			httpGet.setHeader("User-Agent", userAgent);
			httpGet.setHeader("Connection", "close");

			HttpResponse response = directHttpClient.execute(httpGet, context);

			if (response != null) {
				status = response.getStatusLine().getStatusCode();
			}

			if (callback.supportStatus(status)) {
				HttpEntity entity = response.getEntity();
				int numToRead = (int)entity.getContentLength();		
				String contentType = entity.getContentType().getValue();
				String charset = EntityUtils.getContentCharSet(entity);
				InputStream input = entity.getContent();
				callback.doRead(numToRead, contentType, input, charset);
				input.close();
			}

		} catch (InterruptedException ie) {
			callback.handleException(ie);
			Thread.currentThread().interrupt();
		} catch (Exception exc) {
			callback.handleException(exc);		
		} finally {
			httpGet.abort();
		}
	}

	@Override
	public boolean pingProxyLive(String url) {
		if (proxyHttpClient == null) {
			return false;
		}
		
		HttpHead httpHead = null;
	
		try {
			httpHead = new HttpHead(url);
			proxyHttpClient.execute(httpHead, new BasicHttpContext());
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
