package org.archive.wayback.replay;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;

/**
 * test of {@link RedirectRewritingHttpHeaderProcessor}.
 */
public class RedirectRewritingHttpHeaderProcessorTest extends TestCase {

	RedirectRewritingHttpHeaderProcessor cut;
	ResultURIConverter uriConverter;
	CaptureSearchResult result;

	protected void setUp() throws Exception {
		super.setUp();
		cut = new RedirectRewritingHttpHeaderProcessor();
		cut.setPrefix("X-Archive-Orig-");

		uriConverter = new ResultURIConverter() {
			@Override
			public String makeReplayURI(String datespec, String url) {
				return datespec + "/" + url;
			}
		};

		result = new CaptureSearchResult();
		result.setCaptureTimestamp("20140102030405");
	}

	public void testFilter_withPrefix() {
		Map<String, String> output = new HashMap<String, String>();

		String[][] input = {
			{ "Connection", "close" },
			{ "Content-Length", "49235172" },
			{ "Cache-Control", "max-age=252460800" },
			{ "Last-Modified", "Thu, 20 Feb 2014 04:32:41 GMT" },
			{ "Date", "Thu, 24 Jul 2014 22:05:16 GMT" },
			{ "Content-Type", "audio/mpeg" },
			{ "Content-Range", "bytes 0-49235171/49235172" },
			{ "Accept-Ranges", "bytes" },
			{ "Server", "nginx" },
		};

		for (String[] kv : input) {
			cut.filter(output, kv[0], kv[1], uriConverter, result);
		}

		// Content-Type, Content-Disposition, Content-Range are preserved and also copied
		// to output.
		assertEquals("bytes 0-49235171/49235172", output.get("X-Archive-Orig-Content-Range"));
		assertEquals("bytes 0-49235171/49235172", output.get("Content-Range"));
		assertEquals("audio/mpeg", output.get("Content-Type"));
		assertEquals("audio/mpeg", output.get("X-Archive-Orig-Content-Type"));

		// Content-Length and Content-Transfer-Encoding are dropped, preserved with prefix
		assertEquals(null, output.get("Content-Length"));
		assertEquals("49235172", output.get("X-Archive-Orig-Content-Length"));

		// others are preserved, not copied to output.
		assertEquals("close", output.get("X-Archive-Orig-Connection"));
		assertEquals(null, output.get("Connection"));
		assertEquals("nginx", output.get("X-Archive-Orig-Server"));
		assertEquals(null, output.get("Server"));
	}

	public void testFilter_noPrefix() {
		Map<String, String> output = new HashMap<String, String>();

		String[][] input = {
			{ "Connection", "close" },
			{ "Content-Length", "49235172" },
			{ "Cache-Control", "max-age=252460800" },
			{ "Last-Modified", "Thu, 20 Feb 2014 04:32:41 GMT" },
			{ "Date", "Thu, 24 Jul 2014 22:05:16 GMT" },
			{ "Content-Type", "audio/mpeg" },
			{ "Content-Range", "bytes 0-49235171/49235172" },
			{ "Accept-Ranges", "bytes" },
			{ "Server", "nginx" },
		};

		cut.setPrefix(null);
		for (String[] kv : input) {
			cut.filter(output, kv[0], kv[1], uriConverter, result);
		}

		// Content-Type, Content-Disposition, Content-Range are copied to output.
		assertEquals("bytes 0-49235171/49235172", output.get("Content-Range"));
		assertEquals("audio/mpeg", output.get("Content-Type"));

		// Content-Length and Content-Transfer-Encoding are dropped.
		assertEquals(null, output.get("Content-Length"));

		// others are copied to output.
		assertEquals("close", output.get("Connection"));
		assertEquals("nginx", output.get("Server"));
	}
}
