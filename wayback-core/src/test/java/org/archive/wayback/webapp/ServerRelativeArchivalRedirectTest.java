package org.archive.wayback.webapp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.ServerRelativeArchivalRedirect.ArchivalUrlRef;
import org.easymock.EasyMock;

/**
 * test for {@link ServerRelativeArchivalRedirect}.
 */
public class ServerRelativeArchivalRedirectTest extends TestCase {

	ServerRelativeArchivalRedirect cut;
	HttpServletRequest request;

	/*
	 * (non-Javadoc)
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		cut = new ServerRelativeArchivalRedirect();
		// in current deployment matchHost is not set (=null)
		cut.setMatchPort(80);
		cut.setUseCollection(true);
		cut.setReplayPrefix("/web/");
	}

	protected void setUpRequest(String requestURI, String referer) {
		// to catch common mistake of including protocol & netloc in requestURI
		assert requestURI.startsWith("/");

		request = EasyMock.createNiceMock(HttpServletRequest.class);
		EasyMock.expect(request.getHeader("Referer")).andStubReturn(referer);
		EasyMock.expect(request.getServerName()).andStubReturn(
			"web.archive.org");
		EasyMock.expect(request.getLocalPort()).andStubReturn(80);
		EasyMock.expect(request.getRequestURI()).andStubReturn(requestURI);
		EasyMock.expect(request.getContextPath()).andStubReturn("");
	}

	/**
	 * test clean case with well-formed target URL in {@code Referer}.
	 */
	public void testRegular() throws Exception {

		final String TARGET_URL = "http://example.com/index.html";
		final String REPLAY_BASE = "http://web.archive.org/web/20010203040506/";
		final String REFERER = REPLAY_BASE + TARGET_URL;
		final String REQUEST_URI = "/js/s_code.js";

		final String EXPECTED_REDIRECT_URL = REPLAY_BASE +
				"http://example.com/js/s_code.js";

		// mocks
		setUpRequest(REQUEST_URI, REFERER);

		HttpServletResponse response = EasyMock
			.createMock(HttpServletResponse.class);
		response.addHeader("Vary", "Referer");
		EasyMock.expectLastCall().once();
		response.sendRedirect(EXPECTED_REDIRECT_URL);
		EasyMock.expectLastCall().once();

		EasyMock.replay(request, response);

		boolean handled = cut.handleRequest(request, response);

		EasyMock.verify(response);
		assertTrue(handled);
	}

	/**
	 * {@link WaybackRequest#setRequestUrl(String)} fix up malformed target URL
	 * like {@code http:/example.com}. So replay page with such target URL can
	 * come in as {@code Referer}. {@code ServerRelativeArchivalRedirect} needs
	 * to be able to handle it. See ARI-3905.
	 */
	public void testMalformedTargetURLInReferer() throws Exception {

		final String TARGET_URL = "http:/example.com/index.html";
		final String REPLAY_BASE = "http://web.archive.org/web/20010203040506/";
		final String REFERER = REPLAY_BASE + TARGET_URL;
		final String REQUEST_URI = "/js/s_code.js";

		final String EXPECTED_REDIRECT_URL = REPLAY_BASE +
				"http://example.com/js/s_code.js";

		// mocks
		setUpRequest(REQUEST_URI, REFERER);

		HttpServletResponse response = EasyMock
			.createMock(HttpServletResponse.class);
		response.addHeader("Vary", "Referer");
		EasyMock.expectLastCall().once();
		response.sendRedirect(EXPECTED_REDIRECT_URL);
		EasyMock.expectLastCall().once();

		EasyMock.replay(request, response);

		boolean handled = cut.handleRequest(request, response);

		EasyMock.verify(response);
		assertTrue(handled);
	}

	public static class Extended extends ServerRelativeArchivalRedirect {
		ArchivalUrlRef ref;
		public Extended(ArchivalUrlRef ref) {
			this.ref = ref;
		}
		protected ArchivalUrlRef getOrigin(HttpServletRequest httpRequest) {
			ArchivalUrlRef ref = super.getOrigin(httpRequest);
			if (ref == null) {
				// imaging getting this value from somewhere...
				ref = this.ref;
			}
			return ref;
		};
	}

	/**
	 * Test customization by sub-classing.
	 * @throws Exception
	 */
	public void testCustomOrigin() throws Exception {
		final String ROOT = "http://web.archive.org";
		final String COLLECTION = "/web";
		final String DATESPEC = "20010203040506";
		final String TARGET_BASE = "http://example.com";
		final String REQUEST_URI = "/js/s_code.js";

		final String EXPECTED_REDIRECT_URL = ROOT + COLLECTION + "/" +
				DATESPEC + "/" + TARGET_BASE + REQUEST_URI;

		// Replace cut
		cut = new Extended(new ArchivalUrlRef(ROOT, COLLECTION,
			DATESPEC, TARGET_BASE + "/index.html"));
		// in current deployment matchHost is not set (=null)
		cut.setMatchPort(80);
		cut.setUseCollection(true);

		// mocks - no Referer
		setUpRequest(REQUEST_URI, null);

		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		response.addHeader("Vary", "Referer");
		EasyMock.expectLastCall().once();
		response.sendRedirect(EXPECTED_REDIRECT_URL);
		EasyMock.expectLastCall().once();

		EasyMock.replay(request, response);

		boolean handled = cut.handleRequest(request, response);

		EasyMock.verify(response);
		assertTrue(handled);
	}

	/**
	 * Test of non-ArchivalUrl requests.
	 * ServerRelativeArchivalRedirect is setup as catch-all handler ("{@code +"}).
	 * It must handle non-replay URLs like {@code /} and {@code /favicon.ico}, with
	 * non-ArchivalUrl Referer. It must not fail, and return {@code false}.
	 * @throws Exception
	 */
	public void testNonArchivalUrl() throws Exception {
		final String REQUEST_URI = "/";
		final String REFERER = "http://www.example.com/index.html";

		setUpRequest(REQUEST_URI, REFERER);

		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		// no actions on response
		EasyMock.replay(request, response);

		boolean handled = cut.handleRequest(request, response);

		EasyMock.verify(response);
		assertFalse(handled);
	}
}
