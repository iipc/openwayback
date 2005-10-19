/* WBReplayUIServlet
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

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

/**
 * Servlet implementation for Wayback Replay requests.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WBReplayUIServlet extends HttpServlet {
	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final long serialVersionUID = 1L;

	private WaybackLogic wayback = new WaybackLogic();

	/**
	 * Constructor
	 */
	public WBReplayUIServlet() {
		super();
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

	}

}
