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
import java.text.ParseException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackServlet;
import org.archive.wayback.exception.ConfigurationException;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileLocationDBServlet extends WaybackServlet {

	private static final String FILE_LOCATION_DB_CLASS = "org.archive.wayback.http11resourcestore.FileLocationDB.java";
	
	protected static final String OPERATION_ARGUMENT = "operation";
	protected static final String NAME_ARGUMENT = "name";
	protected static final String URL_ARGUMENT = "url";
	protected static final String LOOKUP_OPERATION = "lookup";
	protected static final String ADD_OPERATION = "add";
	protected static final String REMOVE_OPERATION = "remove";
	protected static final String NO_LOCATION_PREFIX = "ERROR No locations for";

	private static final long serialVersionUID = 1L;


	/**
	 * Constructor
	 */
	public FileLocationDBServlet() {
		super();
	}

	private FileLocationDB getLocationDB() throws ServletException {
		try {
			return (FileLocationDB) wayback.getCachedInstance(FILE_LOCATION_DB_CLASS);
		} catch (ConfigurationException e) {
			throw new ServletException(e);
		}
	}
	
	public void destroy() {
		try {
			getLocationDB().shutdownDB();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		String message;
		FileLocationDB locationDB = getLocationDB();
		try {
			message = handleOperation(locationDB,queryMap);
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			httpResponse.setContentType("text/plain");
			OutputStream os = httpResponse.getOutputStream();
			os.write(message.getBytes());
		} catch (ParseException e) {
			e.printStackTrace();
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
		}
	}

	private String handleOperation(FileLocationDB locationDB, Map queryMap)
			throws ParseException {

		String operation = getRequiredMapParam(queryMap, OPERATION_ARGUMENT);
		String message;
		try {
			if (operation.equals(LOOKUP_OPERATION)) {
				String arcName = getRequiredMapParam(queryMap, NAME_ARGUMENT);

				message = NO_LOCATION_PREFIX + " " + arcName;
				String arcUrls[] = locationDB.arcToUrls(arcName);
				if (arcUrls != null && arcUrls.length > 0) {
					StringBuffer buf = new StringBuffer("OK ");
					for (int i = 0; i < arcUrls.length; i++) {
						if (buf.length() > 0) {
							buf.append("\n");
						}
						buf.append(arcUrls[i]);
					}
					message = buf.toString();
				}

			} else {

				String arcName = getRequiredMapParam(queryMap, NAME_ARGUMENT);
				String arcUrl = getRequiredMapParam(queryMap, URL_ARGUMENT);
				if (operation.equals(ADD_OPERATION)) {

					locationDB.addArcUrl(arcName, arcUrl);
					message = "OK added url " + arcUrl + " for " + arcName;

				} else if (operation.equals(REMOVE_OPERATION)) {

					getLocationDB().removeArcUrl(arcName, arcUrl);
					message = "OK removed url " + arcUrl + " for " + arcName;

				} else {

					throw new ParseException("Unknown operation. Must be one "
							+ "of " + LOOKUP_OPERATION + "," + ADD_OPERATION
							+ ", or " + REMOVE_OPERATION + ".", 0);
				}
			}

		} catch (DatabaseException e) {
			e.printStackTrace();
			message = e.getMessage();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}
}
