package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.archive.util.zip.OpenJDK7GZIPInputStream;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisRobotsLogic {
	
	private final static Logger LOGGER = 
		Logger.getLogger(RedisRobotsLogic.class.getName());
	
	private final static int MIN_GZIP_SIZE = 20;
	
	final static String UTF8 = "UTF-8";
	
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
			redisConn.returnBrokenJedis(jedis);
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
			redisConn.returnBrokenJedis(jedis);
			LOGGER.log(Level.SEVERE, "Jedis Exception", jce);
			jedis = null;
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
	}
	
	protected static class RedisValue {
		String value;
		long ttl;
		
		RedisValue(String value, long ttl)
		{
			this.value = value;
			this.ttl = ttl;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof RedisValue)) return false;
			RedisValue rv = (RedisValue)obj;
			return (ttl == rv.ttl &&
					(value != null && value.equals(rv.value) || rv.value == null));
		}

		public String toString() {
			return String.format("RedisValue(%s, %d)", value == null ? "null"
					: "\"" + value + "\"", ttl);
		}
	}
	
	protected static class KeyRedisValue extends RedisValue
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
					//String value = jedis.get(key);
					byte[] binValue = null;
					
					try {
						binValue = jedis.get(key.getBytes(UTF8));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					if (binValue == null) {
						return null;
					}
					
					String stringValue = null;
					
					try {
						if (isGzipStream(binValue)) {
							InputStream stream = new OpenJDK7GZIPInputStream(new ByteArrayInputStream(binValue));
							stringValue = IOUtils.toString(stream, UTF8);
						}
					} catch (IOException e) {

					}
					
					if (stringValue == null) {					
						try {
							stringValue = new String(binValue, UTF8);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					long ttl = jedis.ttl(key);
					return new RedisValue(stringValue, ttl);
				}
			});
			return value;
			
		} finally {
			//PerformanceLogger.noteElapsed("RedisGetTTL", System.currentTimeMillis() - startTime, ((value == null) ? "REDIS MISS: " : "REDIS HIT: ") + key);
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
			//PerformanceLogger.noteElapsed("RedisMultiGetTTL", System.currentTimeMillis() - startTime, Arrays.toString(keys));
		}
		
		return values;
	}
	
	public void updateValue(final String url, final RedisValue value)
	{
		updateValue(url, value, false);
	}
	
	public void updateValue(final String url, final RedisValue value, final boolean gzip)
	{
		this.runJedisCmd(new JedisRunnerVoid()
		{
			public void run(Jedis jedis)
			{
				if (value.value == null) {
					jedis.expire(url, (int)value.ttl);
				} else if (!gzip || (value.value.length() < MIN_GZIP_SIZE)) {
					jedis.setex(url, (int)value.ttl, value.value);
				} else {
					try {
						byte[] array = value.value.getBytes(UTF8);
						ByteArrayOutputStream buff = new ByteArrayOutputStream(array.length + 8);
						GZIPOutputStream stream = new GZIPOutputStream(buff) {
						  {
							        def.setLevel(Deflater.BEST_COMPRESSION);
						  }
						};
						
						stream.write(array);
						stream.finish();
						
						jedis.setex(url.getBytes(UTF8), (int)value.ttl, buff.toByteArray());
						
					} catch (IOException io) {
						io.printStackTrace();
					}
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
				if (jedis.llen(list) < maxSize) {
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
				if (values == null) {
					return null;
				}
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
	
	public static boolean isGzipStream(byte[] bytes) {
		if (bytes.length < 2) {
			return false;
		}
		
		int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
		return (OpenJDK7GZIPInputStream.GZIP_MAGIC == head);
	}
}
