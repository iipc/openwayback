package org.archive.wayback.accesscontrol.robotstxt;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.webapp.PerformanceLogger;

import redis.clients.jedis.Jedis;

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

	private String host = "localhost";
	private int port = 6379;
	private int db = 0;
	private String password = null;
	
	private LinkedList<Jedis> fastJedisPool = new LinkedList<Jedis>();
		
	private int maxJedisInitTries = 15;
	private int maxJedisCount = 300;
	
	private final static Logger LOGGER = 
		Logger.getLogger(RedisConnectionManager.class.getName());
			
	public RedisConnectionManager()
    {
		LOGGER.setLevel(Level.FINER);	
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
						jedis = fastJedisPool.removeLast();
						poolSize = fastJedisPool.size();
					}
				}
				
				if ((jedis != null) && jedis.isConnected()) {
					PerformanceLogger.noteElapsed("JedisGetPool", System.currentTimeMillis() - startTime, "Size: " + poolSize);
					return jedis;
				}
				
				startTime = System.currentTimeMillis();
				
				jedis = new Jedis(host, port);
				
				jedis.connect();
				
				if (password != null) {
					jedis.auth(password);
				}
				
				if (db != 0) {
					jedis.select(db);
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
