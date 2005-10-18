package org.archive.wayback.servletglue;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayUI;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.core.WaybackLogic;

public class WBReplayUIServlet extends HttpServlet {
	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final long serialVersionUID = 1L;

	private WaybackLogic wayback = new WaybackLogic();

	public WBReplayUIServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig c) throws ServletException {

		Properties p = new Properties();
		for (Enumeration e = c.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, c.getInitParameter(key));
		}

		try {
			wayback.init(p);
		} catch (Exception e) {
			throw new ServletException(e.getMessage());
		}
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		WMRequest wmRequest = (WMRequest) request
				.getAttribute(WMREQUEST_ATTRIBUTE);
		if (wmRequest == null) {
			throw new ServletException("No WMRequest object");
		}
		ReplayUI replayUI = wayback.getReplayUI();
		replayUI.handle(wayback, wmRequest, request, response);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
