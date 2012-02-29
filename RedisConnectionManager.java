package org.archive.wayback.accesscontrol.robotstxt;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.webapp.PerformanceLogger;

import redis.clients.jedis.Jedis;

public class RedisConnectionManager {
	
	private String redisHost;
	private int redisPort;
	private int redisDB;
	
	private LinkedList<Jedis> fastJedisPool = new LinkedList<Jedis>();
		
	private int maxJedisInitTries = 15;
	private int maxJedisCount = 500;
	
	private final static Logger LOGGER = 
		Logger.getLogger(RedisConnectionManager.class.getName());
	
	public RedisConnectionManager(String redisHost, int redisPort)
    {
		this(redisHost, redisPort, 0);
	}
		
	public RedisConnectionManager(String redisHost, int redisPort, int redisDB)
    {
		LOGGER.setLevel(Level.FINER);
		
		this.redisHost = redisHost;
		this.redisPort = redisPort;
		this.redisDB = redisDB;			
	}
	
	public String getRedisHost() {
		return redisHost;
	}

	public int getRedisPort() {
		return redisPort;
	}

	public int getRedisDB() {
		return redisDB;
	}

	protected Jedis getJedisInstance()
	{
		Jedis jedis = null;
		int poolSize = 0;
					
		for (int i = 0; i < maxJedisInitTries; i++) {		
			try {
				long startTime = System.currentTimeMillis();
				synchronized (fastJedisPool) {
					if (!fastJedisPool.isEmpty()) {
						jedis = fastJedisPool.pop();
						poolSize = fastJedisPool.size();
					}
				}
				
				if ((jedis != null) && jedis.isConnected() && jedis.ping().equals("PONG")) {
					PerformanceLogger.noteElapsed("JedisGetPool", System.currentTimeMillis() - startTime, "Size: " + poolSize);
					return jedis;
				}
				
				startTime = System.currentTimeMillis();
				
				jedis = new Jedis(redisHost, redisPort);
				jedis.connect();
				if (redisDB != 0) {
					jedis.select(redisDB);
				}
				PerformanceLogger.noteElapsed("JedisGetNew", System.currentTimeMillis() - startTime, "NEW JEDIS");
				return jedis;
			} catch (Exception exc) {
				this.returnBrokenJedis(jedis);
				LOGGER.severe("Retrying Jedis Init: " + exc.toString());
			}
		}
		
		return jedis;
	}
	
	protected void returnJedisInstance(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		int poolSize = 0;
		
		long startTime = System.currentTimeMillis();
		
		synchronized (fastJedisPool) {
			poolSize = fastJedisPool.size();
			if ((maxJedisCount <= 0) || (poolSize < maxJedisCount)) {
				fastJedisPool.push(jedis);
				jedis = null;
			}
		}
		
		// If not null, then still needs closing
		// If null, then put into pool
		// Doing outside of synchronized for max speed
		
		if (jedis != null) {
			closeJedis(jedis);
			PerformanceLogger.noteElapsed("JedisCloseExtra", System.currentTimeMillis() - startTime, "Size: " + poolSize);
		} else {
			PerformanceLogger.noteElapsed("JedisReturnPool", System.currentTimeMillis() - startTime, "Size: " + poolSize);
		}
	}
	
	protected void returnBrokenJedis(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		closeJedis(jedis);
	}
	
	protected void closeJedis(Jedis jedis)
	{
		try {
			if (jedis.isConnected()) {
				jedis.quit();
				jedis.disconnect();
			}
		} catch (Exception exc)
		{
			LOGGER.warning("Jedis Close Exception: " + exc);
		}		
	}

	public void close() {
		synchronized(fastJedisPool) {
			for (Jedis jedis : fastJedisPool) {
				closeJedis(jedis);
			}
			fastJedisPool.clear();
		}
	}
}
