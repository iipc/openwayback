package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
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
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisRobotsCache implements LiveWebCache {

	private final static Logger LOGGER = Logger
			.getLogger(RedisRobotsCache.class.getName());

	private int connectionTimeoutMS = 10000;
	private int socketTimeoutMS = 5000;

	private ClientConnectionManager connMan;
	private HttpClient httpClient;

	final static int ONE_DAY = 60 * 60 * 24;

	private int totalTTL = ONE_DAY * 4;
	private int refreshTTL = ONE_DAY;

	private int notAvailTotalTTL = ONE_DAY;
	private int notAvailRefreshTTL = 60 * 60;
	
	final static String ROBOTS_TOKEN_EMPTY = "0_ROBOTS_EMPTY";
	final static String ROBOTS_TOKEN_TOO_BIG = "0_ROBOTS_TOO_BIG";
	final static String ROBOTS_TOKEN_ERROR = "0_ROBOTS_ERROR-";
	
	final static int MAX_ROBOTS_SIZE = 500000;

	private RedisConnectionManager redisConn;

	private RedisUpdater updaterThread;

	public RedisRobotsCache(RedisConnectionManager redisConn) {
		LOGGER.setLevel(Level.FINER);

		this.redisConn = redisConn;

		initHttpClient();

		updaterThread = new RedisUpdater();
		updaterThread.start();
	}

	protected void initHttpClient() {
		connMan = new ThreadSafeClientConnManager();

		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeoutMS);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);

		httpClient = new DefaultHttpClient(connMan, params);
	}

	public Resource getCachedResource(URL url, long maxCacheMS,
				boolean bUseOlder) throws LiveDocumentNotAvailableException,
				LiveWebCacheUnavailableException, LiveWebTimeoutException,
				IOException {
			
		Jedis jedis = null;
		
		try {
			jedis = redisConn.getJedisInstance();			
			return getRobots(jedis, url.toExternalForm());
			
		} catch (JedisConnectionException jedisExc) {
			LOGGER.severe("Jedis Exception: " + jedisExc);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
			throw new LiveWebCacheUnavailableException(jedisExc.toString());	
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
	}

	public Resource getRobots(Jedis jedis, String urlKey)
			throws LiveDocumentNotAvailableException {

		String robotsFile = null;

		robotsFile = jedis.get(urlKey);

		if ((robotsFile == null) || robotsFile.equals(ROBOTS_TOKEN_TOO_BIG)) {
			
			LOGGER.info("UNCACHED Robots: " + urlKey);

			robotsFile = updateCache(jedis, urlKey);
			
			if (robotsFile == null) {
				throw new LiveDocumentNotAvailableException("Error Loading Live Robots");	
			}
			
		} else if (robotsFile.startsWith(ROBOTS_TOKEN_ERROR)) {
			
			long ttl = jedis.ttl(urlKey);
			LOGGER.info("Cached Robots NOT AVAIL " + urlKey + " TTL: " + ttl);

			if ((notAvailTotalTTL - ttl) >= notAvailRefreshTTL) {
				LOGGER.info("Refreshing NOT AVAIL robots: "
						+ (notAvailTotalTTL - ttl) + ">=" + notAvailRefreshTTL);
				updaterThread.addUrlLookup(urlKey);
				jedis = null;
			}

			throw new LiveDocumentNotAvailableException(urlKey);

		} else {
			long ttl = jedis.ttl(urlKey);
			
			LOGGER.info("Cached Robots: " + urlKey + " TTL: " + ttl);

			if ((totalTTL - ttl) >= refreshTTL) {
				LOGGER.info("Refreshing robots: " + (totalTTL - ttl) + ">="
						+ refreshTTL);
				updaterThread.addUrlLookup(urlKey);
				jedis = null;
			}
			
			if (robotsFile.equals(ROBOTS_TOKEN_EMPTY)) {
				robotsFile = "";
			}
		}

		return new RobotsTxtResource(robotsFile);
	}
	
	public String updateRedisCache(Jedis jedis, String url, RobotResponse robotResponse)
	{
		if (robotResponse.isValid()) {
			String contents = robotResponse.contents;
			
			if (contents.isEmpty()) {
				jedis.setex(url, totalTTL, ROBOTS_TOKEN_EMPTY);
			} else if (contents.length() > MAX_ROBOTS_SIZE) {
				jedis.setex(url, totalTTL, ROBOTS_TOKEN_TOO_BIG);
			} else {
				jedis.setex(url, totalTTL, contents);
			}
			
			return contents;
		} else {
			jedis.setex(url, notAvailTotalTTL, ROBOTS_TOKEN_ERROR + robotResponse.status);
			return null;
		}
	}

	private String updateCache(Jedis jedis, String url) {
		
		RobotResponse robotResponse = loadRobotsUrl(url);
		
		String contents = updateRedisCache(jedis, url, robotResponse);
		
		return contents;
	}

	class RedisUpdater extends Thread {
		private LinkedBlockingQueue<String> queue;
		private boolean toRun;

		private RedisUpdater() {
			this.toRun = true;
			this.queue = new LinkedBlockingQueue<String>();
		}

		public void addUrlLookup(String url) {
			try {
				if (!queue.contains(url)) {
					queue.put(url);
				}
			} catch (InterruptedException ignore) {
				// ignore?
			}
		}

		@Override
		public void run() {
			Jedis updaterJedis = null;

			try {
				String url = null;

				while (toRun) {
					try {
						url = queue.take();

						if (updaterJedis == null) {
							updaterJedis = redisConn.getJedisInstance();
						}

						if (updateCache(updaterJedis, url) == null) {
							LOGGER.warning("Unable to retrieve robots.txt");					
						}
						
						connMan.closeExpiredConnections();

					} catch (JedisConnectionException jedisExc) {
						LOGGER.severe("Jedis Exception: " + jedisExc);
						redisConn.returnBrokenJedis(updaterJedis);
						addUrlLookup(url);
						updaterJedis = null;
					} catch (InterruptedException interrupted) {
						LOGGER.warning("Interrupted: " + interrupted);
					}
				}
			} finally {
				redisConn.returnJedisInstance(updaterJedis);
			}
		}
	}
	
	class RobotResponse
	{
		String contents;
		int status;
		
		RobotResponse(String contents, int status)
		{
			this.contents = contents;
			this.status = status;
		}
		
		boolean isValid()
		{
			return (contents != null) && (status == 200);
		}
	}
	
	private ByteArrayOutputStream readMaxBytes(InputStream input, int max) throws IOException
	{
		byte[] byteBuff = new byte[8192];
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		int totalRead = 0;
			
		while (true) {
			int toRead = Math.min(byteBuff.length, max - totalRead);
			
			if (toRead <= 0) {
				break;
			}
			
			int numRead = input.read(byteBuff, 0, toRead);
			
			if (numRead < 0) {
				break;
			}
			
			totalRead += numRead;
			
			baos.write(byteBuff, 0, numRead);
		}
		
		return baos;
	}

	private RobotResponse loadRobotsUrl(String url) {

		int status = 200;

		try {
			HttpGet httpGet = new HttpGet(url);

			HttpResponse response = httpClient.execute(httpGet);

			String contents = null;

			if (response != null) {
				status = response.getStatusLine().getStatusCode();
			}

			if (status == 200) {
				HttpEntity entity = response.getEntity();
				
				int numToRead = (int)entity.getContentLength();
				
				if (numToRead < 0) {
					numToRead = 4096;
				} else if (numToRead > MAX_ROBOTS_SIZE) {
					numToRead = MAX_ROBOTS_SIZE;
				}
				
				ByteArrayOutputStream baos = readMaxBytes(entity.getContent(), numToRead);
								
				String charset = EntityUtils.getContentCharSet(entity);
				
				if (charset == null) {
					charset = "UTF-8";
				}
				
				contents = baos.toString(charset);
				
				return new RobotResponse(contents, status);
				
			} else {
				return new RobotResponse(null, status);
			}

		} catch (Exception exc) {
			LOGGER.info("Exception: " + exc + " url: " + url + " status " + status);
			return new RobotResponse(null, status);			
		}
	}

	public void shutdown() {		
		if (updaterThread != null) {
			updaterThread.toRun = false;
			updaterThread.interrupt();
			
			try {
				updaterThread.join(5000);
			} catch (InterruptedException e) {

			}
		}
		
		if (redisConn != null) {
			redisConn.close();
		}
		
		if (connMan != null) {
			connMan.shutdown();
		}
	}
}
