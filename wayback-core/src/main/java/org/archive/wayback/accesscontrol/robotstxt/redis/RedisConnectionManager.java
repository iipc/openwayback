package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.io.PrintWriter;
import java.util.logging.Logger;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisConnectionManager {
	
	private final static Logger LOGGER = Logger
	.getLogger(RedisConnectionManager.class.getName());
	
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
	private int timeout = 2000;
	
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
	
	//private JedisPool pool;
	private GenericObjectPool goPool;
	
	private class JedisValid extends Jedis
	{
		boolean valid = true;
		
		public JedisValid(String host, int port, int timeout) {
			super(host, port, timeout);
		}
	}
	
    private class JedisFactory extends BasePoolableObjectFactory {

        public Object makeObject() throws Exception {
            final Jedis jedis;
            jedis = new JedisValid(host, port, timeout);
            if (password != null) {
            	jedis.getClient().setPassword(password);
            }
            jedis.connect();
            return jedis;
        }

        public void destroyObject(final Object obj) throws Exception {
            if (obj instanceof JedisValid) {
                final JedisValid jedis = (JedisValid) obj;
                if (jedis.isConnected()) {
                    try {
                    	if (jedis.valid) {
	                        try {
	                            jedis.quit();
	                        } catch (Exception e) {
	                        }
                    	}
                        jedis.disconnect();
                    } catch (Exception e) {
                    	LOGGER.warning("REDISCONN: DISCONNECT: " + e);
                    }
                }
            }
        }

        public boolean validateObject(final Object obj) {
            if (obj instanceof JedisValid) {
                final JedisValid jedis = (JedisValid) obj;
                try {
                    return jedis.isConnected();// && jedis.ping().equals("PONG");
                } catch (final Exception e) {
                	jedis.valid = false;
                	LOGGER.warning("REDISCONN: VALIDATE: " + e);
                    return false;
                }
            } else {
                return false;
            }
        }
    }
			
	public RedisConnectionManager()
    {
	}
	
	public void init()
	{
		if (config == null) {
			config = new JedisPoolConfig();
			config.lifo = true;
			config.timeBetweenEvictionRunsMillis = -1;
			config.maxActive = connections;
			config.maxIdle = connections;
			config.testOnBorrow = true;
			config.testOnReturn = false;
			config.testWhileIdle = true;
			config.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
		}
		
		goPool = new GenericObjectPool(new JedisFactory(), config);
		//pool = new JedisPool(config, host, port, timeout, password);
	}
	
	public Jedis getJedisInstance()
	{
		long startTime = System.currentTimeMillis();

		//Jedis jedis = pool.getResource();
		Jedis jedis;
		
		try {
			jedis = (Jedis)goPool.borrowObject();
		} catch (Exception e) {
			if (e instanceof JedisConnectionException) {
				throw (JedisConnectionException)e;
			}
			throw new JedisConnectionException("Connection: ", e);
		}
		
		if (db != 0) {
			jedis.select(db);
		}
		
		//PerformanceLogger.noteElapsed("JedisGetResource", System.currentTimeMillis() - startTime);		
		return jedis;
	}
	
	public void returnJedisInstance(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
					
		//pool.returnResource(jedis);
		try {
			goPool.returnObject(jedis);
		} catch (Exception e) {
        	LOGGER.warning("REDISCONN: RETURN: " + e);
		}
	}
	
	public void returnBrokenJedis(Jedis jedis)
	{
		if (jedis == null) {
			return;
		}
		
		((JedisValid)jedis).valid = false;
		
		try {
			goPool.invalidateObject(jedis);
		} catch (Exception e) {
        	LOGGER.warning("REDISCONN: BROKEN: " + e);
		}
		//pool.returnBrokenResource(jedis);
	}
	
	public void close() {
		if (goPool != null) {
			try {
				goPool.close();
			} catch (Exception e) {
	        	LOGGER.warning("REDISCONN: CLOSE: " + e);
			}
		}
	}
	
	public void appendLogInfo(PrintWriter info)
	{
		info.println("  Jedis Active: " + goPool.getNumActive());
		info.println("  Jedis Idle: " + goPool.getNumIdle());
	}
}
