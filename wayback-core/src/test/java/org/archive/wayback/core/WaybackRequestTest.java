package org.archive.wayback.core;

import junit.framework.TestCase;

/**
 * Tests for {@link WaybackRequst}
 */
public class WaybackRequestTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * {@link WaybackRequest#setRequestUrl(String)} fix
	 * common errors in request URL - specifically, missing
	 * scheme part, and missing slash in scheme part.
	 */
	public void testSetRequestUrl() {
		WaybackRequest wr = new WaybackRequest();
		wr.setRequestUrl("http:/example.com/");
		assertEquals("http://example.com/", wr.getRequestUrl());

		wr.setRequestUrl("https:/example.com/");
		assertEquals("https://example.com/", wr.getRequestUrl());

		wr.setRequestUrl("example.com/index.html");
		assertEquals("http://example.com/index.html", wr.getRequestUrl());

		wr.setRequestUrl("ftp:/example.com/");
		assertEquals("ftp://example.com/", wr.getRequestUrl());

		// "ftp://" shall be recognized as scheme part, it shall not get
		// "http://" prepended.
		wr.setRequestUrl("ftp://example.com/");
		assertEquals("ftp://example.com/", wr.getRequestUrl());
	}
}
