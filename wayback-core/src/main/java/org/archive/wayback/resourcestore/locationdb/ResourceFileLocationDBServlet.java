/* FileLocationDBServlet
 *
 * $Id$
 *
 * Created on 5:35:31 PM Aug 21, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.locationdb;

import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.resourcestore.locationdb.ResourceFileLocationDB;
import org.archive.wayback.webapp.ServletRequestContext;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFileLocationDBServlet extends ServletRequestContext {

	protected static final String OPERATION_ARGUMENT = "operation";
	protected static final String NAME_ARGUMENT = "name";
	protected static final String URL_ARGUMENT = "url";
	protected static final String START_ARGUMENT = "start";
	protected static final String END_ARGUMENT = "end";
	protected static final String LOOKUP_OPERATION = "lookup";
	protected static final String GETMARK_OPERATION = "getmark";
	protected static final String GETRANGE_OPERATION = "getrange";
	protected static final String ADD_OPERATION = "add";
	protected static final String REMOVE_OPERATION = "remove";
	protected static final String NO_LOCATION_PREFIX = "ERROR No locations for";

	private static final long serialVersionUID = 1L;
	private ResourceFileLocationDB locationDB = null;

	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {
		@SuppressWarnings("unchecked")
		Map<String,String[]> queryMap = httpRequest.getParameterMap();
		String message;
		ResourceFileLocationDB locationDB = getLocationDB();
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
		return true;
	}

	private String handleOperation(ResourceFileLocationDB locationDB, 
			Map<String,String[]> queryMap)
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
						buf.append("\n");
						buf.append(arcUrls[i]);
					}
					message = buf.toString();
				}

			} else if (operation.equals(GETMARK_OPERATION)) {

				message = "OK \n" + String.valueOf(locationDB.getCurrentMark());

			} else if (operation.equals(GETRANGE_OPERATION)) {

				long start = Long.parseLong(getRequiredMapParam(queryMap, START_ARGUMENT));
				long end = Long.parseLong(getRequiredMapParam(queryMap, END_ARGUMENT));
				Iterator<String> itr = locationDB.getArcsBetweenMarks(start,end);
				StringBuilder str = new StringBuilder();
				str.append("OK ");
				while(itr.hasNext()) {
					str.append("\n");
					str.append((String)itr.next());
				}
				message = str.toString();
				
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
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			message = e.getMessage();
		}
		return message;
	}

	/**
	 * @return the locationDB
	 */
	public ResourceFileLocationDB getLocationDB() {
		return locationDB;
	}

	/**
	 * @param locationDB the locationDB to set
	 */
	public void setLocationDB(ResourceFileLocationDB locationDB) {
		this.locationDB = locationDB;
	}
}
