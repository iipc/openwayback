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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.accesscontrol.robotstxt.RedisRobotsLogic.RedisValue;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.archive.wayback.webapp.PerformanceLogger;

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
	private int readTimeoutMS = 5000;
	private int responseTimeoutMS = 10000;
	private int pingConnectTimeoutMS = 100;
	
//	private int maxConnections = 1500;
//	private int maxPerRoute = 20;
	
	private String proxyHost;
	private int proxyPort;
	
	private String userAgent;
	
	//private ThreadSafeClientConnManager connMan;

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
	private Map<String, RobotsContext> activeContexts;
	
	/* REDIS */
	private RedisRobotsLogic redisCmds;

	public RedisRobotsCache(RedisConnectionManager redisConn) {
		LOGGER.setLevel(Level.FINER);

		this.redisCmds = new RedisRobotsLogic(redisConn);

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
		activeContexts = new HashMap<String, RobotsContext>();
		
		HttpURLConnection.setFollowRedirects(true);
	}
	
//	public void setMaxTotalConnections(int max)
//	{
//		connMan.setMaxTotal(max);
//	}
//	
//	public int getMaxTotalConnections()
//	{
//		return connMan.getMaxTotal();
//	}
//	
//	public void setMaxPerRoute(int max)
//	{
//		connMan.setDefaultMaxPerRoute(max);
//	}
//	
//	public int getMaxPerRoute()
//	{
//		return connMan.getDefaultMaxPerRoute();
//	}
	
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
		
		String url = urlURL.toExternalForm();
		
		RedisValue value = redisCmds.getValue(url);
		
		if (value == null) {
			throw new LiveWebCacheUnavailableException("Jedis Error");
		}		
				
		return processSingleRobots(value, url);
	}
	
	
