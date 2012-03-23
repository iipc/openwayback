package org.archive.wayback.accesscontrol.robotstxt;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.archive.wayback.webapp.PerformanceLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisConnectionManager {
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getDb() {
		return db;
	}

	public void setDb(int db) {
		this.db = db;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	private String host = "localhost";
	private int port = 6379;
	private int db = 0;
	private String password = null;
	private int timeout = 200;
	
	private int maxJedisCount = 300;
	
	private final static Logger LOGGER = 
		Logger.getLogger(RedisConnectionManager.class.getName());
	
	private JedisPool pool;
			
	public RedisConnectionManager()
    {
		LOGGER.setLevel(Level.FINER);
	}
	
	public void init()
	{
		GenericObjectPool.Config config = new GenericObjectPool.Config();
		config.lifo = false;
		config.maxActive = maxJedisCount;
		config.maxIdle = 100;
		config.testOnBorrow = true;
		config.testOnReturn = false;
		config.testWhileIdle = false;
		
		pool = new JedisPool(config, host, port, timeout, password);
	}
	
	public Jedis getJedisInstance()
	{
		long startTime = System.currentTimeMillis();

		Jedis jedis = pool.getResource();
		
		if (db != 0) {
			jedis.select(db);
		}
		
		PerformanceLogger.noteElapsed("JedisGetResource", System.currentTimeMillis() - startTime);		
		return jedis;
	}
	
	public void returnJedisInstance(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
					
		pool.returnResource(jedis);
	}
	
	public void returnBrokenJedis(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		pool.returnBrokenResource(jedis);
	}
	
	public void close() {
		if (pool != null) {
			pool.destroy();
		}
	}
}
