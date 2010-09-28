/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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

import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

/**
 * ServletRequestContext enabling remote HTTP GET/POST access to a local 
 * ResourceFileLocationDB. See RemoveResourceFileLocationDB for the client
 * class implemented against this.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFileLocationDBServlet extends AbstractRequestHandler {

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
		try {
			message = handleOperation(queryMap);
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			httpResponse.setContentType("text/plain");
			OutputStream os = httpResponse.getOutputStream();
			os.write(message.getBytes());
		} catch (ParseException e) {
			e.printStackTrace();
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
		} catch(BadQueryException e) {
			e.printStackTrace();
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
		}
		return true;
	}

	private String handleOperation(Map<String,String[]> queryMap)
			throws ParseException, BadQueryException {

		String operation = AbstractRequestHandler.getRequiredMapParam(queryMap, OPERATION_ARGUMENT);
		String message;
		try {
			if (operation.equals(LOOKUP_OPERATION)) {
				String name = AbstractRequestHandler.getRequiredMapParam(queryMap, NAME_ARGUMENT);

				message = NO_LOCATION_PREFIX + " " + name;
				String arcUrls[] = locationDB.nameToUrls(name);
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

				long start = Long.parseLong(AbstractRequestHandler.getRequiredMapParam(queryMap, START_ARGUMENT));
				long end = Long.parseLong(AbstractRequestHandler.getRequiredMapParam(queryMap, END_ARGUMENT));
				Iterator<String> itr = locationDB.getNamesBetweenMarks(start,end);
				StringBuilder str = new StringBuilder();
				str.append("OK ");
				while(itr.hasNext()) {
					str.append("\n");
					str.append((String)itr.next());
				}
				message = str.toString();
				
			} else {

				String name = AbstractRequestHandler.getRequiredMapParam(queryMap, NAME_ARGUMENT);
				String url = AbstractRequestHandler.getRequiredMapParam(queryMap, URL_ARGUMENT);
				if (operation.equals(ADD_OPERATION)) {

					locationDB.addNameUrl(name, url);
					message = "OK added url " + url + " for " + name;

				} else if (operation.equals(REMOVE_OPERATION)) {

					locationDB.removeNameUrl(name, url);
					message = "OK removed url " + url + " for " + name;

				} else {

					throw new ParseException("Unknown operation. Must be one "
							+ "of " + LOOKUP_OPERATION + "," + ADD_OPERATION
							+ ", or " + REMOVE_OPERATION + ".", 0);
				}
			}

		} catch (IOException e) {
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
