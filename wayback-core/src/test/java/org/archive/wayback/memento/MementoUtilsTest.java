package org.archive.wayback.memento;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.AccessPoint;
import org.easymock.EasyMock;

public class MementoUtilsTest extends TestCase {
	Properties configs;
	AccessPoint accessPoint;

	protected void setUp() throws Exception {
		final String[][] properties = {
			//MementoConstants.AGGREGATION_PREFIX_CONFIG, "",
			//MementoConstants.PAGE_MAXRECORDS_CONFIG, "0",

		};
		configs = new Properties();
		if (properties.length > 0) {
			for (String[] kv : properties) {
				configs.setProperty(kv[0], kv[1]);
			}
		}
		accessPoint = new AccessPoint();
		accessPoint.setConfigs(configs);
		accessPoint.setReplayPrefix("/web/");
		accessPoint.setQueryPrefix("/web/");
		ArchivalUrlResultURIConverter uriConverter = new ArchivalUrlResultURIConverter();
		uriConverter.setReplayURIPrefix("/web/");
		accessPoint.setUriConverter(uriConverter);
	}

	protected final HttpServletResponse createResponseMock() {
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		return response;
	}

	protected final CaptureSearchResult createCapture(String timestamp) {
		FastCaptureSearchResult cap = new FastCaptureSearchResult();
		cap.setCaptureTimestamp(timestamp);
		return cap;
	}

	public void testAddDoNotNegotiateHeader() {
		HttpServletResponse response = createResponseMock();
		response.setHeader("Link", "<http://mementoweb.org/terms/donotnegotiate>; rel=\"type\"");

		EasyMock.replay(response);

		MementoUtils.addDoNotNegotiateHeader(response);

		EasyMock.verify(response);
	}

	public void testAddOrigHeader() {
		final String url = "http://example.com/";
		HttpServletResponse response = createResponseMock();
		response.setHeader("Link", String.format("<%s>; rel=\"original\"", url));

		EasyMock.replay(response);

		MementoUtils.addOrigHeader(response, url);

		EasyMock.verify(response);
	}

	public void testAddVaryHeader() {
		// TODO
	}

	public void testGetTimemapUrl() {
		final String url = "http://example.com/";
		String result = MementoUtils.getTimemapUrl(accessPoint, "cdx", url);

		assertEquals(String.format("/web/timemap/cdx/%s", url), result);
	}

	public void testGetTimegateUrl() {
		final String url = "http://example.com/";
		String result = MementoUtils.getTimegateUrl(accessPoint, url);

		assertEquals(String.format("/web/%s", url), result);
	}

	final static Pattern LINK_ELEMENT_PATTERN = Pattern.compile("(<.*?>(?:;\\s*[a-z]+=\"(.*?)\")+)(,\\s*)?");

	public void testGenerateMementoLinkHeaders() {
		final CaptureSearchResults results = new CaptureSearchResults();
		results.addSearchResult(createCapture("20140101000000"));
		results.addSearchResult(createCapture("20140125000000"));
		results.setClosest(results.getResults().getLast());

		final WaybackRequest wbr = new WaybackRequest();
		wbr.setAccessPoint(accessPoint);
		wbr.setRequestUrl("http://example.com/");

		String result = MementoUtils.generateMementoLinkHeaders(results, wbr, true, true);

		List<String> elements = new ArrayList<String>();
		Matcher m = LINK_ELEMENT_PATTERN.matcher(result);
		while (m.regionStart() < result.length() && m.lookingAt()) {
			elements.add(m.group(1));
			m.region(m.end(), result.length());
		}
		assertEquals(result.length(), m.regionStart());

		// TODO: order doesn't really matter
		assertEquals("<http://example.com/>; rel=\"original\"",
			elements.get(0));
		assertEquals("</web/timemap/link/http://example.com/>; rel=\"timemap\"; type=\"application/link-format\"",
			elements.get(1));
		assertEquals("</web/http://example.com/>; rel=\"timegate\"",
			elements.get(2));
		assertEquals("</web/20140125000000/http://example.com/>; rel=\"last memento\"; datetime=\"Sat, 25 Jan 2014 00:00:00 GMT\"",
			elements.get(3));
		assertEquals("</web/20140101000000/http://example.com/>; rel=\"prev first memento\"; datetime=\"Wed, 01 Jan 2014 00:00:00 GMT\"",
			elements.get(4));
	}
}