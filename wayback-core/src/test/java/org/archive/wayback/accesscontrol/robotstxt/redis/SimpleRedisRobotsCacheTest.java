package org.archive.wayback.accesscontrol.robotstxt.redis;

import java.net.URL;

import junit.framework.TestCase;

import org.archive.wayback.accesscontrol.robotstxt.redis.RedisRobotsLogic.RedisValue;
import org.archive.wayback.accesscontrol.robotstxt.redis.SimpleRedisRobotsCache.RobotsResult;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.liveweb.LiveWebCache;
import org.easymock.EasyMock;

/**
 * Test for {@link SimpleRedisRobotsCache}
 */
public class SimpleRedisRobotsCacheTest extends TestCase {

	/**
	 * Interface for mocking RedisRobotsLogic. Would be unnecessary if
	 * RedisRobotsLogic itself were an interface.
	 */
	public interface RedisRobotsLogicDelegate {
		public RedisValue getValue(String key)
				throws LiveWebCacheUnavailableException;

		public void updateValue(String url, RedisValue value, boolean gzip);

		public void pushKey(String list, String key, int maxSize);

		public void close();
	}

	/**
	 * RedisRobotsLogic mock object. Redirects method calls
	 * (only those used in this test) to RedisRobotsLogicDelegate.
	 */
	public static class TestRedisRobotsLogic extends RedisRobotsLogic {
		private RedisRobotsLogicDelegate delegate;

		public TestRedisRobotsLogic(RedisRobotsLogicDelegate delegate) {
			super(null);
			this.delegate = delegate;
		}

		@Override
		public RedisValue getValue(String key)
				throws LiveWebCacheUnavailableException {
			return delegate.getValue(key);
		}

		@Override
		public void updateValue(String url, RedisValue value, boolean gzip) {
			delegate.updateValue(url, value, gzip);
		}

		@Override
		public void pushKey(String list, String key, int maxSize) {
			delegate.pushKey(list, key, maxSize);
		}

		@Override
		public void close() {
			delegate.close();
		}
	}

	SimpleRedisRobotsCache cut;
	RedisRobotsLogicDelegate redisRobotsLogic;
	LiveWebCache liveweb;

	protected void setUp() throws Exception {
		super.setUp();
		redisRobotsLogic = EasyMock.createMock(RedisRobotsLogicDelegate.class);
		liveweb = EasyMock.createMock(LiveWebCache.class);
		cut = new SimpleRedisRobotsCache();
		cut.setRedisCmds(new TestRedisRobotsLogic(redisRobotsLogic));
		cut.setLiveweb(liveweb);
		cut.setGzipRobots(false); // == default
	}

	protected void setupCached(String key, String value) throws Exception {
		RedisValue redisValue = value != null ? new RedisValue(value,
			Long.MAX_VALUE) : null;
		EasyMock.expect(redisRobotsLogic.getValue(key)).andReturn(redisValue);
	}

	protected void expectCacheUpdate(String key, String value, long ttl) {
		RedisValue expectedRedisValue = new RedisValue(value, ttl);
		redisRobotsLogic.updateValue(key, expectedRedisValue,
			cut.isGzipRobots());
	}

	public void testCached() throws Exception {
		setupCached("http://example.com/robots.txt", "");

		EasyMock.replay(redisRobotsLogic, liveweb);

		Resource r = cut.getCachedResource(new URL(
			"http://example.com/robots.txt"), 0, false);

		assertTrue(r instanceof RobotsTxtResource);
		assertEquals(200, ((RobotsTxtResource)r).getStatusCode());

		EasyMock.verify(redisRobotsLogic);
	}

	public void testCachedFailure() throws Exception {
		setupCached("http://example.com/robots.txt",
			SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + "404");

		EasyMock.replay(redisRobotsLogic, liveweb);

		try {
			cut.getCachedResource(new URL("http://example.com/robots.txt"), 0,
				false);
			fail("did not throw " +
					LiveDocumentNotAvailableException.class.getName());
		} catch (LiveDocumentNotAvailableException ex) {
			// expected, and ex must have the same status code.
			assertEquals(404, ex.getOriginalStatuscode());
		}

		EasyMock.verify(redisRobotsLogic);
	}

	// TODO: test expiration