//	public Resource getCachedResource(String[] urls) throws LiveDocumentNotAvailableException,
//			LiveWebCacheUnavailableException, LiveWebTimeoutException,
//			IOException {
//				
//		List<String> robotsFiles = null;
//		Jedis jedis = null;
//		long startTime = 0;
//		
//		try {
//			jedis = redisConn.getJedisInstance();
//	
//			startTime = System.currentTimeMillis();
//			robotsFiles = jedis.mget(urls);
//			PerformanceLogger.noteElapsed("RedisGet", System.currentTimeMillis() - startTime, "# URLS" + urls.length);
//		} catch (JedisConnectionException jce) {
//			LOGGER.severe("Jedis Exception: " + jce);
//			redisConn.returnBrokenJedis(jedis);
//			jedis = null;
//		}
//		
//		int count = 0;
//		String theUrl = null;
//		String theRobots = null;
//		
//		for (String robots : robotsFiles) {
//			if (theRobots == null) {
//				theRobots = robots;
//				theUrl = urls[count];
//			} else if (theRobots.startsWith(ROBOTS_TOKEN_ERROR) && !robots.startsWith(ROBOTS_TOKEN_ERROR)) {
//				theRobots = robots;
//				theUrl = urls[count];			
//			}
//			count++;
//		}
//		
//		return processSingleRobots(jedis, theUrl, theRobots);
//	}
	
	protected Resource processSingleRobots(RedisValue value, String url) throws LiveDocumentNotAvailableException
	{				
		if (value.value == null) {
			RobotsContext context = doSyncUpdate(url, null, true);
											
			if ((context == null) || !context.isValid()) {
				throw new LiveDocumentNotAvailableException("Error Loading Live Robots");	
			}
			
			value.value = context.getNewRobots();
			
		} else {
			
			if (isExpired(value, url)) {	
				redisCmds.pushKey(UPDATE_QUEUE_KEY, url);
			}
			
			
			if (value.value.startsWith(ROBOTS_TOKEN_ERROR)) {
				throw new LiveDocumentNotAvailableException("Robots Error: " + value.value);	
			} else if (value.value.equals(ROBOTS_TOKEN_EMPTY)) {
				value.value = "";
			}
		}				
			
		return new RobotsTxtResource(value.value);
	}
			
	public boolean isExpired(RedisValue value, String url) {
				
		int maxTime, refreshTime;
		
		if (value.value.startsWith(ROBOTS_TOKEN_ERROR)) {
			maxTime = notAvailTotalTTL;
			refreshTime = notAvailRefreshTTL;
		} else {
			maxTime = totalTTL;
			refreshTime = refreshTTL;
		}
		
		if ((maxTime - value.ttl) >= refreshTime) {
			LOGGER.info("Queuing robot refresh: "
					+ (maxTime - value.ttl) + ">=" + refreshTime + " " + url);
			
			return true;
		}
		
		return false;
	}
	
	protected void processRedisUpdateQueue()
	{
		ExecutorService mainLoopService = null;
		
		try {			
			mainLoopService = Executors.newFixedThreadPool(maxCoreUpdateThreads);
						
			while (true) {
				String url = redisCmds.popKey(UPDATE_QUEUE_KEY);
				
				synchronized(activeContexts) {
					if (activeContexts.containsKey(url)) {
						continue;
					}
				}
				
				mainLoopService.execute(new URLRequestTask(url));
				
				Thread.sleep(1);
			}
		} catch (InterruptedException e) {
			//DO NOTHING
		} finally {
			mainLoopService.shutdown();
		}
	}
	
	private RobotsContext doSyncUpdate(String url, String current, boolean canceleable)
	{
		RobotsContext context = null;
		boolean toLoad = false;
		
		int numUrls = 0;
		
		synchronized(activeContexts) {
			context = activeContexts.get(url);
			if (context == null) {
				context = new RobotsContext(url, current);
				activeContexts.put(url, context);
				toLoad = true;
			}
			numUrls = activeContexts.size();
		}
				
		if (toLoad) {		
			Future<RobotsContext> futureResponse = 
				refreshService.submit(new AsyncLoadAndUpdate(context), context);
			
			try {
				context = futureResponse.get(responseTimeoutMS, TimeUnit.MILLISECONDS);
				
			} catch (Exception e) {
				LOGGER.info("INTERRUPTED: " + url + " " + e);
				
				if (canceleable)	{	
					futureResponse.cancel(true);
				}
			
			} finally {				
				context.latch.countDown();
				
				synchronized(activeContexts) {
					activeContexts.remove(url);
				}
			}
		} else {
			
			try {
				LOGGER.info("WAITING FOR: " + url + " -- # URLS: " + numUrls);
				
				if (!context.latch.await(responseTimeoutMS, TimeUnit.MILLISECONDS)) {
					LOGGER.info("WAIT FOR " + url + " timed out!");
				}
				
			} catch (InterruptedException e) {
				LOGGER.info("INTERRUPT FOR " + url);
			}
		}
		
		return context;
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
	
	private void updateCache(final RobotsContext context) {		
		String contents = null;
		
		String newRedisValue = null;
		int newTTL = 0;
		boolean ttlOnly = false;
		
		if ((context != null) && context.isValid()) {
			contents = context.getNewRobots();
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
			newRedisValue = ROBOTS_TOKEN_ERROR + context.getStatus();
		}
		
		String currentValue = context.current;		
		
		if (currentValue != null) {
			if (currentValue.equals(newRedisValue)) {
				ttlOnly = true;
			}
			
			// Don't override a valid robots with a timeout error
			if (newRedisValue.startsWith(ROBOTS_TOKEN_ERROR) && !currentValue.startsWith(ROBOTS_TOKEN_ERROR)) {
				newRedisValue = currentValue;
				newTTL = totalTTL;
				LOGGER.info("REFRESH TIMEOUT: Keeping same robots for " + context.url + ", refresh timed out");
			}
		}
		
		final RedisValue value = new RedisValue((ttlOnly ? newRedisValue : null), newTTL);
		redisCmds.updateValue(context.url, value);
	}
	
	public void processAsyncUpdate(final String url)
	{
		RedisValue value = redisCmds.getValue(url);
		
		if (value == null) {
			return;
		}
		
		if (value.value == null) {
			LOGGER.info("NULL VALUE FOR: " + url);
		}
					
		if ((value.value != null) && !isExpired(value, url)) {
			return;
		}
			
		doSyncUpdate(url, value.value, false);
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
	
	class AsyncLoadAndUpdate implements Runnable
	{
		RobotsContext context;
		
		AsyncLoadAndUpdate(RobotsContext context)
		{
			this.context = context;
		}
		
		@Override
		public void run()
		{
			long startTime = System.currentTimeMillis();
			
			LOGGER.info("REFRESH workers " + refreshService.getQueue().size());
									
			try {
				loadRobotsHttpConn(context);
			} catch (Exception exc) {
				context.setStatus(LIVE_HOST_ERROR);
			}
				
			pingProxyLiveHttpConn(context.url);
			
			updateCache(context);
			
			PerformanceLogger.noteElapsed("AsyncLoadAndUpdate", System.currentTimeMillis() - startTime, context.url);
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
			connection.setConnectTimeout(pingConnectTimeoutMS);
			connection.setRequestProperty("Connection", "close");
			connection.setRequestMethod("HEAD");
			connection.connect();
			PerformanceLogger.noteElapsed("PingProxySuccess", System.currentTimeMillis() - startTime, url + " " + connection.getResponseMessage());
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
	
	private void loadRobotsHttpConn(RobotsContext context)
	{
		String contents = null;
		int status = 0;
		HttpURLConnection connection = null;
		InputStream input = null;
		URL theURL;
		
		String url = context.url;
		
		long startTime = System.currentTimeMillis();
		
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
			
			context.setStatus(status);
						
			if (status == LIVE_OK) {
				// Content Type check
				String contentType = connection.getContentType();
				
				if ((contentType == null) || (contentType.indexOf("text/plain") < 0)) {
					LOGGER.info("Questionable Content-Type: " + contentType + " for: " + url);
				}
				
				int numToRead = connection.getContentLength();
				
				if ((numToRead <= 0) || (numToRead > MAX_ROBOTS_SIZE)) {
					numToRead = MAX_ROBOTS_SIZE;
				}

				input = connection.getInputStream();
				ByteArrayOutputStream baos = readMaxBytes(input, numToRead);
								
				String charset = connection.getContentEncoding();
				
				if (charset == null) {
					charset = "utf-8";
				}
				
				baos.flush();
				contents = baos.toString(charset);
				baos.close();
				
				context.setNewRobots(contents);				
			}
			
			PerformanceLogger.noteElapsed("HttpLoadSuccess", System.currentTimeMillis() - startTime, url + " " + status + ((contents != null) ? " Size: " + contents.length() : " NULL"));
			
		} catch (Exception exc) {
			
			if (exc instanceof InterruptedIOException) {
				status = LIVE_TIMEOUT_ERROR; //Timeout (gateway timeout)
			} else if (exc instanceof InterruptedException) {
				status = LIVE_TIMEOUT_ERROR;
			} else if (exc instanceof UnknownHostException) {
				status = LIVE_HOST_ERROR;
			}
			
			context.setStatus(status);
			
			PerformanceLogger.noteElapsed("HttpLoadFail", System.currentTimeMillis() - startTime, 
					"Exception: " + exc + " url: " + url + " status " + status);
			
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
			
			if (connection != null) {
				connection.disconnect();
			}
			
			synchronized(httpConnectionCount) {
				LOGGER.info("HTTP CONNECTIONS: " + httpConnectionCount--);
			}
		}
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

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
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
		
		if (redisCmds != null) {
			redisCmds.close();
			redisCmds = null;
		}
		
//		if (connMan != null) {
//			connMan.shutdown();
//			connMan = null;
//		}
	}
	
	public static void main(String args[])
	{
		String redisHost = "localhost";
		String redisPassword = null;
		String recProxy = null;
		String userAgent = null;
		int redisPort = 6379;
		
		Iterator<String> paramsIter = Arrays.asList(args).iterator();
		
		while (paramsIter.hasNext()) {
			String flag = paramsIter.next();
			
			if (!paramsIter.hasNext()) {
				break;
			}
			
			if (flag.equals("-h")) {
				redisHost = paramsIter.next();
			} else if (flag.equals("-p")) {
				redisPort = Integer.parseInt(paramsIter.next());			
			} else if (flag.equals("-a")) {
				redisPassword = paramsIter.next();
			} else if (flag.equals("-r")) {
				recProxy = paramsIter.next();
			} else if (flag.equals("-u")) {
				userAgent = paramsIter.next();
			}
		}
		
		RedisConnectionManager manager = new RedisConnectionManager();
		manager.setHost(redisHost);
		manager.setPort(redisPort);
		manager.setPassword(redisPassword);
		
		LOGGER.info("Redis Updater: " + redisHost + ":" + redisPort);
		
		RedisRobotsCache cache = null;
		
		if (recProxy != null) {
			cache = new RedisRobotsCache(manager, recProxy);
		} else {
			cache = new RedisRobotsCache(manager);
		}
		
		cache.setUserAgent(userAgent);
		cache.processRedisUpdateQueue();
	}
}
