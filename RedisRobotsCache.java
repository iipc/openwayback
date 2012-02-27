package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpHost;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
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
	private int socketTimeoutMS = 10000;

	private ThreadSafeClientConnManager connMan;
	private HttpClient httpClient;
	
	private String proxyHost;
	private int proxyPort;

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
		connMan.setDefaultMaxPerRoute(10);
		connMan.setMaxTotal(100);

		HttpParams params = new BasicHttpParams();
		params.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeoutMS);
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeoutMS);
		
		if ((proxyHost != null) && (proxyPort != 0)) {
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
			LOGGER.info("=== HTTP Proxy through: " + proxyHost + ":" + proxyPort);
		}

		httpClient = new DefaultHttpClient(connMan, params);
	}

	public Resource getCachedResource(URL url, long maxCacheMS,
				boolean bUseOlder) throws LiveDocumentNotAvailableException,
				LiveWebCacheUnavailableException, LiveWebTimeoutException,
				IOException {
			
		Jedis jedis = null;
		
		try {
			jedis = redisConn.getJedisInstance();
			String urlString = url.toExternalForm();
			return getRobots(jedis, urlString);
			
		} catch (JedisConnectionException jedisExc) {
			LOGGER.severe("Jedis Exception: " + jedisExc);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
			throw new LiveWebCacheUnavailableException(jedisExc.toString());	
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
	}

	public Resource getRobots(Jedis jedis, String url)
			throws LiveDocumentNotAvailableException {

		String robotsFile = null;

		robotsFile = jedis.get(url);

		if ((robotsFile == null) || robotsFile.equals(ROBOTS_TOKEN_TOO_BIG)) {
			
			LOGGER.info("UNCACHED Robots: " + url);

			robotsFile = updateCache(jedis, url);
			
			if (robotsFile == null) {
				throw new LiveDocumentNotAvailableException("Error Loading Live Robots");	
			}
			
		} else if (robotsFile.startsWith(ROBOTS_TOKEN_ERROR)) {
			
			long ttl = jedis.ttl(url);
			LOGGER.info("Cached Robots NOT AVAIL " + url + " TTL: " + ttl);

			if ((notAvailTotalTTL - ttl) >= notAvailRefreshTTL) {
				LOGGER.info("Refreshing NOT AVAIL robots: "
						+ (notAvailTotalTTL - ttl) + ">=" + notAvailRefreshTTL);
				updaterThread.addUrlLookup(url);
				jedis = null;
			}

			throw new LiveDocumentNotAvailableException(url);

		} else {
			long ttl = jedis.ttl(url);
			
			LOGGER.info("Cached Robots: " + url + " TTL: " + ttl);

			if ((totalTTL - ttl) >= refreshTTL) {
				LOGGER.info("Refreshing robots: " + (totalTTL - ttl) + ">="
						+ refreshTTL);
				updaterThread.addUrlLookup(url);
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

		int status = 0;
		String contents = null;
		HttpGet httpGet = null;
		
		try {
			httpGet = new HttpGet(url);
			HttpContext context = new BasicHttpContext();

			HttpResponse response = httpClient.execute(httpGet, context);

			if (response != null) {
				status = response.getStatusLine().getStatusCode();
			}

			if (status == 200) {
				HttpEntity entity = response.getEntity();
				
				int numToRead = Math.min((int)entity.getContentLength(), MAX_ROBOTS_SIZE);

				ByteArrayOutputStream baos = readMaxBytes(entity.getContent(), numToRead);
								
				String charset = EntityUtils.getContentCharSet(entity);
				
				if (charset == null) {
					charset = "utf-8";
				}
				
				contents = baos.toString(charset);
			}

		} catch (Exception exc) {
			LOGGER.info("Exception: " + exc + " url: " + url + " status " + status);		
		
		} finally {
			httpGet.abort();
		}
		
		return new RobotResponse(contents, status);
	}
	
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if (colonIdx > 0) {
    		proxyHost = hostPort.substring(0,colonIdx);
    		proxyPort = Integer.valueOf(hostPort.substring(colonIdx+1));   		
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
			updaterThread = null;
		}
		
		if (redisConn != null) {
			redisConn.close();
			redisConn = null;
		}
		
		if (connMan != null) {
			connMan.shutdown();
			connMan = null;
		}
	}
}
