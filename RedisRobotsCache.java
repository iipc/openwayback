package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.archive.wayback.accesscontrol.robotstxt.RedisRobotsLogic.RedisValue;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.webapp.PerformanceLogger;

public class RedisRobotsCache extends LiveWebProxyCache {

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
	
	/* THREAD WORKER SETTINGS */

	private Map<String, RobotsContext> activeContexts;
	
	/* REDIS */
	private RedisRobotsLogic redisCmds;
		
	public void setRedisConnMan(RedisConnectionManager redisConn) {
		this.redisCmds = new RedisRobotsLogic(redisConn);
	}

	@Override
	public void init() {
		super.init();
		activeContexts = new HashMap<String, RobotsContext>();
	}
	
	@Override
	public Resource getCachedResource(URL urlURL, long maxCacheMS,
				boolean cacheFails) throws LiveDocumentNotAvailableException,
				LiveWebCacheUnavailableException, LiveWebTimeoutException,
				IOException {
		
		String url = urlURL.toExternalForm();
		
		// HACK: until the boolean can be specified in spring
		cacheFails = (cacheFails || (maxCacheMS == 0));
		
		RedisValue value = redisCmds.getValue(url);
			
		if (value == null) {
			RobotsContext context = doSyncUpdate(url, null, cacheFails, true);
											
			if ((context == null) || !context.isValid()) {
				throw new LiveDocumentNotAvailableException("Error Loading Live Robots");	
			}
			
			return new RobotsTxtResource(context.getNewRobots());
			
		} else {
			
			if (isExpired(value, url)) {	
				redisCmds.pushKey(UPDATE_QUEUE_KEY, url, MAX_UPDATE_QUEUE_SIZE);
			}			
			
			if (value.value.startsWith(ROBOTS_TOKEN_ERROR)) {
				throw new LiveDocumentNotAvailableException("Robots Error: " + value.value);	
			} else if (value.value.equals(ROBOTS_TOKEN_EMPTY)) {
				value.value = "";
			}
			
			return new RobotsTxtResource(value.value);
		}
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
			LOGGER.info("Queue for robot refresh: "
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
				Thread.sleep(1);
				
				String url = redisCmds.popKey(UPDATE_QUEUE_KEY);
				
				if (url == null) {
					continue;
				}
				
				synchronized(activeContexts) {
					if (activeContexts.containsKey(url)) {
						continue;
					}
				}
				
				mainLoopService.execute(new URLRequestTask(url));
			}
		} catch (InterruptedException e) {
			//DO NOTHING
		} finally {
			mainLoopService.shutdown();
		}
	}
	
	private RobotsContext doSyncUpdate(String url, String current, boolean cacheFails, boolean canceleable)
	{
		RobotsContext context = null;
		boolean toLoad = false;
		
		int numUrls = 0;
		
		synchronized(activeContexts) {
			context = activeContexts.get(url);
			if (context == null) {
				context = new RobotsContext(url, current, cacheFails);
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
	
//	private RobotsContextTask getRobotsContextTask(String url, String current, boolean cacheFails)
//	{
//		RobotsContext context;
//		boolean toLoad = true;
//		
//		synchronized(activeContexts) {
//			context = activeContexts.get(url);
//			if (context == null) {
//				context = new RobotsContext(url, current, cacheFails);
//				activeContexts.put(url, context);
//				toLoad = true;
//			}
//		}
//		
//		return new RobotsContextTask(toLoad, context);		
//	}
//	
//	class RobotsContextTask implements Callable<RobotsContext>
//	{
//		final boolean finalToLoad;
//		final RobotsContext finalContext;
//		
//		RobotsContextTask(boolean toLoad, RobotsContext context)
//		{
//			finalToLoad = toLoad;
//			finalContext = context;
//		}
//		
//		@Override
//		public RobotsContext call() throws Exception {
//			
//			if (finalToLoad) {	
//				try {
//					processAsyncUpdate(finalContext);
//				} finally {
//					finalContext.latch.countDown();
//					
//					synchronized(activeContexts) {
//						activeContexts.remove(finalContext.url);
//					}
//				}
//			} else {
//				try {
//					LOGGER.info("WAITING FOR: " + finalContext.url);
//					
//					if (!finalContext.latch.await(responseTimeoutMS, TimeUnit.MILLISECONDS)) {
//						LOGGER.info("WAIT FOR " + finalContext.url + " timed out!");
//					}
//					
//				} catch (InterruptedException e) {
//					LOGGER.info("INTERRUPT FOR " + finalContext.url);
//				}
//			}
//			return finalContext;
//		}
//	}
//	
//
//	private RobotsContext doSyncUpdate(List<String> urls, boolean cacheFails, boolean canceleable)
//	{		
//		LinkedList<RobotsContextTask> tasks = new LinkedList<RobotsContextTask>();
//		
//		for (String url : urls) {
//			tasks.add(getRobotsContextTask(url, null, cacheFails));
//		}
//		
//		ExecutorCompletionService<RobotsContext> completer = new ExecutorCompletionService<RobotsContext>(refreshService);
//		
//		for (Callable<RobotsContext> task : tasks) {
//			completer.submit(task);
//		}
//		
//		try {
//			Future<RobotsContext> future = completer.poll(this.responseTimeoutMS, TimeUnit.MILLISECONDS);
//			if (future == null) {
//				return tasks.getFirst().finalContext;
//			}
//			
//			RobotsContext firstInvalid = null;
//						
//			while (future != null) {
//				RobotsContext context = future.get();
//				
//				if (context.isValid()) {
//					return context;
//				} else if (firstInvalid == null) {
//					firstInvalid = context;
//				}
//				
//				future = completer.poll();
//			}
//			
//			return firstInvalid;
//			
//		} catch (InterruptedException e) {
//			return tasks.getFirst().finalContext;
//		} catch (ExecutionException e) {
//			return tasks.getFirst().finalContext;
//		}
//	}
	
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
			} else if (contents.length() > RobotsContext.MAX_ROBOTS_SIZE) {
				newRedisValue = contents.substring(0, RobotsContext.MAX_ROBOTS_SIZE);
			} else {
				newRedisValue = contents;
			}
			
		} else {
			// Only Cacheing successful lookups
			if (!context.cacheFails) {
				return;
			}
			
			newRedisValue = ROBOTS_TOKEN_ERROR + context.getStatus();
			
			switch (context.getStatus()) {
			case RobotsContext.LIVE_HOST_ERROR:
				newTTL = totalTTL;
				break;
				
			default:
				newTTL = notAvailTotalTTL;
			}
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
				LOGGER.info("REFRESH ERROR: Keeping same robots for " + context.url + ", refresh timed out");
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
			
		doSyncUpdate(url, value.value, true, true);
	}
	
	public void processAsyncUpdate(final RobotsContext context)
	{
		context.startTime = System.currentTimeMillis();
		
		try {									
			connMan.loadRobots(context, context.url, userAgent);				
					
			updateCache(context);
			
			String pingStatus;
			long startTimePingProxy = System.currentTimeMillis();
			
			if (connMan.pingProxyLive(context.url)) {
				pingStatus = "PingProxySuccess";
			} else {
				pingStatus = "PingProxyFailure";
			}
			
			PerformanceLogger.noteElapsed(pingStatus, System.currentTimeMillis() - startTimePingProxy, context.url + " ");
			
		} finally {
			PerformanceLogger.noteElapsed("AsyncLoadAndUpdate", System.currentTimeMillis() - context.startTime, context.url);	
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
			processAsyncUpdate(context);
		}
	}

	@Override
	public void shutdown() {		
		super.shutdown();
		
		if (redisCmds != null) {
			redisCmds.close();
			redisCmds = null;
		}
		
		if (connMan != null) {
			connMan.close();
			connMan = null;
		}
	}
	
	@Override
	protected void appendLogInfo(PrintWriter info)
	{
		super.appendLogInfo(info);
        info.println("  Active URLS: " + activeContexts.size());
	}
		
	public static void main(String args[])
	{
		String redisHost = "localhost";
		String redisPassword = null;
		String recProxy = null;
		String userAgent = null;
		int redisPort = 6379;
		
		int maxPerRouteConnections = 0;
		int maxConnections = 0;
		int maxCoreUpdateThreads = 0;
		
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
			} else if (flag.equals("max_conn")) {
				maxConnections = Integer.parseInt(paramsIter.next());
			} else if (flag.equals("max_route_conn")) {
				maxPerRouteConnections = Integer.parseInt(paramsIter.next());
			} else if (flag.equals("max_thread")) {
				maxCoreUpdateThreads = Integer.parseInt(paramsIter.next());
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
		
		if (maxConnections != 0) {		
			httpManager.setMaxConnections(maxConnections);
		}
		
		if (maxPerRouteConnections != 0) {
			httpManager.setMaxPerRouteConnections(maxPerRouteConnections);
		}
		
		httpManager.init();
		
		RedisRobotsCache cache = new RedisRobotsCache();
		cache.setHttpConnMan(httpManager);
		cache.setRedisConnMan(redisMan);
		cache.setUserAgent(userAgent);
		
		if (maxCoreUpdateThreads != 0) {
			cache.setMaxCoreUpdateThreads(maxCoreUpdateThreads);
		}
		
		cache.init();
		cache.processRedisUpdateQueue();
	}
}
