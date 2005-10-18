package org.archive.wayback.servletglue;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.net.UURI;
import org.archive.wayback.RequestParser;
import org.archive.wayback.core.WMRequest;

public class RequestFilter implements Filter {
	private static final Logger LOGGER = Logger.getLogger(RequestFilter.class
			.getName());

	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final String REQUEST_PARSER_CLASS = "requestParser.class";

	private static final String HANDLER_URL = "handler.url";

	private String handlerUrl = null;

	private RequestParser requestParser = null;

	public RequestFilter() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public void init(FilterConfig c) throws ServletException {

		handlerUrl = c.getInitParameter(HANDLER_URL);
		if ((handlerUrl == null) || (handlerUrl.length() <= 0)) {
			throw new ServletException("No config (" + HANDLER_URL + ")");
		}

		String className = c.getInitParameter(REQUEST_PARSER_CLASS);
		if ((className == null) || (className.length() <= 0)) {
			throw new ServletException("No config (" + REQUEST_PARSER_CLASS
					+ ")");
		}
		try {
			requestParser = (RequestParser) Class.forName(className)
					.newInstance();
			LOGGER.info("new " + className + " requestParser created.");

		} catch (Exception e) {
			// Convert. Add info.
			throw new ServletException("Failed making requestParser with "
					+ className + ": " + e.getMessage());
		}

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!handle(request, response)) {
			chain.doFilter(request, response);
		}
	}

	protected boolean handle(final ServletRequest request,
			final ServletResponse response) throws IOException,
			ServletException {
		if (!(request instanceof HttpServletRequest)) {
			return false;
		}
		if (!(response instanceof HttpServletResponse)) {
			return false;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		WMRequest wmRequest = requestParser.parseRequest(httpRequest);

		if (wmRequest == null) {
			return false;
		}

		// if getRedirectURI returns non-null, then the request needs a
		// redirect:
		UURI redirectURI = wmRequest.getRedirectURI();
		if (redirectURI != null) {
			String redirectURL = redirectURI.getEscapedURI();
			// response.sendRedirect(response.encodeRedirectURL(redirectURL));
			httpResponse.sendRedirect(httpResponse
					.encodeRedirectURL(redirectURL));
		} else {
			request.setAttribute(WMREQUEST_ATTRIBUTE, wmRequest);
			RequestDispatcher dispatcher = request
					.getRequestDispatcher(handlerUrl);

			dispatcher.forward(request, response);
		}

		return true;
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

}
