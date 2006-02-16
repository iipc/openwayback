/* ExclusionServlet
 *
 * $Id$
 *
 * Created on 5:28:10 PM Feb 13, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ExclusionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger(ExclusionServlet.class
			.getName());
	
	private static final String OPERATION_ARGUMENT = "operation";
	private static final String URL_ARGUMENT = "url";
	private static final String USER_AGENT_ARGUMENT = "useragent";
	private static final String TIMESTAMP_ARGUMENT = "timestamp";
	
	private static final String OPERATION_CHECK = "check";
	private static final String OPERATION_PURGE = "purge";
	

	RoboCache roboCache = null;
	

	// TODO: push this common initialization code up into a base
	// HttpServlet derivative class: similar code lives in just about
	// every Wayback HttpServlet subclass.
	public void init(ServletConfig c) throws ServletException {
		LOGGER.info("Initializing ExclusionServlet...");
		Properties p = new Properties();
		for (Enumeration e = c.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, c.getInitParameter(key));
		}
		ServletContext sc = c.getServletContext();
		for (Enumeration e = sc.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, sc.getInitParameter(key));
		}

		roboCache = new RoboCache();
		try {
			roboCache.init(p);
			LOGGER.info("Initialized ExclusionServlet.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
	}
	private String getMapParam(Map queryMap, String field) {
		String arr[] = (String[]) queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	public void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		Map queryArgs = httpRequest.getParameterMap();
		String url = getMapParam(queryArgs,URL_ARGUMENT);
		String operation = getMapParam(queryArgs,OPERATION_ARGUMENT);
		String userAgent = getMapParam(queryArgs,USER_AGENT_ARGUMENT);
		String timestamp = getMapParam(queryArgs,TIMESTAMP_ARGUMENT);
		ExclusionResponse eclResponse = null;
		if(operation == null) {
			eclResponse = new ExclusionResponse("-","AccessError",false,
				"No "+ OPERATION_ARGUMENT + " argument supplied");
		} else if(url == null) {
			eclResponse = new ExclusionResponse("-","AccessError",false,
				"No " + URL_ARGUMENT + " argument supplied");
		} else {
			if(operation.equals(OPERATION_CHECK)) {
				if (userAgent == null) {
					eclResponse = new ExclusionResponse("-","AccessError",false,
						"No " + USER_AGENT_ARGUMENT + " argument supplied");							
				} else if(timestamp == null) {
					eclResponse = new ExclusionResponse("-","AccessError",false,
						"No " + TIMESTAMP_ARGUMENT + " argument supplied");
				} else {
					try {
						eclResponse = roboCache.checkExclusion(userAgent,url);

					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						eclResponse = new ExclusionResponse("-","ServerError",
								false,e.getMessage());
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						eclResponse = new ExclusionResponse("-","ServerError",
								false,e.getMessage());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						eclResponse = new ExclusionResponse("-","ServerError",
								false,e.getMessage());
					}
				}
			} else if(operation.equals(OPERATION_PURGE)) {
				try {
					eclResponse = roboCache.purgeUrl(url);
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					eclResponse = new ExclusionResponse("-","ServerError",
							false,e.getMessage());
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					eclResponse = new ExclusionResponse("-","ServerError",
							false,e.getMessage());
				}
			} else {
				eclResponse = new ExclusionResponse("-","AccessError",
						false,"Unknown " + OPERATION_ARGUMENT);
			}
		}
		httpResponse.setContentType(eclResponse.getContentType());
		OutputStream os = httpResponse.getOutputStream();
		eclResponse.writeResponse(os);
	}
}
