package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.accesscontrol.robotstxt.redis.RedisRobotsLogic.KeyRedisValue;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.liveweb.RemoteLiveWebCache;

public class RedisRefresher extends SimpleRedisRobotsCache {
	
	private final static Logger LOGGER = Logger.getLogger(RedisRefresher.class.getName());
	
	protected ExecutorService refreshService;
	
	protected Set<String> activeUrls;
	
	public void setRefreshService(ExecutorService refresh)
	{
		refreshService = refresh;
	}
	
	public static void main(String[] args)
	{
		if (args.length < 1) {
			System.err.println("USAGE: <properties file>");
			System.exit(-1);
		}
		
		String propsFile = args[0];
		
		Properties props = new Properties();
		
		try {
			props.load(new FileInputStream(propsFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Init Live Web
		RemoteLiveWebCache liveweb = new RemoteLiveWebCache();
		liveweb.setMaxHostConnections(Integer.parseInt(props.getProperty("liveweb.maxHostConn", "500")));
		int maxTotalConn = Integer.parseInt(props.getProperty("liveweb.maxTotalConn", "500"));
		liveweb.setMaxTotalConnections(maxTotalConn);
		liveweb.setProxyHostPort(props.getProperty("liveweb.proxyHostPort"));
		liveweb.setConnectionTimeoutMS(Integer.parseInt(props.getProperty("liveweb.timeout", "10000")));
		liveweb.setConnectionTimeoutMS(Integer.parseInt(props.getProperty("liveweb.timeout", "10000")));
		
		// Init Redis Conn
		RedisConnectionManager redisConnMan = new RedisConnectionManager();
		redisConnMan.setHost(props.getProperty("redis.host"));
		redisConnMan.setPort(Integer.parseInt(props.getProperty("redis.port")));
		redisConnMan.setPassword(props.getProperty("redis.password"));		
		redisConnMan.setTimeout(Integer.parseInt(props.getProperty("redis.timeout")));
		redisConnMan.setConnections(Integer.parseInt(props.getProperty("redis.maxConnections")));
		redisConnMan.init();
		
		ExecutorService refreshService = Executors.newFixedThreadPool(maxTotalConn);
		
		RedisRefresher refresher = new RedisRefresher();
		refresher.setLiveweb(liveweb);
		refresher.setRedisConnMan(redisConnMan);
		refresher.setRefreshService(refreshService);
		refresher.setGzipRobots(Boolean.parseBoolean(props.getProperty("redis.gzipRobots", "false")));
		
		refresher.processRedisUpdateQueue();
	}
	
	protected void processRedisUpdateQueue()
	{
		int errorCounter = 0;
		
		int maxErrorThresh = 10;
		int errorSleepTime = 10000;
		
		int maxQueued = 500;
		int currQSize = 0;
		
		activeUrls = new HashSet<String>();
		activeUrls = Collections.synchronizedSet(activeUrls);
		
		try {			
			
			while (true) {
				if (errorCounter >= maxErrorThresh) {
					LOGGER.warning(errorCounter + " Redis ERRORS! Sleeping for " + errorSleepTime);
					Thread.sleep(errorSleepTime);
				}
				
				currQSize = activeUrls.size();
									
				if (currQSize >= maxQueued) {
					Thread.sleep(100);
					continue;
				} else {
					Thread.sleep(0);
				}
				
				KeyRedisValue value = null;
				
				//long startTime = System.currentTimeMillis();
								
				try {
					value = redisCmds.popKeyAndGet(UPDATE_QUEUE_KEY);
					errorCounter = 0;
				} catch (LiveWebCacheUnavailableException e) {
					errorCounter++;
				} catch (Exception exc) {
					errorCounter = maxErrorThresh;
					LOGGER.log(Level.SEVERE, "REDIS SEVERE", exc);
				} finally {
					//PerformanceLogger.noteElapsed("PopKeyAndGet", System.currentTimeMillis() - startTime);
				}
				
				if (value == null) {
					continue;
				}
				
				String url = value.key;
				
				if (!isExpired(value, url, 0)) {
					continue;
				}
								
				if (activeUrls.contains(url)) {
					continue;
				}

				refreshService.execute(new ForceUpdater(url));

			}
		} catch (InterruptedException e) {
			LOGGER.info("Interrupted, Quitting");
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "UPDATER SEVERE", e);
		} finally {
			shutdown();
		}
	}
	
	private class ForceUpdater implements Runnable
	{
		private String url;
		
		private ForceUpdater(String url)
		{
			this.url = url;
		}
		
		@Override
		public void run()
		{
			RobotsResult result = forceUpdate(url, 0, true);
			
			if (LOGGER.isLoggable(Level.INFO)) {
				LOGGER.info((!result.isSameRobots() ? "UPDATE " : "NOCHANGE ") + url); 
			}
			
			if (activeUrls != null) {
				activeUrls.remove(url);
			}
		}
	}
}
