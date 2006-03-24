/* FileLocationDBServlet
 *
 * $Id$
 *
 * Created on 4:44:09 PM Dec 16, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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
package org.archive.wayback.http11resourcestore;

import java.io.IOException;
import java.io.OutputStream;
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

import org.archive.wayback.exception.ConfigurationException;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileLocationDBServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(
			FileLocationDBServlet.class.getName());

	protected static final String OPERATION_ARGUMENT = "operation";
	protected static final String NAME_ARGUMENT = "name";
	protected static final String URL_ARGUMENT = "url";
	protected static final String LOOKUP_OPERATION = "lookup";
	protected static final String ADD_OPERATION = "add";
	protected static final String REMOVE_OPERATION = "remove";
	protected static final String NO_LOCATION_PREFIX = "ERROR No locations for";

	private static final long serialVersionUID = 1L;


	private FileLocationDB locationDB = new FileLocationDB();

	/**
	 * Constructor
	 */
	public FileLocationDBServlet() {
		super();
	}

	public void init(ServletConfig c) throws ServletException {

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

		try {
			locationDB.init(p);
			LOGGER.info("Initialized locationDB.");
		} catch (ConfigurationException e) {
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		}
	}
	public void destroy() {
		try {
			locationDB.shutdownDB();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private String getMapParam(Map queryMap, String field) {
		String arr[] = (String[]) queryMap.get(field);
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arr[0];
	}

	public void doPost(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {
		doGet(httpRequest,httpResponse);
	}

	public void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		Map queryMap = httpRequest.getParameterMap();
		String operation = getMapParam(queryMap,OPERATION_ARGUMENT);
		if(operation == null) {
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,"No " +
					OPERATION_ARGUMENT + " argument.");
			return;
		}
		if(operation.equals(LOOKUP_OPERATION)) {
			String arcName = getMapParam(queryMap,NAME_ARGUMENT);
			if(arcName == null || arcName.length() < 1) {
				httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,"No " +
						NAME_ARGUMENT + " argument.");
			} else {
				doLookup(httpResponse,arcName);
			}
		} else {
			
			String arcName = getMapParam(queryMap,NAME_ARGUMENT);
			String arcUrl = getMapParam(queryMap,URL_ARGUMENT);
			
			if(arcName == null || arcName.length() < 1) {

				httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"No " + NAME_ARGUMENT + " argument.");

			} else if(arcUrl == null || arcUrl.length() < 1) {

				httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
						"No " +	URL_ARGUMENT + " argument.");

			} else {
				if(operation.equals(ADD_OPERATION)) {

					doAdd(httpResponse,arcName,arcUrl);
					
				} else if(operation.equals(REMOVE_OPERATION)) {

					doRemove(httpResponse,arcName,arcUrl);
					
				} else {

					httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
							"Unknown operation. Must be one of " +
							LOOKUP_OPERATION + "," + ADD_OPERATION + ", or " +
							REMOVE_OPERATION + ".");
				}
			}
		}
	}
	
	private void sendMessage(HttpServletResponse httpResponse, final String 
			message) throws IOException {
		httpResponse.setStatus(HttpServletResponse.SC_OK);
		httpResponse.setContentType("text/plain");
		OutputStream os = httpResponse.getOutputStream();
		os.write(message.getBytes());
	}
	
	private void doLookup(HttpServletResponse httpResponse, final String 
			arcName) throws IOException {
		String arcUrls[];
		String message = NO_LOCATION_PREFIX + " " + arcName;
		try {
			arcUrls = locationDB.arcToUrls(arcName);
			if(arcUrls != null && arcUrls.length > 0) {
				StringBuffer buf = new StringBuffer("OK ");
				for(int i = 0; i < arcUrls.length; i++) {
					if(buf.length() > 0) {
						buf.append("\n");
					}
					buf.append(arcUrls[i]);
				}
				message = buf.toString();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		sendMessage(httpResponse,message);
	}
	
	private void doAdd(HttpServletResponse httpResponse, final String arcName,
			final String arcUrl) throws IOException {

		String message = "OK added url " + arcUrl + " for " + arcName;
		try {
			locationDB.addArcUrl(arcName,arcUrl);
		} catch (DatabaseException e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		sendMessage(httpResponse,message);
	}

	private void doRemove(HttpServletResponse httpResponse, final String 
			arcName, final String arcUrl) throws IOException {

		String message = "OK removed url " + arcUrl + " for " + arcName;
		try {
			locationDB.removeArcUrl(arcName,arcUrl);
		} catch (DatabaseException e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		sendMessage(httpResponse,message);
		
	}
}
