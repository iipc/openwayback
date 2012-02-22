package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private final String robotsNotAvailTxt = "0_Invalid_0";

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

		if (robotsFile == null) {
			LOGGER.info("UNCACHED Robots: " + urlKey);

			robotsFile = updateCache(jedis, urlKey);
		} else if (robotsFile.equals(robotsNotAvailTxt)) {
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
		}

		return new RobotsTxtResource(robotsFile);
	}

	private String updateCache(Jedis jedis, String url)
			throws LiveDocumentNotAvailableException {
		try {
			String robotsFile = loadRobotsUrl(url);
			jedis.setex(url, totalTTL, robotsFile);
			return robotsFile;
		} catch (LiveDocumentNotAvailableException notAvail) {
			jedis.setex(url, notAvailTotalTTL, robotsNotAvailTxt);
			throw notAvail;
		}
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

						updateCache(updaterJedis, url);

					} catch (LiveDocumentNotAvailableException e) {
						LOGGER.warning("Unable to retrieve robots.txt: "
								+ e.getMessage());

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

	private String loadRobotsUrl(String url)
			throws LiveDocumentNotAvailableException {

		int status = 200;

		try {
			HttpGet httpGet = new HttpGet(url);

			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT,
					socketTimeoutMS);
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
					connectionTimeoutMS);

			HttpResponse response = httpClient.execute(httpGet);

			String contents = null;

			if (response != null) {
				status = response.getStatusLine().getStatusCode();
			}

			if (status == 200) {
				contents = EntityUtils.toString(response.getEntity());
			} else if (status == 502) {
				throw new LiveDocumentNotAvailableException(url);
			} else if (status == 504) {
				// throw new LiveWebTimeoutException("Timeout:" + url);
				throw new LiveDocumentNotAvailableException("Timeout: " + url);
			}

			if (contents == null) {
				throw throwNotAvail("Null or invalid robots.txt from ", url,
						status);
			} else {
				LOGGER.info("Got Robots: " + status + " from " + url);
				return contents;
			}

		} catch (IllegalArgumentException il) {
			throw throwNotAvail("IllegalArgumentException: ", url, status);
		} catch (IOException e) {
			throw throwNotAvail("IOException: ", url, status);
		}
	}

	private LiveDocumentNotAvailableException throwNotAvail(String string,
			String url, int status) {
		String msg = string + " url: " + url + " status: " + status;
		LOGGER.info(msg);
		return new LiveDocumentNotAvailableException(msg);
	}

	public void shutdown() {
		if (updaterThread != null) {
			updaterThread.toRun = false;
			updaterThread.interrupt();
			try {
				updaterThread.join();
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
