package org.archive.wayback.accesscontrol.robotstxt;

import java.util.LinkedList;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisConnectionManager {
	
	private String redisHost;
	private int redisPort;
	private int redisDB;
	
	private LinkedList<Jedis> fastJedisPool = new LinkedList<Jedis>();
	private int jedisCount = 0;
	private int fastJedisPoolSize = 0;
	
	private JedisPool jedisPool;
	
	private final static Logger LOGGER = 
		Logger.getLogger(RedisConnectionManager.class.getName());
	
	boolean usePool = false;
	
	public RedisConnectionManager(String redisHost, int redisPort, int redisDB)
    {
		this(redisHost, redisPort, redisDB, null);
    }

	public RedisConnectionManager(String redisHost, int redisPort, int redisDB, JedisPoolConfig config)
    {
		LOGGER.setLevel(Level.FINER);
		
		this.redisHost = redisHost;
		this.redisPort = redisPort;
		this.redisDB = redisDB;
		
		if (!usePool) {
			return;
		}
	
		if (config == null) {
			config = new JedisPoolConfig();
	        config.setMaxActive(50);
	        config.setTestOnBorrow(true);
	        config.setTestWhileIdle(true);
	        config.setTestOnReturn(true);
		}
        
		LOGGER.info("Initializing Jedis Pool: Host = " + redisHost + " Port: " + redisPort);
		
		this.jedisPool = new JedisPool(config, redisHost, redisPort);
			
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
		
		try {
			if (usePool) {
				jedis = jedisPool.getResource();
			} else {
				synchronized (fastJedisPool) {
					if (!fastJedisPool.isEmpty()) {
						jedis = fastJedisPool.removeLast();
					}
					LOGGER.info("Fast Pool Size: " + --fastJedisPoolSize);
				}
				
				if (jedis != null) {
					if (!jedis.isConnected()) {
						jedis = null;
					}
				}
				
				if (jedis == null) {
					jedis = new Jedis(redisHost, redisPort);
					jedis.connect();
				}
			}
			
			if (redisDB != 0) {
				jedis.select(redisDB);
			}
			
			LOGGER.info("GET Jedis Instance: " + (++jedisCount));
			return jedis;
		} catch (RuntimeException rte) {
			this.returnBrokenJedis(jedis);
			LOGGER.severe(rte.toString());
			return null;
		}
	}
	
	protected void returnJedisInstance(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		if (usePool) {
			if ((jedisPool != null)) {
				jedisPool.returnResource(jedis);
			}	
		} else {
			synchronized (fastJedisPool) {
				fastJedisPool.addFirst(jedis);
				LOGGER.info("Fast Pool Size: " + ++fastJedisPoolSize);
			}
		}
		
		LOGGER.info("RET Jedis Instance: " + --jedisCount);
	}
	
	protected void returnBrokenJedis(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		if (usePool) {
			if ((jedisPool != null)) {
				jedisPool.returnBrokenResource(jedis);
			}	
		} else {
			closeJedis(jedis);
		}
		
		LOGGER.info("RET Broken Jedis: " + --jedisCount);
	}
	
	protected void closeJedis(Jedis jedis)
	{
		try {
			if (jedis.isConnected()) {
				jedis.quit();
				jedis.disconnect();
			}
		} catch (Exception exc){}		
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
