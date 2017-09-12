package org.archive.wayback.util.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.archive.wayback.test.HttpServletRequestFixture;
import org.easymock.EasyMock;

/**
 * Tests for {@link RequestMapper}
 */
public class RequestMapperTest extends TestCase {

	ServletContext servletContext;

	protected void setUp() throws Exception {
		super.setUp();

		// ServletContext is simply passed on to RequestHandler.setServletContext().
		// not used in any way in this test.
		servletContext = EasyMock.createNiceMock(ServletContext.class);
		EasyMock.replay(servletContext);
	}

	/**
	 * RequestHandler with {@code getInternalPort} and
	 * {@code getAccessPointPath}. XXX RequestHandler with these two methods
	 * should have been defined as an interface.
	 */
	public static class TestAccessPoint extends AbstractRequestHandler {

		public TestAccessPoint(String beanName, int internalPort,
				String accessPointPath) {
			setBeanName(beanName);
			setInternalPort(internalPort);
			setAccessPointPath(accessPointPath);
		}

		@Override
		public boolean handleRequest(HttpServletRequest httpRequest,
				HttpServletResponse httpResponse) throws ServletException,
				IOException {
			return true;
		}

	}

	/**
	 * creates a RequestHandler fixture. if {@code accessPointPath} is non-
	 * {@code null}, returns TestAccessPoint instance. otherwise, returns mock
	 * object with {@code beanName}.
	 * @param beanName bean name
	 * @param accessPointPath
	 * @param port
	 * @return new RequestHandler object
	 */
	protected static RequestHandler rh(String beanName, String accessPointPath,
			int port) {
		// XXX RequestHandler with
		if (accessPointPath != null) {
			RequestHandler rh = new TestAccessPoint(beanName, port,
				accessPointPath);
			return rh;
		} else {
			RequestHandler rh = EasyMock.createNiceMock(RequestHandler.class);
			EasyMock.expect(rh.getBeanName()).andStubReturn(beanName);
			EasyMock.replay(rh);
			return rh;
		}
	}

	protected static RequestHandler rh(String beanName) {
		return rh(beanName, null, -1);
	}

	/**
	 * Create HttpServletRequest fixture.
	 * {@code getContextPath()} returns empty string emulating ROOT deployment.
	 * @param server value for {@code getServerName()}
	 * @param port value for {@code getLocalPort()}
	 * @param path value for {@code getRequestURI()} (must have leading slash)
	 * @return mock object in replay mode
	 */
	protected HttpServletRequest req(String server, int port, String path) {
		return new HttpServletRequestFixture().serverName(server)
			.localPort(port).requestURI(path);
	}

	protected HttpServletRequest req(int port, String path) {
		return req("web.archive.org", port, path);
	}

