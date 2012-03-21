package org.archive.wayback.accesscontrol.robotstxt;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ClientConnectionOperator;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
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
	
	private int dnsTimeoutMS = 5000;
	
	private DefaultHttpClient directHttpClient;
	private DefaultHttpClient proxyHttpClient;
	
	@Override
	public void init() {
		connMan = new ThreadSafeClientConnManager()
		{
			@Override
			protected ClientConnectionOperator createConnectionOperator(
					SchemeRegistry schreg) {
				return new TimedDNSConnectionOperator(schreg, maxConnections, dnsTimeoutMS);
			}
		};		
		
		connMan.setDefaultMaxPerRoute(maxPerRoute);
		connMan.setMaxTotal(maxConnections);
		
		HttpRequestRetryHandler retryHandler = new DefaultHttpRequestRetryHandler(0, false);

		BasicHttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, readTimeoutMS);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
		params.setParameter(CoreConnectionPNames.SO_LINGER, 0);
		params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
		
		directHttpClient = new DefaultHttpClient(connMan, params);
		directHttpClient.setHttpRequestRetryHandler(retryHandler);
		directHttpClient.setReuseStrategy(new NoConnectionReuseStrategy());
		
		BasicHttpParams proxyParams  = new BasicHttpParams();
		proxyParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, pingConnectTimeoutMS);
		proxyParams.setParameter(CoreConnectionPNames.SO_LINGER, 0);
		proxyParams.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
		
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
			//LOGGER.info("HTTP CONNECTIONS: " + connMan.getConnectionsInPool());
		} catch (InterruptedException ie) {
			callback.handleException(ie);
			Thread.currentThread().interrupt();
		} catch (Exception exc) {
			callback.handleException(exc);
							
			//LOGGER.info("HTTP CONNECTIONS: " + connMan.getConnectionsInPool());			
//			PerformanceLogger.noteElapsed("HttpLoadFail", System.currentTimeMillis() - startTime, 
//					"Exception: " + exc + " url: " + url + " status " + status);

			//LOGGER.info("Exception: " + exc + " url: " + url + " status " + status);		
		
		} finally {
			httpGet.abort();
			this.connMan.closeIdleConnections(connectionTimeoutMS / 2, TimeUnit.MILLISECONDS);
//			PerformanceLogger.noteElapsed("HttpLoadRobots", System.currentTimeMillis() - startTime, url + " " + status + ((contents != null) ? " Size: " + contents.length() : " NULL"));
		}
	}

	@Override
	public boolean pingProxyLive(String url) {
		if (proxyHttpClient == null) {
			return false;
		}
		
		HttpHead httpHead = null;
		//long startTime = System.currentTimeMillis();
	
		try {
			httpHead = new HttpHead(url);
			proxyHttpClient.execute(httpHead, new BasicHttpContext());
//			PerformanceLogger.noteElapsed("PingProxyRobots", System.currentTimeMillis() - startTime, url + " " + response.getStatusLine());
			return true;
		} catch (Exception exc) {
			httpHead.abort();
//			PerformanceLogger.noteElapsed("PingProxyFailure", System.currentTimeMillis() - startTime, url + " " + exc);
			return false;
		} finally {
			//httpHead.abort();
		}
	}

}
