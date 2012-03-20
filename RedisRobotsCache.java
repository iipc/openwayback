package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
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
	final static int MAX_UPDATE_QUEUE_SIZE = 50000;

	final static int LIVE_OK = 200;
	final static int LIVE_TIMEOUT_ERROR = 900;
	final static int LIVE_HOST_ERROR = 910;
	final static int LIVE_INVALID_TYPE_ERROR = 920;
	
	
	final static int MAX_ROBOTS_SIZE = 500000;
	
	/* SOCKET SETTINGS / PARAMS */
	
	BaseHttpConnMan connMan = null;

	private int connectionTimeoutMS = 5000;
	private int readTimeoutMS = 5000;
	private int responseTimeoutMS = 10000;
	private int pingConnectTimeoutMS = 100;
	
	private String userAgent;
	
	/* THREAD WORKER SETTINGS */
	
	private int maxNumUpdateThreads = 1000;
	private int maxCoreUpdateThreads = 75;
	
	private int threadKeepAliveTime = 5000;
	
	private ThreadPoolExecutor refreshService;

	private Map<String, RobotsContext> activeContexts;
	
	/* REDIS */
	private RedisRobotsLogic redisCmds;

	public RedisRobotsCache(RedisConnectionManager redisConn, BaseHttpConnMan connMan) {
		LOGGER.setLevel(Level.FINER);

		redisCmds = new RedisRobotsLogic(redisConn);
		
		refreshService = new ThreadPoolExecutor(maxCoreUpdateThreads, maxNumUpdateThreads, threadKeepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());		
		
		activeContexts = new HashMap<String, RobotsContext>();
		
		this.connMan = connMan;
		this.connMan.setSocketReadTimeoutMS(readTimeoutMS);
		this.connMan.setConnectionTimeout(connectionTimeoutMS);
		this.connMan.setPingConnectTimeout(pingConnectTimeoutMS);
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
				redisCmds.pushKey(UPDATE_QUEUE_KEY, url, MAX_UPDATE_QUEUE_SIZE);
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
	

	private void updateCache(final RobotsContext context) {		
		String contents = null;
		
		String newRedisValue = null;
		int newTTL = 0;
		boolean ttlOnly = false;
		
		if (context.isValid()) {
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
		
		final RedisValue value = new RedisValue((ttlOnly ? null : newRedisValue), newTTL);
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
		} else if(!isExpired(value, url)) {
			return;
		}
			
		doSyncUpdate(url, value.value, true);
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
				connMan.loadRobots(new RobotsLoadCallback(context), context.url, userAgent);
			} catch (Exception exc) {
				context.setStatus(LIVE_HOST_ERROR);
			}
			
			if (!Thread.interrupted()) {
				String pingStatus;
				
				if (connMan.pingProxyLive(context.url)) {
					pingStatus = "PingProxySuccess";
				} else {
					pingStatus = "PingProxyFailure";
				}
				
				PerformanceLogger.noteElapsed(pingStatus, System.currentTimeMillis() - startTime, context.url + " ");
				
				updateCache(context);
			}

			PerformanceLogger.noteElapsed("AsyncLoadAndUpdate", System.currentTimeMillis() - startTime, context.url);
		}
	}

	
	private ByteArrayOutputStream readMaxBytes(InputStream input, int max) throws IOException, InterruptedException
	{
		byte[] byteBuff = new byte[8192];
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream(max);
		
		int totalRead = 0;
			
		while (true) {
			Thread.sleep(1);
			
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
		}
		
		return baos;
	}
	
	class RobotsLoadCallback implements BaseHttpConnMan.ConnectionCallback
	{
		RobotsContext context;
		long startTime;
		
		RobotsLoadCallback(RobotsContext context)
		{
			this.context = context;
			this.startTime = System.currentTimeMillis();
		}

		@Override
		public boolean supportStatus(int status) {
			context.setStatus(status);
			return (status == LIVE_OK);
		}

		@Override
		public void doRead(int numToRead, String contentType, InputStream input, String charset) throws IOException, InterruptedException {
			if ((contentType == null) || (contentType.indexOf("text/plain") < 0)) {
				LOGGER.info("Questionable Content-Type: " + contentType + " for: " + context.url);
			}
					
			if ((numToRead <= 0) || (numToRead > MAX_ROBOTS_SIZE)) {
				numToRead = MAX_ROBOTS_SIZE;
			}

			ByteArrayOutputStream baos = readMaxBytes(input, numToRead);
										
			if (charset == null) {
				charset = "utf-8";
			}
			
			baos.flush();
			String contents = baos.toString(charset);
			baos.close();
			
			context.setNewRobots(contents);
			PerformanceLogger.noteElapsed("HttpLoadSuccess", System.currentTimeMillis() - startTime, context.url + " " + context.getStatus() + ((contents != null) ? " Size: " + contents.length() : " NULL"));
		}

		@Override
		public void handleException(Exception exc) {
			int status = 0;
			
			if (exc instanceof InterruptedIOException) {
				status = LIVE_TIMEOUT_ERROR; //Timeout (gateway timeout)
			} else if (exc instanceof InterruptedException) {
				status = LIVE_TIMEOUT_ERROR;
			} else if (exc instanceof UnknownHostException) {
				status = LIVE_HOST_ERROR;
			}
			
			context.setStatus(status);
			
			PerformanceLogger.noteElapsed("HttpLoadFail", System.currentTimeMillis() - startTime, 
					"Exception: " + exc + " url: " + context.url + " status " + status);				
		}
		
	}
		
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public void shutdown() {		
		refreshService.shutdown();
		
		if (redisCmds != null) {
			redisCmds.close();
			redisCmds = null;
		}
		
		if (connMan != null) {
			connMan.close();
			connMan = null;
		}
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
		
		RedisConnectionManager redisMan = new RedisConnectionManager();
		redisMan.setHost(redisHost);
		redisMan.setPort(redisPort);
		redisMan.setPassword(redisPassword);
		redisMan.init();
				
		LOGGER.info("Redis Updater: " + redisHost + ":" + redisPort);
		
		BaseHttpConnMan httpManager = new ApacheHttpConnMan();
		
		if (recProxy != null) {
			httpManager.setProxyHostPort(recProxy);	
		}
		
		httpManager.init();
		
		RedisRobotsCache cache = new RedisRobotsCache(redisMan, httpManager);
		cache.setUserAgent(userAgent);
		cache.processRedisUpdateQueue();
	}
}
