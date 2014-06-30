/**
 * 
 */
package org.archive.wayback.accesspoint.proxy;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.wayback.ResultURIConverter;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

/**
 * Test for {@link ProxyAccessPoint}.
 */
public class ProxyAccessPointTest extends TestCase {

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/**
	 * {@code ProxyAccessPoint} recognizes Archival-URL style request and redirects
	 * it to either calendar view (if timestamp is {@code "*"}) or replay of specific capture
	 * (if timestamp is explicit).
	 * Check if queryString part is property included in the redirect URL (ARI-3903).
	 * @throws Exception
	 */
	public void testHandleProxy() throws Exception {
		// request.getRequestURL() returns URL without queryString.
		final String REQUEST_PROTOCOL_AND_HOST = "http://wayback.archive-it.org:8081";
		// request.getRequestURI() returns path part only.
		final String REQUEST_URI = "/3548/20140430194857/http://www.example.edu/index.php";
		// request.getQueryString() does not include "?"
		final String QUERY_STRING = "id=860233";

		HttpServletRequest request = EasyMock.createNiceMock(HttpServletRequest.class);
		EasyMock.expect(request.getRequestURL()).andStubAnswer(new IAnswer<StringBuffer>() {
			@Override
			public StringBuffer answer() throws Throwable {
				return new StringBuffer(REQUEST_PROTOCOL_AND_HOST + REQUEST_URI);
			}
		});
		EasyMock.expect(request.getRequestURI()).andStubReturn(REQUEST_URI);
		EasyMock.expect(request.getQueryString()).andStubReturn(QUERY_STRING);

		ResultURIConverter archivalToProxy = EasyMock.createMock(ResultURIConverter.class);
		EasyMock.expect(
				archivalToProxy.makeReplayURI("20140430194857",
						"http://www.example.edu/index.php?id=860233"))
				.andReturn("http://got-it-right/");
		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		response.sendRedirect("http://got-it-right/");

		ProxyConfigSelector configSelector = EasyMock.createNiceMock(ProxyConfigSelector.class);
		EasyMock.expect(configSelector.resolveConfig(request)).andReturn("3548");


		// not setting nonProxyAccessPoint
		ProxyAccessPoint cut = new ProxyAccessPoint();
		cut.setReplayPrefix("//wayback.archive-it.org:8081");
		cut.setConfigSelector(configSelector);
		cut.setArchivalToProxyConverter(archivalToProxy);
		
		EasyMock.replay(request, response, archivalToProxy, configSelector);
		
		boolean r = cut.handleProxy(request, response);
		
		EasyMock.verify(response, archivalToProxy);
		
		assertTrue("handleProxy return value", r);
	}

}
