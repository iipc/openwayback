package org.archive.wayback.accesscontrol.robotstxt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.UnknownHostException;
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
import org.archive.wayback.webapp.PerformanceLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisRobotsCache implements LiveWebCache {

	private final static Logger LOGGER = Logger
			.getLogger(RedisRobotsCache.class.getName());

	private int connectionTimeoutMS = 5000;
	private int socketTimeoutMS = 5000;

	private ThreadSafeClientConnManager connMan;
	private HttpClient httpClient;
	
	private String proxyHost;
	private int proxyPort;

	final static int ONE_DAY = 60 * 60 * 24;

	private int totalTTL = ONE_DAY * 4;
	private int refreshTTL = ONE_DAY;

	private int notAvailTotalTTL = ONE_DAY * 2;
	private int notAvailRefreshTTL = ONE_DAY / 2;
	
	final static String ROBOTS_TOKEN_EMPTY = "0_ROBOTS_EMPTY";
	
	final static String ROBOTS_TOKEN_ERROR = "0_ROBOTS_ERROR-";
	final static String ROBOTS_TOKEN_ERROR_UNKNOWN = "0_ROBOTS_ERROR-0";
	
	final static int LIVE_TIMEOUT_ERROR = 900;
	final static int LIVE_HOST_ERROR = 910;
	
//	final static String ROBOTS_ERROR_LIVE_TIMEOUT = ROBOTS_TOKEN_ERROR + LIVE_TIMEOUT_ERROR;
//	final static String ROBOTS_ERROR_LIVE_HOST_UNKNOWN = ROBOTS_TOKEN_ERROR + LIVE_HOST_ERROR;
	
	final static int MAX_ROBOTS_SIZE = 500000;

	private RedisConnectionManager redisConn;

	public RedisRobotsCache(RedisConnectionManager redisConn) {
		LOGGER.setLevel(Level.FINER);

		this.redisConn = redisConn;

		initHttpClient();
	}

	protected void initHttpClient() {
		connMan = new ThreadSafeClientConnManager();
		connMan.setDefaultMaxPerRoute(10);
		connMan.setMaxTotal(1000);

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

	public Resource getCachedResource(URL urlURL, long maxCacheMS,
				boolean bUseOlder) throws LiveDocumentNotAvailableException,
				LiveWebCacheUnavailableException, LiveWebTimeoutException,
				IOException {
					
		String robotsFile = null;
		Jedis jedis = null;
		String url = urlURL.toExternalForm();
		
		try {
			jedis = redisConn.getJedisInstance();
	
			long startTime = System.currentTimeMillis();
			robotsFile = jedis.get(url);
			PerformanceLogger.noteElapsed("RedisGet", System.currentTimeMillis() - startTime, ((robotsFile == null) ? "REDIS MISS: " : "REDIS HIT: ") + url);

	
			if (robotsFile == null) {
				//PerformanceLogger.noteElapsed("RedisTTL", System.currentTimeMillis() - startTime, "UNCACHED: " + url);

				redisConn.returnJedisInstance(jedis);
				jedis = null;
				
				RobotResponse robotResponse = loadRobotsUrl(url);
					
				updateCache(robotResponse, url, null);
				
				if (!robotResponse.isValid()) {
					throw new LiveWebTimeoutException("Error Loading Live Robots");	
				}
				
				robotsFile = robotResponse.contents;
				
			} else if (robotsFile.startsWith(ROBOTS_TOKEN_ERROR)) {
				startTime = System.currentTimeMillis();
				long ttl = jedis.ttl(url);
				PerformanceLogger.noteElapsed("RedisTTL", System.currentTimeMillis() - startTime, "NOTAVAIL: " + url);
				
				redisConn.returnJedisInstance(jedis);
				jedis = null;
				
				//LOGGER.info("Cached Robots NOT AVAIL " + url + " TTL: " + ttl);
	
				if ((notAvailTotalTTL - ttl) >= notAvailRefreshTTL) {
					LOGGER.info("Refreshing NOT AVAIL robots: "
							+ (notAvailTotalTTL - ttl) + ">=" + notAvailRefreshTTL);
					//updaterThread.addUrlLookup(url);
					new RedisInstaUpdater(url, robotsFile).start();
				}
				
				throw new LiveDocumentNotAvailableException(url);
	
			} else {
				startTime = System.currentTimeMillis();
				long ttl = jedis.ttl(url);
				PerformanceLogger.noteElapsed("RedisTTL", System.currentTimeMillis() - startTime, "ISCACHED: " + url + " " + robotsFile.length());

				redisConn.returnJedisInstance(jedis);
				jedis = null;
				
				//LOGGER.info("Cached Robots: " + url + " TTL: " + ttl);
	
				if ((totalTTL - ttl) >= refreshTTL) {
					LOGGER.info("Refreshing robots: " + (totalTTL - ttl) + ">="
							+ refreshTTL);
					new RedisInstaUpdater(url, robotsFile).start();
				}
				
				if (robotsFile.equals(ROBOTS_TOKEN_EMPTY)) {
					robotsFile = "";
				}
			}
		} catch (JedisConnectionException jce) {
			LOGGER.severe("Jedis Exception: " + jce);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
			throw new LiveWebCacheUnavailableException(jce.toString());			
		} finally {
			redisConn.returnJedisInstance(jedis);
		}

		return new RobotsTxtResource(robotsFile);
	}
	
	private boolean updateCache(RobotResponse robotResponse, String url, String currentValue) {		
		String contents = null;
		Jedis jedis = null;
		
		String newRedisValue = null;
		int newTTL = 0;
		
		if (robotResponse.isValid()) {
			contents = robotResponse.contents;
			newTTL = totalTTL;
			
			if (contents.isEmpty()) {
				newRedisValue = ROBOTS_TOKEN_EMPTY;
			} else if (contents.length() > MAX_ROBOTS_SIZE) {
				newRedisValue = contents.substring(0, MAX_ROBOTS_SIZE);
			} else {
				newRedisValue = contents;
			}
			
		} else {
			newTTL = notAvailTotalTTL;
			newRedisValue = ROBOTS_TOKEN_ERROR + robotResponse.status;
		}
		
		
		if (currentValue != null) {
			if (currentValue.equals(newRedisValue)) {
				return false;
			}
			
			// Don't override a valid robots with a timeout error
			if ((robotResponse.status == LIVE_TIMEOUT_ERROR) && !currentValue.startsWith(ROBOTS_TOKEN_ERROR)) {
				newRedisValue = currentValue;
				newTTL = notAvailTotalTTL;
				LOGGER.info("REFRESH TIMEOUT: Keeping same robots for " + url + ", refresh timed out");
			}
		}
				
		try {
			jedis = redisConn.getJedisInstance();
			jedis.setex(url, newTTL, newRedisValue);
			
		} catch (JedisConnectionException jce) {
			LOGGER.severe("Jedis Exception: " + jce);
			redisConn.returnBrokenJedis(jedis);
			jedis = null;
			return false;
			
		} finally {
			redisConn.returnJedisInstance(jedis);
		}
		
		return true;
	}
	
	class RedisInstaUpdater extends Thread
	{
		String url;
		String current;
		
		RedisInstaUpdater(String url, String current)
		{
			this.url = url;
			this.current = current;
		}
		
		public void run()
		{
			long startTime = System.currentTimeMillis();
			RobotResponse robotResponse = loadRobotsUrl(url);
			updateCache(robotResponse, url, current);
			PerformanceLogger.noteElapsed("AsyncRedisUpdate", System.currentTimeMillis() - startTime, url);
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
				
				int numToRead = (int)entity.getContentLength();
				
				if ((numToRead <= 0) || (numToRead > MAX_ROBOTS_SIZE)) {
					numToRead = MAX_ROBOTS_SIZE;
				}

				ByteArrayOutputStream baos = readMaxBytes(entity.getContent(), numToRead);
								
				String charset = EntityUtils.getContentCharSet(entity);
				
				if (charset == null) {
					charset = "utf-8";
				}
				
				contents = baos.toString(charset);
			}

		} catch (Exception exc) {
			
			if (exc instanceof InterruptedIOException) {
				status = LIVE_TIMEOUT_ERROR; //Timeout (gateway timeout)
			} else if (exc instanceof UnknownHostException) {
				status = LIVE_HOST_ERROR;
			}
			
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
//		if (updaterThread != null) {
//			updaterThread.toRun = false;
//			updaterThread.interrupt();
//			
//			try {
//				updaterThread.join(5000);
//			} catch (InterruptedException e) {
//
//			}
//			updaterThread = null;
//		}
		
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
