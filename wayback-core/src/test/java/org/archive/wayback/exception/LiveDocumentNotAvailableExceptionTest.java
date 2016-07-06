package org.archive.wayback.exception;

import java.net.URL;
import java.net.UnknownHostException;

import junit.framework.TestCase;

public class LiveDocumentNotAvailableExceptionTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	/**
	 * Just a test of {@link LiveDocumentNotAvailableException#getMessage()}
	 * @throws Exception
	 */
	public void testLiveDocumentNotAvailableException() throws Exception {
		final String url = "http://example.com/";
		LiveDocumentNotAvailableException ex1 = new LiveDocumentNotAvailableException(
			url, 502);
		System.out.println(ex1);

		LiveDocumentNotAvailableException ex2 = new LiveDocumentNotAvailableException(
			url, new UnknownHostException("example.com"));
		System.out.println(ex2);

		LiveDocumentNotAvailableException ex3 = new LiveDocumentNotAvailableException(
			new URL(url), "unknown error");
		System.out.println(ex3);
	}

}
