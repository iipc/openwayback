package org.archive.wayback.accesscontrol.robotstxt;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;

public class RedisConnectionManager {
	
	private String redisHost;
	private int redisPort;
	private int redisDB;
	
	private LinkedList<Jedis> fastJedisPool = new LinkedList<Jedis>();
	private int activeJedisCount = 0;
	private int pooledJedisCount = 0;
		
	private int maxJedisInitTries = 15;
	private int maxJedisCount = 100;
	
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
					
		for (int i = 0; i < maxJedisInitTries; i++) {		
			try {
				synchronized (fastJedisPool) {
					if (!fastJedisPool.isEmpty()) {
						jedis = fastJedisPool.removeLast();
						LOGGER.info("Fast Pool Size: " + --pooledJedisCount);
					}
				}
				
				if (jedis != null) {
					if (!jedis.isConnected()) {
						jedis = null;
					}
				}
				
				if (jedis == null) {
					jedis = new Jedis(redisHost, redisPort);
					jedis.connect();
					if (redisDB != 0) {
						jedis.select(redisDB);
					}
				}
				
				LOGGER.info("GET Jedis Instance: " + (++activeJedisCount));
				return jedis;
			} catch (Exception exc) {
				this.returnBrokenJedis(jedis);
				LOGGER.severe(exc.toString());
			}
		}
		
		return null;
	}
	
	protected void returnJedisInstance(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		if ((maxJedisCount > 0) && (pooledJedisCount >= maxJedisCount)) {
			closeJedis(jedis);
		} else {	
			synchronized (fastJedisPool) {
				fastJedisPool.addFirst(jedis);
				LOGGER.info("Fast Pool Size: " + ++pooledJedisCount);
			}
		}
		
		LOGGER.info("RET Jedis Instance: " + --activeJedisCount);
	}
	
	protected void returnBrokenJedis(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		closeJedis(jedis);
		
		LOGGER.info("RET Broken Jedis: " + --activeJedisCount);
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
