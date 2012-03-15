package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.webapp.PerformanceLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisRobotsCache implements LiveWebCache {

	private final static Logger LOGGER = Logger
			.getLogger(RedisRobotsCache.class.getName());
	
	/* ROBOTS TTL PARAMS */
	
	final static int ONE_DAY = 60 * 60 * 24;

	private int totalTTL = ONE_DAY * 10;
	private int refreshTTL = ONE_DAY;

	private int notAvailTotalTTL = ONE_DAY * 2;
	private int notAvailRefreshTTL = ONE_DAY / 2;
	
	final static String ROBOTS_TOKEN_EMPTY = "0_ROBOTS_EMPTY";
	
	final static String ROBOTS_TOKEN_ERROR = "0_ROBOTS_ERROR-";
	final static String ROBOTS_TOKEN_ERROR_UNKNOWN = "0_ROBOTS_ERROR-0";
	
	final static String UPDATE_QUEUE_KEY = "robots_update_queue";

	final static int LIVE_OK = 200;
	final static int LIVE_TIMEOUT_ERROR = 900;
	final static int LIVE_HOST_ERROR = 910;
	final static int LIVE_INVALID_TYPE_ERROR = 920;
	
	
	final static int MAX_ROBOTS_SIZE = 500000;
	
	/* SOCKET SETTINGS / PARAMS */

	private int connectionTimeoutMS = 5000;
	private int socketTimeoutMS = 5000;
	
	private int maxConnections = 1500;
	private int maxPerRoute = 20;
	
	private String proxyHost;
	private int proxyPort;
	
	private ThreadSafeClientConnManager connMan;

//	private HttpClient directHttpClient;
//	private HttpClient proxyHttpClient;
	Proxy proxy;
	
	/* THREAD WORKER SETTINGS */
	
	private int maxNumUpdateThreads = 1000;
	private int maxCoreUpdateThreads = 75;
	
	private int threadKeepAliveTime = 5000;
	
//	private int maxWorkQueueSize = 500;
	
	private ThreadPoolExecutor refreshService;
//	private HashSet<String> urlsToRefresh;
	
	//private ThreadPoolExecutor loadService;
	private Map<String, UrlLoader> urlsToLoad;
	
	/* REDIS */
	private RedisConnectionManager redisConn;

	public RedisRobotsCache(RedisConnectionManager redisConn) {
		LOGGER.setLevel(Level.FINER);

		this.redisConn = redisConn;

//		connMan = new ThreadSafeClientConnManager();
//		connMan.setDefaultMaxPerRoute(maxPerRoute);
//		connMan.setMaxTotal(maxConnections);
//
//		BasicHttpParams params = new BasicHttpParams();
//		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeoutMS);
//		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
//		params.setParameter(CoreConnectionPNames.SO_LINGER, 0);
//		params.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
//		directHttpClient = new DefaultHttpClient(connMan, params);
		
		refreshService = new ThreadPoolExecutor(maxCoreUpdateThreads, maxNumUpdateThreads, threadKeepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());		
//		urlsToRefresh = new HashSet<String>();
		
		//loadService = new ThreadPoolExecutor(maxCoreUpdateThreads, maxNumUpdateThreads, threadKeepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());		
		urlsToLoad = new HashMap<String, UrlLoader>();
	}
	
	public void setMaxTotalConnections(int max)
	{
		connMan.setMaxTotal(max);
	}
	
	public int getMaxTotalConnections()
	{
		return connMan.getMaxTotal();
	}
	
	public void setMaxPerRoute(int max)
	{
		connMan.setDefaultMaxPerRoute(max);
	}
	
	public int getMaxPerRoute()
	{
		return connMan.getDefaultMaxPerRoute();
	}
	
	public RedisRobotsCache(RedisConnectionManager redisConn, String proxyHostPort) {

		this(redisConn);
		this.setProxyHostPort(proxyHostPort);
		
		if ((proxyHost != null) && (proxyPort != 0)) {
//			HttpParams proxyParams  = new BasicHttpParams();
//			proxyParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeoutMS);
//			proxyParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
//			proxyParams.setParameter(CoreConnectionPNames.SO_LINGER, 0);
//			proxyParams.setParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, true);
//			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
//			proxyParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			LOGGER.info("=== HTTP Proxy through: " + proxyHost + ":" + proxyPort);
			proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
//			proxyHttpClient = new DefaultHttpClient(connMan, proxyParams);
		}
	}

	public Resource getCachedResource(URL urlURL, long maxCacheMS,
				boolean bUseOlder) throws LiveDocumentNotAvailableException,
				LiveWebCacheUnavailableException, LiveWebTimeoutException,
				IOException {
		return getCachedResource(urlURL.toExternalForm());
	}
	
	public Resource getCachedResource(String url) throws LiveDocumentNotAvailableException, LiveWebCacheUnavailableException {
				
		String robotsFile = null;
		Jedis jedis = null;
		long startTime = 0;
		
		try {
			jedis = redisConn.getJedisInstance();
	
			startTime = System.currentTimeMillis();
			robotsFile = jedis.get(url);
			PerformanceLogger.noteElapsed("RedisGet", System.currentTimeMillis() - startTime, ((robotsFile == null) ? "REDIS MISS: " : "REDIS HIT: ") + url);
		} catch (JedisConnectionException jce) {
			LOGGER.severe("Jedis Exception: " + jce);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
		}
		
		return processSingleRobots(jedis, url, robotsFile);
	}
	
	
	public Resource getCachedResource(String[] urls) throws LiveDocumentNotAvailableException,
			LiveWebCacheUnavailableException, LiveWebTimeoutException,
			IOException {
				
		List<String> robotsFiles = null;
		Jedis jedis = null;
		long startTime = 0;
		
		try {
			jedis = redisConn.getJedisInstance();
	
			startTime = System.currentTimeMillis();
			robotsFiles = jedis.mget(urls);
			PerformanceLogger.noteElapsed("RedisGet", System.currentTimeMillis() - startTime, "# URLS" + urls.length);
		} catch (JedisConnectionException jce) {
			LOGGER.severe("Jedis Exception: " + jce);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
		}
		
		int count = 0;
		String theUrl = null;
		String theRobots = null;
		
		for (String robots : robotsFiles) {
			if (theRobots == null) {
				theRobots = robots;
				theUrl = urls[count];
			} else if (theRobots.startsWith(ROBOTS_TOKEN_ERROR) && !robots.startsWith(ROBOTS_TOKEN_ERROR)) {
				theRobots = robots;
				theUrl = urls[count];			
			}
			count++;
		}
		
		return processSingleRobots(jedis, theUrl, theRobots);
	}
	
	protected Resource processSingleRobots(Jedis jedis, String url, String robotsFile) throws LiveWebCacheUnavailableException, LiveDocumentNotAvailableException
	{		
		try {			
			if (robotsFile == null) {
				redisConn.returnJedisInstance(jedis);
				jedis = null;

				RobotResponse robotResponse = doSyncUpdate(url);
												
				if ((robotResponse == null) || !robotResponse.isValid()) {
					throw new LiveDocumentNotAvailableException("Error Loading Live Robots");	
				}
				
				robotsFile = robotResponse.contents;
				
			} else {
				
				if (isExpired(jedis, url, robotsFile)) {
					
//					boolean pushKey = false;
//					synchronized(urlsToRefresh) {
//						if (!urlsToRefresh.contains(url)) {
//							urlsToRefresh.add(url);
//							pushKey = true;
//						}
//					}
					
//					if (pushKey) {
						jedis.rpush(UPDATE_QUEUE_KEY, url);
//					}
//					refreshService.submit(new CacheUpdateTask(url, current));
				}
				
				redisConn.returnJedisInstance(jedis);
				jedis = null;
				
				if (robotsFile.startsWith(ROBOTS_TOKEN_ERROR)) {
					throw new LiveDocumentNotAvailableException("Robots Error: " + robotsFile);	
				} else if (robotsFile.equals(ROBOTS_TOKEN_EMPTY)) {
					robotsFile = "";
				}
			}				
			
		} catch (JedisConnectionException jce) {
			LOGGER.severe("Jedis Exception: " + jce);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
			if (robotsFile != null) {
				return new RobotsTxtResource(robotsFile);
			}
			throw new LiveWebCacheUnavailableException(jce.toString());
		} finally {
			redisConn.returnJedisInstance(jedis);
		}

		return new RobotsTxtResource(robotsFile);
	}
			
	public boolean isExpired(Jedis jedis, String url, String contents) {
		
		long ttl = jedis.ttl(url);
		
		int maxTime, refreshTime;
		
		if (contents.startsWith(ROBOTS_TOKEN_ERROR)) {
			maxTime = notAvailTotalTTL;
			refreshTime = notAvailRefreshTTL;
		} else {
			maxTime = totalTTL;
			refreshTime = refreshTTL;
		}
		
		if ((maxTime - ttl) >= refreshTime) {
			LOGGER.info("Queuing robot refresh: "
					+ (maxTime - ttl) + ">=" + refreshTime + " " + url);
			
			return true;
		}
		
		return false;
	}
	
	protected void processRedisUpdateQueue()
	{
		Jedis jedis = null;
		ExecutorService mainLoopService = null;
		
		try {			
			mainLoopService = Executors.newFixedThreadPool(maxCoreUpdateThreads);
			
			while (true) {
				try {
					if (jedis == null) {
						jedis = redisConn.getJedisInstance();
					}
					
					List<String> urls = jedis.blpop(0, UPDATE_QUEUE_KEY);
					String url = urls.get(1);
					
//					synchronized(urlsToRefresh) {
//						if (urlsToRefresh.contains(url)) {
//							continue;
//						}
//						urlsToRefresh.add(url);
//					}
					
					mainLoopService.execute(new URLRequestTask(url));
					
	//				String current = jedis.get(url);
	//				
	//				if (current == null) {
	//					LOGGER.info("NULL VALUE FOR: " + url);
	//				}
	//				
	//				if ((current != null) && !isExpired(jedis, url, current)) {
	//					continue;
	//				}
	//				
	//				refreshService.submit(new CacheUpdateTask(url, current));
					
					Thread.sleep(1);
				} catch (JedisConnectionException jce) {
					redisConn.returnBrokenJedis(jedis);
					jedis = null;
				} catch (InterruptedException e) {
					
				}
			}
		} finally {
			redisConn.returnJedisInstance(jedis);
			mainLoopService.shutdown();
		}
	}
	
	class UrlLoader
	{
		CountDownLatch latch = new CountDownLatch(1);
		RobotResponse response;
	}
	
	
	private RobotResponse doSyncUpdate(String url)
	{
		UrlLoader updater = null;
		boolean toLoad = false;
		
		synchronized(urlsToLoad) {
			updater = urlsToLoad.get(url);
			if (updater == null) {
				updater = new UrlLoader();
				urlsToLoad.put(url, updater);
				toLoad = true;
			}
		}
				
		if (toLoad) {
			//refreshService.submit(new LiveProxyPing(url));
			//updater.response = doUpdateDirect(url, null);
		
			//synchronized(urlsToLoad) {
			//	urlsToLoad.remove(url);
			//}
			Future<RobotResponse> futureResponse = refreshService.submit(new CacheUpdateTask(url, null));
			
			try {
				updater.response = futureResponse.get(connectionTimeoutMS + socketTimeoutMS, TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				LOGGER.info("INTERRUPTED: " + e);
			} finally {
				updater.latch.countDown();	
			}
			
		} else {
			
			try {
				LOGGER.info("WAITING FOR " + url);
				if (!updater.latch.await(connectionTimeoutMS, TimeUnit.MILLISECONDS)) {
					LOGGER.info("WAIT FOR " + url + " timed out!");
				}
			} catch (InterruptedException e) {
				LOGGER.info("INTERRUPT FOR " + url);
			}
		}
		
		return updater.response;
	}
	
	private RobotResponse doUpdateDirect(String url, String current)
	{		
		RobotResponse robotResponse;
		
		try {
			//robotResponse = this.loadRobotsUrl(url);
			robotResponse = this.loadRobotsHttpConn(url);
		} catch (Exception exc) {
			robotResponse = new RobotResponse(LIVE_HOST_ERROR);
		}
		
		updateCache(robotResponse, url, current);
		
		return robotResponse;
	}
	
//	private RobotResponse doUpdate(ExecutorService executor, String url, String current)
//	{		
//		LinkedList<Callable<RobotResponse>> tasks = new LinkedList<Callable<RobotResponse>>();
//		tasks.add(new LoadRobotsDirectTask(url));
//		//tasks.add(new LoadRobotsProxyTask(url));
//		
//		RobotResponse robotResponse;
//		
//		try {
//			robotResponse = executor.invokeAny(tasks, connectionTimeoutMS + socketTimeoutMS, TimeUnit.MILLISECONDS);
//		} catch (TimeoutException te) {
//			robotResponse = new RobotResponse(LIVE_TIMEOUT_ERROR);
//		} catch (Exception exc) {
//			robotResponse = new RobotResponse(LIVE_HOST_ERROR);
//		}
//		
//		updateCache(robotResponse, url, current);
//		
//		return robotResponse;
//	}
	
	private boolean updateCache(RobotResponse robotResponse, String url, String currentValue) {		
		String contents = null;
		Jedis jedis = null;
		
		String newRedisValue = null;
		int newTTL = 0;
		boolean ttlOnly = false;
		
		if (robotResponse.isValid()) {
			contents = robotResponse.contents;
			newTTL = totalTTL;
			
			if (contents.isEmpty()) {
				newRedisValue = ROBOTS_TOKEN_EMPTY;
			} else if (contents.length() > MAX_ROBOTS_SIZE) {
				newRedisValue = contents.substring(0, MAX_ROBOTS_SIZE);
			} else {
				newRedisValue = contents;
			}
			
		} else {
			newTTL = notAvailTotalTTL;
			newRedisValue = ROBOTS_TOKEN_ERROR + robotResponse.status;
		}
		
		
		if (currentValue != null) {
			if (currentValue.equals(newRedisValue)) {
				ttlOnly = true;
			}
			
			// Don't override a valid robots with a timeout error
			if (newRedisValue.startsWith(ROBOTS_TOKEN_ERROR) && !currentValue.startsWith(ROBOTS_TOKEN_ERROR)) {
				newRedisValue = currentValue;
				newTTL = notAvailTotalTTL;
				LOGGER.info("REFRESH TIMEOUT: Keeping same robots for " + url + ", refresh timed out");
			}
		}
				
		try {
			jedis = redisConn.getJedisInstance();
			
			if (ttlOnly) {
				jedis.expire(url, newTTL);
			} else {
				jedis.setex(url, newTTL, newRedisValue);
			}
			
		} catch (JedisConnectionException jce) {
			LOGGER.severe("Jedis Exception: " + jce);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
			return false;
			
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
		
		return true;
	}
	
	public void processAsyncUpdate(String url)
	{
		Jedis jedis = null;
		
		try {
			jedis = redisConn.getJedisInstance();
			String current = jedis.get(url);
			
			if (current == null) {
				LOGGER.info("NULL VALUE FOR: " + url);
			}
			
			if ((current != null) && !isExpired(jedis, url, current)) {
				return;
			}
			
			refreshService.submit(new CacheUpdateTask(url, current));
		} catch (JedisConnectionException jce) {
			LOGGER.severe("Jedis Exception: " + jce);
			redisConn.returnBrokenJedis(jedis);		
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
	}
	
	class URLRequestTask implements Runnable
	{
		private String url;

		URLRequestTask(String url)
		{
			this.url = url;
		}
		
		@Override
		public void run()
		{
			processAsyncUpdate(url);
		}
	}
	
	class CacheUpdateTask implements Callable<RobotResponse>
	{
		String url;
		String current;
		
		CacheUpdateTask(String url, String current)
		{
			this.url = url;
			this.current = current;
		}
		
		@Override
		public RobotResponse call()
		{
			long startTime = System.currentTimeMillis();
			
			LOGGER.info("REFRESH workers " + refreshService.getQueue().size());
									
			//doUpdate(refreshService, this.url, this.current);
			pingProxyLiveHttpConn(url);
			RobotResponse response = doUpdateDirect(url, current);
			
//			synchronized(urlsToRefresh) {
//				urlsToRefresh.remove(url);
//			}
			
			PerformanceLogger.noteElapsed("AsyncLoadAndUpdate", System.currentTimeMillis() - startTime, url);
			
			return response;
		}
	}

	class RobotResponse
	{
		String contents;
		int status;
		
		RobotResponse(int status)
		{
			this.status = status;
		}
		
		RobotResponse(String contents, int status)
		{
			this.contents = contents;
			this.status = status;
		}
		
		boolean isValid()
		{
			return (contents != null) && (status == LIVE_OK);
		}
	}
	
	private ByteArrayOutputStream readMaxBytes(InputStream input, int max) throws IOException, InterruptedException
	{
		byte[] byteBuff = new byte[8192];
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(max);
		
		int totalRead = 0;
			
		while (true) {
			int toRead = Math.min(byteBuff.length, max - totalRead);
			
			if (toRead <= 0) {
				break;
			}
			
			int numRead = input.read(byteBuff, 0, toRead);
			
			if (numRead < 0) {
				break;
			}
			
			totalRead += numRead;
			
			baos.write(byteBuff, 0, numRead);
			
			Thread.sleep(1);
		}
		
		return baos;
	}
	
	private void pingProxyLiveHttpConn(String url) {
		if (proxy == null) {
			return;
		}
		
		HttpURLConnection connection = null;
		URL theURL;
		long startTime = System.currentTimeMillis();
		
		synchronized(httpConnectionCount) {
			httpConnectionCount++;
		}
		
		try {
			theURL = new URL(url);
			connection = (HttpURLConnection)theURL.openConnection(proxy);
			connection.setConnectTimeout(connectionTimeoutMS);
			connection.setReadTimeout(socketTimeoutMS);
			connection.setRequestProperty("Connection", "close");
			connection.setRequestMethod("HEAD");
			connection.connect();
			PerformanceLogger.noteElapsed("PingProxyRobots", System.currentTimeMillis() - startTime, url + " " + connection.getResponseMessage());
		} catch (Exception exc) {
			PerformanceLogger.noteElapsed("PingProxyFailure", System.currentTimeMillis() - startTime, url + " " + exc);
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			synchronized(httpConnectionCount) {
				LOGGER.info("HTTP CONNECTIONS: " + httpConnectionCount--);
			}
		}
	}
	
//	private void pingProxyLive(String url) {
//		if (proxyHttpClient == null) {
//			return;
//		}
//		
//		HttpHead httpHead = null;
//		long startTime = System.currentTimeMillis();
//
//		try {
//			httpHead = new HttpHead(url);
//			HttpResponse response = proxyHttpClient.execute(httpHead, new BasicHttpContext());
//			PerformanceLogger.noteElapsed("PingProxyRobots", System.currentTimeMillis() - startTime, url + " " + response.getStatusLine());
//			
//		} catch (Exception exc) {
//			PerformanceLogger.noteElapsed("PingProxyFailure", System.currentTimeMillis() - startTime, url + " " + exc);
//		} finally {
//			httpHead.abort();
//		}
//	}
	
//	private RobotResponse loadProxyLive(String url) {
//		if (proxyHttpClient == null) {
//			return new RobotResponse(0);
//		}
//		
//		HttpGet httpGet = null;
//		long startTime = System.currentTimeMillis();
//		int status = 200;
//		InputStream input = null;
//
//		try {
//			httpGet = new HttpGet(url);
//			HttpResponse response = proxyHttpClient.execute(httpGet, new BasicHttpContext());
//			
//			if (response != null) {
//				status = response.getStatusLine().getStatusCode();
//			}
//
//			if (status != 200) {
//				return new RobotResponse(status);
//			}
//			
//			HttpEntity entity = response.getEntity();
//			input = entity.getContent();
//			
//    		ARCRecord r = new ARCRecord(
//    				new GZIPInputStream(input),
//    				"id",0L,false,false,true);
//    		ArcResource ar = (ArcResource) 
//    			ResourceFactory.ARCArchiveRecordToResource(r, null);
//    		
//    		if ((ar.getStatusCode() == 502) || (ar.getStatusCode() == 504)) {
//    			return new RobotResponse(ar.getStatusCode());
//    		}
//    		
//			int numToRead = (int)ar.getRecordLength();
//			
//			if ((numToRead <= 0) || (numToRead > MAX_ROBOTS_SIZE)) {
//				numToRead = MAX_ROBOTS_SIZE;
//			}
//    		
//			ByteArrayOutputStream baos = readMaxBytes(ar, numToRead);
//								
//			RobotResponse robotResponse = new RobotResponse(baos.toString(), ar.getStatusCode());
//			
//			PerformanceLogger.noteElapsed("LoadProxyRobots", System.currentTimeMillis() - startTime, url + " Size: " + robotResponse.contents.length());
//			LOGGER.info("HTTP CONNECTIONS: " + connMan.getConnectionsInPool());
//			
//			return robotResponse;
//			
//		} catch (Exception exc) {
//			//exc.printStackTrace();
//			LOGGER.info("HTTP CONNECTIONS: " + connMan.getConnectionsInPool());
//			this.connMan.closeIdleConnections(10, TimeUnit.SECONDS);
//			PerformanceLogger.noteElapsed("LoadProxyFailure", System.currentTimeMillis() - startTime, url + " " + exc);
//			return new RobotResponse(500);
//		} finally {
//			httpGet.abort();
//		}
//	}
	
	private Integer httpConnectionCount = 0;
	
	private RobotResponse loadRobotsHttpConn(String url) {

		int status = 0;
		String contents = null;
		HttpURLConnection connection = null;
		URL theURL;
		long startTime = System.currentTimeMillis();
		
		synchronized(httpConnectionCount) {
			httpConnectionCount++;
		}
		
		try {
			theURL = new URL(url);
			connection = (HttpURLConnection)theURL.openConnection();
			connection.setConnectTimeout(connectionTimeoutMS);
			connection.setReadTimeout(socketTimeoutMS);
			connection.setRequestProperty("Connection", "close");
			connection.connect();

			status = connection.getResponseCode();
			
			String contentType = connection.getContentType();
			
			boolean isText = (contentType == null) || (contentType.indexOf("text/plain") >= 0);

			if ((status == LIVE_OK) && isText) {			
				int numToRead = connection.getContentLength();
				
				if ((numToRead <= 0) || (numToRead > MAX_ROBOTS_SIZE)) {
					numToRead = MAX_ROBOTS_SIZE;
				}

				ByteArrayOutputStream baos = readMaxBytes(connection.getInputStream(), numToRead);
								
				String charset = connection.getContentEncoding();
				
				if (charset == null) {
					charset = "utf-8";
				}
				
				contents = baos.toString(charset);
				
				connection.getInputStream().close();
				
			} else if (status == LIVE_OK) {
				LOGGER.info("Invalid content type: " + contentType + " for: " + url);
				status = LIVE_INVALID_TYPE_ERROR;
			}
			
			PerformanceLogger.noteElapsed("HttpLoadSuccess", System.currentTimeMillis() - startTime, url + " " + status + ((contents != null) ? " Size: " + contents.length() : " NULL"));
			
		} catch (Exception exc) {
			
			if (exc instanceof InterruptedIOException) {
				status = LIVE_TIMEOUT_ERROR; //Timeout (gateway timeout)
			} else if (exc instanceof UnknownHostException) {
				status = LIVE_HOST_ERROR;
			}
			
			//this.connMan.closeIdleConnections(10, TimeUnit.SECONDS);
			
			PerformanceLogger.noteElapsed("HttpLoadFail", System.currentTimeMillis() - startTime, 
					"Exception: " + exc + " url: " + url + " status " + status);

			//LOGGER.info("Exception: " + exc + " url: " + url + " status " + status);		
		
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
			synchronized(httpConnectionCount) {
				LOGGER.info("HTTP CONNECTIONS: " + httpConnectionCount--);
			}
		}
		
		return new RobotResponse(contents, status);
	}

//	private RobotResponse loadRobotsUrl(String url) {
//
//		int status = 0;
//		String contents = null;
//		HttpGet httpGet = null;
//		long startTime = System.currentTimeMillis();
//		
//		try {
//			httpGet = new HttpGet(url);
//			HttpContext context = new BasicHttpContext();
//
//			HttpResponse response = directHttpClient.execute(httpGet, context);
//
//			if (response != null) {
//				status = response.getStatusLine().getStatusCode();
//			}
//
//			if (status == 200) {
//				HttpEntity entity = response.getEntity();
//				
//				int numToRead = (int)entity.getContentLength();
//				
//				if ((numToRead <= 0) || (numToRead > MAX_ROBOTS_SIZE)) {
//					numToRead = MAX_ROBOTS_SIZE;
//				}
//
//				ByteArrayOutputStream baos = readMaxBytes(entity.getContent(), numToRead);
//								
//				String charset = EntityUtils.getContentCharSet(entity);
//				
//				if (charset == null) {
//					charset = "utf-8";
//				}
//				
//				contents = baos.toString(charset);
//			}
//			LOGGER.info("HTTP CONNECTIONS: " + connMan.getConnectionsInPool());
//			
//		} catch (Exception exc) {
//			
//			if (exc instanceof InterruptedIOException) {
//				status = LIVE_TIMEOUT_ERROR; //Timeout (gateway timeout)
//			} else if (exc instanceof UnknownHostException) {
//				status = LIVE_HOST_ERROR;
//			}
//			
//			LOGGER.info("HTTP CONNECTIONS: " + connMan.getConnectionsInPool());
//			this.connMan.closeIdleConnections(10, TimeUnit.SECONDS);
//			
//			PerformanceLogger.noteElapsed("HttpLoadFail", System.currentTimeMillis() - startTime, 
//					"Exception: " + exc + " url: " + url + " status " + status);
//
//			//LOGGER.info("Exception: " + exc + " url: " + url + " status " + status);		
//		
//		} finally {
//			httpGet.abort();
//			PerformanceLogger.noteElapsed("HttpLoadRobots", System.currentTimeMillis() - startTime, url + " " + status + ((contents != null) ? " Size: " + contents.length() : " NULL"));
//		}
//		
//		return new RobotResponse(contents, status);
//	}
	
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if (colonIdx > 0) {
    		proxyHost = hostPort.substring(0,colonIdx);
    		proxyPort = Integer.valueOf(hostPort.substring(colonIdx+1));   		
    	}
    }

	public void shutdown() {
//		if (updaterThread != null) {
//			updaterThread.toRun = false;
//			updaterThread.interrupt();
//			
//			try {
//				updaterThread.join(5000);
//			} catch (InterruptedException e) {
//
//			}
//			updaterThread = null;
//		}
		
		refreshService.shutdown();
		
		if (redisConn != null) {
			redisConn.close();
			redisConn = null;
		}
		
		if (connMan != null) {
			connMan.shutdown();
			connMan = null;
		}
	}
	
	public static void main(String args[])
	{
		String redisHost = "localhost";
		int redisPort = 6379;
		
		if (args.length >= 1) {
			redisHost = args[0];
		}
		
		if (args.length >= 2) {
			redisPort = Integer.parseInt(args[1]);
		}
		
		LOGGER.info("Redis Updater: " + redisHost + ":" + redisPort);
		
		RedisConnectionManager manager = new RedisConnectionManager(redisHost, redisPort);
		RedisRobotsCache cache = null;
		
		if (args.length >= 3) {
			cache = new RedisRobotsCache(manager, args[2]);
		} else {
			cache = new RedisRobotsCache(manager);
		}
		
		cache.processRedisUpdateQueue();
	}
}
