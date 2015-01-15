/**
 *
 */
package org.archive.wayback.resourceindex.cdxserver;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.filter.CDXFilter;
import org.archive.format.cdx.CDXFieldConstants;
import org.archive.format.cdx.CDXLine;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Test for {@link AccessCheckFilter}
 */
public class AccessCheckFilterTest extends TestCase {

	AccessCheckFilter cut;

	AuthToken authToken;
	ObjectFilter<CaptureSearchResult> adminFilter;
	ObjectFilter<CaptureSearchResult> robotsFilter;
	CDXFilter scopeFilter;

	boolean ignoreRobots = false;

	private static class MockExclusionFilter extends ExclusionFilter {
		ObjectFilter<CaptureSearchResult> mock;
		public MockExclusionFilter(ObjectFilter<CaptureSearchResult> mock) {
			this.mock = mock;
		}
		@Override
		public int filterObject(CaptureSearchResult o) {
			return mock.filterObject(o);
		}
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		super.setUp();
		authToken = new AuthToken() {
			public boolean isIgnoreRobots() {
				return ignoreRobots;
			};
		};
		adminFilter = EasyMock.createMock(ObjectFilter.class);
		robotsFilter = EasyMock.createMock(ObjectFilter.class);
		scopeFilter = EasyMock.createMock(CDXFilter.class);

		cut = new AccessCheckFilter(authToken, new MockExclusionFilter(
			adminFilter), new MockExclusionFilter(robotsFilter), scopeFilter);
	}

	static class CheckCaptureSearchResult implements IAnswer<Integer> {
		String url;
		String urlkey;
		String timestamp;

		public CheckCaptureSearchResult(String url, String urlkey, String timestamp) {
			this.url = url;
			this.urlkey = urlkey;
			this.timestamp = timestamp;
		}
		@Override
		public Integer answer() throws Throwable {
			CaptureSearchResult o = (CaptureSearchResult)EasyMock.getCurrentArguments()[0];
			assertEquals(url, o.getOriginalUrl());
			assertEquals(urlkey, o.getUrlKey());
			assertEquals(timestamp, o.getCaptureTimestamp());
			return ExclusionFilter.FILTER_INCLUDE;
		}
	}

	public void testIncludeUrl() throws Exception {
		final String url = "http://example.com/";
		final String urlkey = "example.com)/";

		EasyMock.expect(adminFilter.filterObject(EasyMock.<CaptureSearchResult>notNull())).andAnswer(
			new CheckCaptureSearchResult(url, urlkey, null));
		EasyMock.expect(robotsFilter.filterObject(EasyMock.<CaptureSearchResult>notNull())).andAnswer(
			new CheckCaptureSearchResult(url, urlkey, null));
		// expects no calls to scopeFilter

		EasyMock.replay(adminFilter, robotsFilter, scopeFilter);

		boolean result = cut.includeUrl(urlkey, url);

		assertTrue(result);
		EasyMock.verify(adminFilter, robotsFilter, scopeFilter);
	}

	public void testIncludeCapture() throws Exception {
		final String url = "http://example.com/";
		final String urlkey = "example.com)/";
		final String timestamp = "20100429123456";

		final String cdxLine = StringUtils.join(new Object[] {
			urlkey, timestamp, url, "text/html", 200, "DIGEST",
			"-", "-", 1024, 966357, "crawl-a/crawl-a-20100429000000.warc.gz"
		}, " ");

		final CDXLine input = new CDXLine(cdxLine, CDXFieldConstants.CDX_ALL_NAMES);

		EasyMock.expect(adminFilter.filterObject(EasyMock.<CaptureSearchResult>notNull())).andAnswer(
			new CheckCaptureSearchResult(url, urlkey, timestamp));
		EasyMock.expect(robotsFilter.filterObject(EasyMock.<CaptureSearchResult>notNull())).andAnswer(
			new CheckCaptureSearchResult(url, urlkey, timestamp));
		EasyMock.expect(scopeFilter.include(EasyMock.<CDXLine>notNull())).andAnswer(
			new IAnswer<Boolean>() {
				@Override
				public Boolean answer() throws Throwable {
					CDXLine line = (CDXLine)EasyMock.getCurrentArguments()[0];
					assertEquals(url, line.getOriginalUrl());
					assertEquals(urlkey, line.getUrlKey());
					return true;
				}
			});

		EasyMock.replay(adminFilter, robotsFilter, scopeFilter);

		boolean result = cut.includeCapture(input);

		assertTrue(result);
		EasyMock.verify(adminFilter, robotsFilter, scopeFilter);
	}
}
