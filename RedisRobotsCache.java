package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.liveweb.LiveWebCache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisRobotsCache {
	
	private final static Logger LOGGER = 
		Logger.getLogger(RedisRobotsCache.class.getName());
	
    private int connectionTimeoutMS = 10000;
    private int socketTimeoutMS = 5000;
    
    private ClientConnectionManager connMan;
    private HttpClient httpClient;
    
	   
    private JedisPool jedisPool;
    
    private String redisHost;
    private int redisPort;
    private int redisDB;
    
    final static int ONE_DAY = 60 * 60 * 24;

	private int totalTTL = ONE_DAY * 4;
	private int refreshTTL = ONE_DAY;
	
	private int notAvailTotalTTL = ONE_DAY;
	private int notAvailRefreshTTL = 60 * 60;
	
	private final String robotsNotAvailTxt = "0_Invalid_0";
	
	private JedisPoolConfig jedisConfig = new JedisPoolConfig();
	private int jedisCount = 0;
	
	private RedisUpdater updaterThread;
    
    public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public int getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(int redisPort) {
		this.redisPort = redisPort;
	}

	public int getRedisDB() {
		return redisDB;
	}

	public void setRedisDB(int redisDB) {
		this.redisDB = redisDB;
	}

	public JedisPoolConfig getJedisConfig() {
		return jedisConfig;
	}

	public void setJedisConfig(JedisPoolConfig jedisConfig) {
		this.jedisConfig = jedisConfig;
	}

	public RedisRobotsCache()
    {
		initLogger();
        initHttpClient();
        initPool();
    }
	
	protected void initLogger()
	{
		LOGGER.setLevel(Level.FINER);
	}
	
	protected void initHttpClient()
	{
        connMan = new ThreadSafeClientConnManager();
        
		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeoutMS);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
		
        httpClient = new DefaultHttpClient(connMan, params);
	}
	
	protected void initPool()
	{
        jedisConfig.setMaxActive(50);
        jedisConfig.setTestOnBorrow(true);
        jedisConfig.setTestWhileIdle(true);
        jedisConfig.setTestOnReturn(true);
        
		LOGGER.fine("Initializing Jedis Pool: Host = " + redisHost + " Port: " + redisPort);
		
		jedisPool = new JedisPool(jedisConfig, redisHost, redisPort);
		
		updaterThread = new RedisUpdater();
		updaterThread.start();
	}
	
	class CacheInstance implements LiveWebCache
	{
		private Jedis jedis = null;
				
		public Resource getCachedResource(URL url, long maxCacheMS,
				boolean bUseOlder) throws LiveDocumentNotAvailableException,
				LiveWebCacheUnavailableException, LiveWebTimeoutException,
				IOException {
			
			try {
				if (jedis == null) {
					jedis = getJedisInstance();
				}
				
				return getRobots(jedis, url.toExternalForm());
			} catch (JedisConnectionException jedisExc) {
				LOGGER.severe("Jedis Exception: " + jedisExc);
				returnBrokenJedis(jedis);
				jedis = null;
				throw new LiveWebCacheUnavailableException(jedisExc.toString());	
			}
		}

		public void shutdown() {
			returnJedisInstance(jedis);
		}
	}
		
	public CacheInstance getCacheInstance()
	{
		return new CacheInstance();
	}
	
	protected Jedis getJedisInstance()
	{
		Jedis jedis = null;
		
		try {		
			jedis = jedisPool.getResource();
			if (redisDB != 0) {
				jedis.select(redisDB);
			}
			LOGGER.fine("Getting Jedis Instance: " + ++jedisCount);
			return jedis;
		} catch (RuntimeException rte) {
			this.returnBrokenJedis(jedis);
			LOGGER.severe(rte.toString());
			return null;
		}
	}
	
	protected void returnJedisInstance(Jedis jedis)
	{
		if ((jedisPool != null) && (jedis != null)) {
			jedisPool.returnResource(jedis);
			LOGGER.fine("Returning Jedis Instance: " + --jedisCount);
		}
	}
	
	protected void returnBrokenJedis(Jedis jedis)
	{
		if ((jedisPool != null) && (jedis != null)) {
			jedisPool.returnBrokenResource(jedis);
			LOGGER.fine("Returning Broken Jedis Instance: " + --jedisCount);
		}		
	}
	
	public Resource getRobots(Jedis jedis, String urlKey) throws LiveDocumentNotAvailableException {
		
		String robotsFile = null;
		
		robotsFile = jedis.get(urlKey);
		
		if (robotsFile == null) {
			LOGGER.fine("UNCACHED Robots: " + urlKey);
			
			robotsFile = updateCache(jedis, urlKey);
		} else if (robotsFile.equals(robotsNotAvailTxt)) {
			long ttl = jedis.ttl(urlKey);
			LOGGER.fine("Cached Robots NOT AVAIL " + urlKey + " TTL: " + ttl);
			
			if ((notAvailTotalTTL - ttl) >= notAvailRefreshTTL) {
				LOGGER.fine("Refreshing NOT AVAIL robots: " + (notAvailTotalTTL - ttl) + ">=" + notAvailRefreshTTL);
				updaterThread.addUrlLookup(urlKey);
				jedis = null;
			}
			
			throw new LiveDocumentNotAvailableException(urlKey);
			
		} else {
			long ttl = jedis.ttl(urlKey);
			LOGGER.fine("Cached Robots: " + urlKey + " TTL: " + ttl);
			
			if ((totalTTL - ttl) >= refreshTTL) {
				LOGGER.fine("Refreshing robots: " + (totalTTL - ttl) + ">=" + refreshTTL);
				updaterThread.addUrlLookup(urlKey);
				jedis = null;
			}
		}
		
		return new RobotsTxtResource(robotsFile);
	}
	
	private String updateCache(Jedis jedis, String url) throws LiveDocumentNotAvailableException {
		try {
			String robotsFile = loadRobotsUrl(url);
			jedis.setex(url, totalTTL, robotsFile);
			return robotsFile;
		} catch (LiveDocumentNotAvailableException notAvail) {
			jedis.setex(url, notAvailTotalTTL, robotsNotAvailTxt);
			throw notAvail;
		}
	}

	class RedisUpdater extends Thread
	{
		private LinkedBlockingQueue<String> queue;
		private boolean toRun;
		
		private RedisUpdater()
		{		
			this.toRun = true;
			this.queue = new LinkedBlockingQueue<String>();
		}
		
		public void addUrlLookup(String url)
		{
			try {
				if (!queue.contains(url)) {
					queue.put(url);
				}
			} catch (InterruptedException ignore) {
				//ignore?
			}
		}
		
		
		@Override
		public void run()
		{
			Jedis updaterJedis = null;
			
			try {
				String url = null;
				
				while (toRun) {
					try {
					  url = queue.take();
					  
					  if (updaterJedis == null) {
						  updaterJedis = getJedisInstance();
					  }
					  
					  updateCache(updaterJedis, url);
					
					} catch (LiveDocumentNotAvailableException e) {
						LOGGER.warning("Unable to retrieve robots.txt: " + e.getMessage());
						
					} catch (JedisConnectionException jedisExc) {
						LOGGER.severe("Jedis Exception: " + jedisExc);
						returnBrokenJedis(updaterJedis);
						addUrlLookup(url);
						updaterJedis = null;
					} catch (InterruptedException interrupted) {
						LOGGER.warning("Interrupted: " + interrupted);
					}
				}			
			} finally {
				returnJedisInstance(updaterJedis);
			}
		}
	}

	private String loadRobotsUrl(String url) throws LiveDocumentNotAvailableException {

		int status = 200;
		
		try {
			HttpGet httpGet = new HttpGet(url);
			
			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeoutMS);
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
			
			HttpResponse response = httpClient.execute(httpGet);
			
			String contents = null;
			
			if (response != null) {			
				status = response.getStatusLine().getStatusCode();
			}
			
			if (status == 200) {
				contents = EntityUtils.toString(response.getEntity());
			} else if (status == 502) {
    			throw new LiveDocumentNotAvailableException(url);
    		} else if (status == 504) {
    			//throw new LiveWebTimeoutException("Timeout:" + url);
    			throw new LiveDocumentNotAvailableException("Timeout: " + url);
    		}
			
			if (contents == null) {
				throw throwNotAvail("Null or invalid robots.txt from ", url, status);
			} else {
				LOGGER.fine("Got Robots: " + status + " from " + url);
				return contents;
			}
			
		} catch (IllegalArgumentException il) {
			throw throwNotAvail("IllegalArgumentException: ", url, status);
		} catch (IOException e) {
			throw throwNotAvail("IOException: ", url, status);
		}
	}

	private LiveDocumentNotAvailableException throwNotAvail(String string, String url, int status) {
		String msg = string + " url: " + url + " status: " + status;
		LOGGER.fine(msg);
		return new LiveDocumentNotAvailableException(msg);
	}

	public void shutdown() {
		if (updaterThread != null) {
			updaterThread.toRun = false;
			updaterThread.interrupt();
			try {
				updaterThread.join();
			} catch (InterruptedException e) {
			
			}
		}
		if (jedisPool != null) {
			jedisPool.destroy();
		}
		if (connMan != null) {
			connMan.shutdown();
		}
	}
}