	/**
	 * mapping by beanName, port number only.
	 */
	public void testMappingByBeanName_port() {
		RequestHandler[] handlers = {
			rh("8080"),
			rh("8081:")
		};
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);
		{
			HttpServletRequest request = req(8080, "/web/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[0], rhc.getRequestHandler());
			assertEquals("", rhc.getPathPrefix());
		}
		{
			// null is returned for undefined port.
			HttpServletRequest request = req(9000, "/web/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertNull(rhc);
			;
		}
	}

	/**
	 * mapping by beanName, port number and path
	 */
	public void testMappingByBeanName_portPath() {
		RequestHandler[] handlers = {
			rh("8080:blue"),
			rh("8080:fish"),
			rh("8081:blue"),
			rh("8081:"),
		};
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);
		{
			HttpServletRequest request = req(8080, "/blue/grass");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[0], rhc.getRequestHandler());
			assertEquals("/blue", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8081, "/blue/ocean");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[2], rhc.getRequestHandler());
			assertEquals("/blue", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8080, "/green/fields");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertNull(rhc);
		}
		{
			HttpServletRequest request = req(8081, "/green/fields");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[3], rhc.getRequestHandler());
			assertEquals("", rhc.getPathPrefix());
		}
	}

	/**
	 * mapping by beanName, host, port number, and optional path
	 */
	public void testMappingByBeanName_hostPortPath() {
		RequestHandler[] handlers = {
			rh("web.archive.org:8080:blue"),
			rh("web.archive.org:8081:blue"),
			rh("web-beta.archive.org:8080:blue"),
			rh("web-beta.archive.org:8080:fish"),
			rh("web.archive.org:8081:"),
			// testing interaction between route with specific host and route without
			rh("web.archive.org:8082:blue"),
			rh("8082:fish"),
			rh("web.archive.org:8083:"),
			rh(":8083:fish")
		};
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);
		{
			HttpServletRequest request = req("web.archive.org", 8080, "/blue/grass");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[0], rhc.getRequestHandler());
			assertEquals("/blue", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req("web.archive.org", 8081, "/blue/ocean");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[1], rhc.getRequestHandler());
			assertEquals("/blue", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req("web-beta.archive.org", 8080, "/blue/sky");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[2], rhc.getRequestHandler());
			assertEquals("/blue", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req("web.archive.org", 8080, "/fish/pond");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertNull(rhc);
		}
		{
			HttpServletRequest request = req("web-beta.archive.org", 8080, "/fish/eye");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[3], rhc.getRequestHandler());
			assertEquals("/fish", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8080, "/green/fields");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertNull(rhc);
		}
		{
			HttpServletRequest request = req(8081, "/green/fields");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[4], rhc.getRequestHandler());
			assertEquals("", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req("web.archive.org", 8082, "/fish/bowl");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[6], rhc.getRequestHandler());
			assertEquals("/fish", rhc.getPathPrefix());
		}
		{
			// route with specific host wins over host-agnostic route matching path prefix
			HttpServletRequest request = req("web.archive.org", 8083, "/fish/bowl");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[7], rhc.getRequestHandler());
			assertEquals("", rhc.getPathPrefix());
		}

	}

	/**
	 * mapping by beanName, general URI
	 */
	public void testMappingByBeanName_uri() {
		RequestHandler[] handlers = {
			rh(":8080/check-access/"),
			rh("/web/"),
			rh("http://web.archive.org:8081/static/"),
			rh("/extra/path/segments/have/no/effect")
		};
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);
		{
			HttpServletRequest request = req(8080, "/check-access/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[0], rhc.getRequestHandler());
			assertEquals("/check-access", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8080, "/web/*/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[1], rhc.getRequestHandler());
			assertEquals("/web", rhc.getPathPrefix());
		}
		{
			// port defaults to 8080 if not present in beanName.
			HttpServletRequest request = req(9000, "/web/*/http://exmaple.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertNull(rhc);
		}
		{
			HttpServletRequest request = req("web.archive.org", 8081, "/static/logo.jpg");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[2], rhc.getRequestHandler());
			assertEquals("/static", rhc.getPathPrefix());
		}
		{
			// currently host part has no effect in this syntax.
			HttpServletRequest request = req("web-beta.archive.org", 8081, "/static/up.jpg");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[2], rhc.getRequestHandler());
			assertEquals("/static", rhc.getPathPrefix());
		}
		{
			// only the first path segment is used for matching.
			HttpServletRequest request = req(8080, "/extra/thingy");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[3], rhc.getRequestHandler());
			assertEquals("/extra", rhc.getPathPrefix());
		}
	}

	/**
	 * mapping by (accessPointPath, internalPort).
	 * Most common usage.
	 */
	public void testMappingByAccessPointPath() {
		RequestHandler[] handlers = {
			rh("aclChecker", "/check-access/", 8081),
			rh("waybackAccessPoint", "/web/", 8081),
			rh("staticAccessPoint", "/static/", 8081),
			rh("livewebWarcWriter", "/save/", 8081),
			rh("livewebWarcWriter", "/save/", 9000) };
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);
		{
			HttpServletRequest request = req(8081, "/web/*/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[1], rhc.getRequestHandler());
			assertEquals("/web", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8081, "/static/logo.jpg");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[2], rhc.getRequestHandler());
			assertEquals("/static", rhc.getPathPrefix());
		}
		{
			// beanName is not used as route specification
			HttpServletRequest request = req(8081, "/aclChecker/abc");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertNull(rhc);
		}
		{
			HttpServletRequest request = req(9000, "/save/http://example.com");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[4], rhc.getRequestHandler());
			assertEquals("/save", rhc.getPathPrefix());
		}
	}

	/**
	 * accessPoinPath also accepts host:port:path syntax as with beanName.
	 */
	public void testMappingByAccessPointPath_hostPortPath() {
		RequestHandler[] handlers = {
			rh("waybackAccessPoint", "8081:web", 0),
			// internalPort property should have no effect
			rh("staticAccessPoint", "8081:static", 9000),
			rh("catchall", "8081:", 0) };
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);
		{
			HttpServletRequest request = req(8081, "/web/*/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[0], rhc.getRequestHandler());
			assertEquals("/web", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8081, "/static/logo.jpg");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[1], rhc.getRequestHandler());
			assertEquals("/static", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8081, "/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[2], rhc.getRequestHandler());
			assertEquals("", rhc.getPathPrefix());
		}
	}

	public void testMappingByAccessPointPath_PathSegmentParameter() {
		RequestHandler[] handlers = {
			rh("livewebAccessPoint", "/save;/", 8080),
			rh("waybackAccessPoint", "/web/", 8080)
		};
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers), servletContext);
		{
			HttpServletRequest request = req(8080, "/save/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[0], rhc.getRequestHandler());
			assertEquals("/save", rhc.getPathPrefix());
		}
		{
			HttpServletRequest request = req(8080, "/save;param=1/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertEquals(handlers[0], rhc.getRequestHandler());
			assertEquals("/save;param=1", rhc.getPathPrefix());
		}
		{
			// in-segment parameter is not recognized for /web
			HttpServletRequest request = req(8080, "/web;param=1/*/http://example.com/");
			RequestHandlerContext rhc = mapper.mapRequest(request);

			assertNull(rhc);
		}
	}

	/**
	 * Test {@link RequestMapper#handleRequest(HttpServletRequest, HttpServletResponse)}
	 */
	public void testHandleRequest() throws Exception {
		// set
		AbstractRequestHandler rh = new AbstractRequestHandler() {
			public boolean handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ServletException ,IOException {
				// these methods must be checked against response object passed to handleRequest().
				assertEquals("/web/", RequestMapper.getRequestPathPrefix(httpRequest));
				assertEquals("*/http://example.com/", RequestMapper.getRequestContextPath(httpRequest));

				httpResponse.getWriter().write("response");
				return true;
			};
		};
		rh.setBeanName("waybackAccessPoint");
		rh.setAccessPointPath("/web/");
		rh.setInternalPort(8080);

		RequestHandler[] handlers = { rh };
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);

		// It is important to use HttpServletRequestFixture instead of mock
		// object here, so that we can test RequetMapper.getRequestPathPrefix() etc.
		// without getting into internals of RequestMapper.
		HttpServletRequest request = req("web.archive.org", 8080, "/web/*/http://example.com/");

		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		EasyMock.expect(response.getWriter()).andReturn(pw).once();

		EasyMock.replay(response);

		boolean handled = mapper.handleRequest(request,  response);

		assertTrue(handled);

		assertEquals("response", sw.toString());
	}

	/**
	 * {@link RequestMapper#handleRequest(HttpServletRequest, HttpServletResponse)}
	 * stores a list of defined paths in {@code AccessPointNames} attribute.
	 *
	 * This feature may be dropped in favor of RequestMapper exposing service method
	 * to UI code.
	 *
	 * @throws Exception
	 */
	public void testAccessPointNames() throws Exception {
		RequestHandler[] handlers = {
			rh("aclChecker", "/check-access/", 8080),
			rh("waybackAccessPoint", "/web/", 8080),
			rh("staticAccessPoint", "/static/", 8080),
			rh("livewebWarcWriter", "/save;/", 8080),
			// different port - this will not show up in AccessPointNames
			rh("livewebWarcWriter", "/save2/", 9000)
		};
		RequestMapper mapper = new RequestMapper(Arrays.asList(handlers),
			servletContext);

		HttpServletRequest request = req("web.archive.org", 8080, "/no/such/prefix");

		HttpServletResponse response = EasyMock.createMock(HttpServletResponse.class);

		EasyMock.replay(response);

		boolean handled = mapper.handleRequest(request, response);

		assertFalse(handled);

		Object o = request.getAttribute("AccessPointNames");
		// XXX there's existing code expecting ArrayList specifically
		assertTrue(o instanceof ArrayList<?>);
		Collection<String> accessPointNames = (Collection<String>)o;
		String[] names = (accessPointNames).toArray(new String[accessPointNames.size()]);
		Arrays.sort(names);

		String[] expected = { "check-access", "save", "static", "web" };
		assertEquals(expected.length, names.length);
		for (int i = 0; i < expected.length; i++) {
			assertEquals(expected[i], names[i]);
		}
	}
}
