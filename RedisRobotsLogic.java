package org.archive.wayback.accesscontrol.robotstxt;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.webapp.PerformanceLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisRobotsLogic {
	
	private final static Logger LOGGER = 
		Logger.getLogger(RedisRobotsLogic.class.getName());
	
	private RedisConnectionManager redisConn;
	
	RedisRobotsLogic(RedisConnectionManager redisConn)
	{
		this.redisConn = redisConn;		
	}

	public <T> T runJedisCmd(JedisRunner<T> runner) throws LiveWebCacheUnavailableException
	{
		Jedis jedis = null;
		
		try {
			jedis = redisConn.getJedisInstance();
			return runner.run(jedis);
		} catch (JedisConnectionException jce) {
			redisConn.returnJedisInstance(jedis);
			LOGGER.log(Level.SEVERE, "Jedis Exception", jce);
			jedis = null;
			throw new LiveWebCacheUnavailableException("No Jedis");
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
	}
	
	public void runJedisCmd(JedisRunnerVoid runner)
	{
		Jedis jedis = null;
		
		try {
			jedis = redisConn.getJedisInstance();
			runner.run(jedis);
		} catch (JedisConnectionException jce) {
			redisConn.returnJedisInstance(jedis);
			LOGGER.log(Level.SEVERE, "Jedis Exception", jce);
			jedis = null;
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
	}
	
	static class RedisValue
	{
		String value;
		long ttl;
		
		RedisValue(String value, long ttl)
		{
			this.value = value;
			this.ttl = ttl;
		}
	}
	
	static class KeyRedisValue extends RedisValue
	{
		String key;
		
		KeyRedisValue(String key, String value, long ttl)
		{
			super(value, ttl);
			this.key = key;
		}
	}
	
	public RedisValue getValue(final String key) throws LiveWebCacheUnavailableException
	{
		long startTime = System.currentTimeMillis();
		RedisValue value = null;
		
		try {
			value = this.runJedisCmd(new JedisRunner<RedisValue>()
			{
				public RedisValue run(Jedis jedis)
				{
					String value = jedis.get(key);
					if (value == null) {
						return null;
					}
					long ttl = jedis.ttl(key);
					return new RedisValue(value, ttl);
				}
			});
			return value;
			
		} finally {
			PerformanceLogger.noteElapsed("RedisGetTTL", System.currentTimeMillis() - startTime, ((value == null) ? "REDIS MISS: " : "REDIS HIT: ") + key);
		}
	}
	
	public List<RedisValue> getValue(final String[] keys) throws LiveWebCacheUnavailableException
	{
		long startTime = System.currentTimeMillis();

		List<RedisValue> values = null;
		
		try {
			values = this.runJedisCmd(new JedisRunner<List<RedisValue>>()
			{
				public List<RedisValue> run(Jedis jedis)
				{
					List<String> values = jedis.mget(keys);
					List<RedisValue> redisValues = new LinkedList<RedisValue>();
					int index = 0;
					for (String value : values) {
						if (value == null) {
							redisValues.add(null);
						} else {
							long ttl = jedis.ttl(keys[index]);
							redisValues.add(new RedisValue(value, ttl));
						}
						index++;
					}
					return redisValues;
				}
			});
		} finally {		
			PerformanceLogger.noteElapsed("RedisMultiGetTTL", System.currentTimeMillis() - startTime, Arrays.toString(keys));
		}
		
		return values;
	}
	
	public void updateValue(final String url, final RedisValue value)
	{
		this.runJedisCmd(new JedisRunnerVoid()
		{
			public void run(Jedis jedis)
			{
				if (value.value == null) {
					jedis.expire(url, (int)value.ttl);
				} else {
					jedis.setex(url, (int)value.ttl, value.value);
				}
			}
		});
	}
	
	public void pushKey(final String list, final String key)
	{
		this.runJedisCmd(new JedisRunnerVoid()
		{
			public void run(Jedis jedis)
			{
				jedis.rpush(list, key);				
			}
		});
	}
	
	public void pushKey(final String list, final String key, final int maxSize)
	{
		this.runJedisCmd(new JedisRunnerVoid()
		{
			public void run(Jedis jedis)
			{
				if (jedis.llen(list) <= maxSize) {
					jedis.rpush(list, key);
				}
			}
		});
	}
	
	public KeyRedisValue popKeyAndGet(final String list) throws LiveWebCacheUnavailableException
	{
		return this.runJedisCmd(new JedisRunner<KeyRedisValue>()
		{
			public KeyRedisValue run(Jedis jedis)
			{
				List<String> values = jedis.blpop(0, list);
				String key = values.get(1);
				String value = jedis.get(key);
				if (value == null) {
					return null;
				}
				long ttl = jedis.ttl(key);
				return new KeyRedisValue(key, value, ttl);
			}
		});
	}
	
	public void close()
	{
		redisConn.close();
	}
	
	interface JedisRunner<T>
	{
		public T run(Jedis jedis);
	}
	
	interface JedisRunnerVoid
	{
		public void run(Jedis jedis);
	}

	public void appendLogInfo(PrintWriter info) {
		redisConn.appendLogInfo(info);		
	}
}