	public void testLive() throws Exception {
		setupCached("http://example.com/robots.txt", null);
		URL url = new URL("http://example.com/robots.txt");
		RobotsTxtResource resource = new RobotsTxtResource("/* robots.txt */");
		assert resource.getStatusCode() == 200;
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andReturn(
			resource);
		expectCacheUpdate(url.toString(), resource.getContents(),
			cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Note r is not the same as resource
		Resource r = cut.getCachedResource(url, 0, false);

		assertTrue(r instanceof RobotsTxtResource);
		assertEquals(200, ((RobotsTxtResource)r).getStatusCode());
		assertEquals(resource.getContents(),
			((RobotsTxtResource)r).getContents());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testLiveFailure() throws Exception {
		final int STATUSCODE = 403;
		setupCached("http://example.com/robots.txt", null);
		URL url = new URL("http://example.com/robots.txt");
		//RobotsTxtResource resource = new RobotsTxtResource("/* robots.txt */");
		//assert resource.getStatusCode() == 200;
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andThrow(
			new LiveDocumentNotAvailableException(url, STATUSCODE));
		// As 403 is considered a success as far as robots.txt is concerned, totalTTL
		// shall be used as TTL.
		expectCacheUpdate(url.toString(),
			SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + STATUSCODE,
			cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Note r is not the same as resource
		try {
			cut.getCachedResource(url, 0, false);
			fail();
		} catch (LiveDocumentNotAvailableException ex) {
			// expected, and ex must have the same status code.
			assertEquals(STATUSCODE, ex.getOriginalStatuscode());
		}

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testLiveFailure5xx() throws Exception {
		final int STATUSCODE = 502;
		setupCached("http://example.com/robots.txt", null);
		URL url = new URL("http://example.com/robots.txt");
		//RobotsTxtResource resource = new RobotsTxtResource("/* robots.txt */");
		//assert resource.getStatusCode() == 200;
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andThrow(
			new LiveDocumentNotAvailableException(url, STATUSCODE));
		// notAvailTotalTTL shall be used as TTL for 5xx.
		expectCacheUpdate(url.toString(),
			SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + STATUSCODE,
			cut.getNotAvailTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Note r is not the same as resource
		try {
			cut.getCachedResource(url, 0, false);
			fail();
		} catch (LiveDocumentNotAvailableException ex) {
			// expected, and ex must have the same status code.
			assertEquals(STATUSCODE, ex.getOriginalStatuscode());
		}

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testForceUpdate() throws Exception {
		final String OLD_CONTENT = "";
		final String NEW_CONTENT = "/* robots.txt */";
		setupCached("http://example.com/robots.txt", OLD_CONTENT);
		final URL url = new URL("http://example.com/robots.txt");
		final RobotsTxtResource resource = new RobotsTxtResource(NEW_CONTENT);
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andReturn(
			resource);
		expectCacheUpdate(url.toString(), NEW_CONTENT, cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		RobotsResult result = cut.forceUpdate(url.toString(), 0, false);

		assertNotNull(result);
		assertEquals(OLD_CONTENT, result.oldRobots);
		assertEquals(NEW_CONTENT, result.robots);
		assertEquals(200, result.status);
		assertFalse(result.isSameRobots());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testForceUpdateSuccessToFailure() throws Exception {
		final String OLD_CONTENT = "/* robots.txt */";
		final int NEW_STATUS = 403;
		final String NEW_CONTENT = SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + NEW_STATUS;
		setupCached("http://example.com/robots.txt", OLD_CONTENT);
		final URL url = new URL("http://example.com/robots.txt");
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andThrow(
			new LiveDocumentNotAvailableException(url, NEW_STATUS));
		expectCacheUpdate(url.toString(), NEW_CONTENT, cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Even though cacheFails is false, 403 status is saved because it differs
		// from previous value.
		RobotsResult result = cut.forceUpdate(url.toString(), 0, false);

		assertNotNull(result);
		assertEquals(OLD_CONTENT, result.oldRobots);
		assertEquals(null, result.robots);
		assertEquals(NEW_STATUS, result.status);
		assertFalse(result.isSameRobots());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testForceUpdateNoneToSuccess() throws Exception {
		final String OLD_CONTENT = null;
		final String NEW_CONTENT = "/* robots.txt */";
		setupCached("http://example.com/robots.txt", OLD_CONTENT);
		final URL url = new URL("http://example.com/robots.txt");
		final RobotsTxtResource resource = new RobotsTxtResource(NEW_CONTENT);
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andReturn(
			resource);
		expectCacheUpdate(url.toString(), NEW_CONTENT, cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Even though cacheFails is false, 403 status is saved because it differs
		// from previous value.
		RobotsResult result = cut.forceUpdate(url.toString(), 0, false);

		assertNotNull(result);
		assertEquals(OLD_CONTENT, result.oldRobots);
		assertEquals(NEW_CONTENT, result.robots);
		assertEquals(200, result.status);
		assertFalse(result.isSameRobots());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testForceUpdateNoneToFailure() throws Exception {
		final String OLD_CONTENT = null;
		final int NEW_STATUS = 403;
		final String NEW_CONTENT = SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + NEW_STATUS;
		setupCached("http://example.com/robots.txt", OLD_CONTENT);
		final URL url = new URL("http://example.com/robots.txt");
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andThrow(
			new LiveDocumentNotAvailableException(url, NEW_STATUS));
		expectCacheUpdate(url.toString(), NEW_CONTENT, cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Even though cacheFails is false, 403 status is saved because it differs
		// from previous value.
		RobotsResult result = cut.forceUpdate(url.toString(), 0, false);

		assertNotNull(result);
		assertEquals(OLD_CONTENT, result.oldRobots);
		assertEquals(null, result.robots);
		assertEquals(NEW_STATUS, result.status);
		assertFalse(result.isSameRobots());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testForceUpdateFailureToSuccess() throws Exception {
		final String OLD_CONTENT = SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + "502";
		final String NEW_CONTENT = "/* robots.txt */";
		setupCached("http://example.com/robots.txt", OLD_CONTENT);
		final URL url = new URL("http://example.com/robots.txt");
		final RobotsTxtResource resource = new RobotsTxtResource(NEW_CONTENT);
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andReturn(
			resource);
		expectCacheUpdate(url.toString(), NEW_CONTENT, cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Even though cacheFails is false, 403 status is saved because it differs
		// from previous value.
		RobotsResult result = cut.forceUpdate(url.toString(), 0, false);

		assertNotNull(result);
		assertEquals(OLD_CONTENT, result.oldRobots);
		assertEquals(NEW_CONTENT, result.robots);
		assertEquals(200, result.status);
		assertFalse(result.isSameRobots());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testForceUpdateSameFailures() throws Exception {
		final String OLD_CONTENT = SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + "502";
		final int NEW_STATUS = 502;
		setupCached("http://example.com/robots.txt", OLD_CONTENT);
		final URL url = new URL("http://example.com/robots.txt");
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andThrow(
			new LiveDocumentNotAvailableException(url, NEW_STATUS));
		// expects no cache update

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Even though cacheFails is false, 403 status is saved because it differs
		// from previous value.
		RobotsResult result = cut.forceUpdate(url.toString(), 0, false);

		assertNotNull(result);
		assertEquals(OLD_CONTENT, result.oldRobots);
		assertEquals(null, result.robots);
		assertEquals(NEW_STATUS, result.status);
		assertTrue(result.isSameRobots());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	public void testForceUpdateDifferentFailures() throws Exception {
		final String OLD_CONTENT = SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + "502";
		final int NEW_STATUS = 403;
		final String NEW_CONTENT = SimpleRedisRobotsCache.ROBOTS_TOKEN_ERROR + NEW_STATUS;
		setupCached("http://example.com/robots.txt", OLD_CONTENT);
		final URL url = new URL("http://example.com/robots.txt");
		EasyMock.expect(liveweb.getCachedResource(url, 0, false)).andThrow(
			new LiveDocumentNotAvailableException(url, NEW_STATUS));
		expectCacheUpdate(url.toString(), NEW_CONTENT, cut.getTotalTTL());

		EasyMock.replay(redisRobotsLogic, liveweb);

		// Even though cacheFails is false, 403 status is saved because it differs
		// from previous value.
		RobotsResult result = cut.forceUpdate(url.toString(), 0, false);

		assertNotNull(result);
		assertEquals(OLD_CONTENT, result.oldRobots);
		assertEquals(null, result.robots);
		assertEquals(NEW_STATUS, result.status);
		assertFalse(result.isSameRobots());

		EasyMock.verify(redisRobotsLogic, liveweb);
	}

	// more?
}
