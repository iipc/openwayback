package org.archive.wayback.accesscontrol.robotstxt;

import org.archive.wayback.webapp.PerformanceLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
	
	private int connections = 50;
	
	private JedisPoolConfig config = null;
	
	public JedisPoolConfig getConfig() {
		return config;
	}

	public void setConfig(JedisPoolConfig config) {
		this.config = config;
	}

	public int getConnections() {
		return connections;
	}

	public void setConnections(int connections) {
		this.connections = connections;
	}
	
	private JedisPool pool;
			
	public RedisConnectionManager()
    {
	}
	
	public void init()
	{
		if (config == null) {
			config = new JedisPoolConfig();
			config.lifo = true;
			config.maxActive = connections;
			config.maxIdle = connections;
			config.testOnBorrow = true;
			config.testOnReturn = false;
			config.testWhileIdle = true;
		}
		
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
