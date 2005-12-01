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

package org.archive.wayback.replay;

import java.io.IOException;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.core.WaybackLogic;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.WaybackException;

/**
 * Servlet implementation for Wayback Replay requests.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class ReplayServlet extends HttpServlet {
	private static final Logger LOGGER = Logger.getLogger(ReplayServlet.class
			.getName());

	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final long serialVersionUID = 1L;

	private WaybackLogic wayback = new WaybackLogic();

	/**
	 * Constructor
	 */
	public ReplayServlet() {
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
			wayback.init(p);
		} catch (Exception e) {
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

	private WaybackRequest parseCGIRequest(HttpServletRequest httpRequest)
			throws BadQueryException {
		WaybackRequest wbRequest = new WaybackRequest();
		Map queryMap = httpRequest.getParameterMap();
		Set keys = queryMap.keySet();
		Iterator itr = keys.iterator();
		while (itr.hasNext()) {
			String key = (String) itr.next();
			String val = getMapParam(queryMap, key);
			wbRequest.put(key, val);
		}
		String referer = httpRequest.getHeader("REFERER");
		if (referer == null) {
			referer = null;
		}
		wbRequest.put(WaybackConstants.REQUEST_REFERER_URL, referer);

		return wbRequest;
	}

	private SearchResult getClosest(SearchResults results,
			WaybackRequest wbRequest) throws ParseException {

		SearchResult closest = null;
		long closestDistance = 0;
		SearchResult cur = null;
		Timestamp wantTimestamp;
		wantTimestamp = Timestamp.parseBefore(wbRequest
				.get(WaybackConstants.REQUEST_EXACT_DATE));

		Iterator itr = results.iterator();
		while (itr.hasNext()) {
			cur = (SearchResult) itr.next();
			long curDistance;
			try {
				Timestamp curTimestamp = Timestamp.parseBefore(cur
						.get(WaybackConstants.RESULT_CAPTURE_DATE));
				curDistance = curTimestamp
						.absDistanceFromTimestamp(wantTimestamp);
			} catch (ParseException e) {
				continue;
			}
			if ((closest == null) || (curDistance < closestDistance)) {
				closest = cur;
				closestDistance = curDistance;
			}
		}
		return closest;
	}

	public void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		WaybackRequest wbRequest = (WaybackRequest) httpRequest
				.getAttribute(WMREQUEST_ATTRIBUTE);

		ResourceIndex idx = wayback.getResourceIndex();
		ResourceStore store = wayback.getResourceStore();
		ReplayResultURIConverter uriConverter = wayback.getURIConverter();
		ReplayRenderer renderer = wayback.getReplayRenderer();
		Resource resource = null;
		try {

			if (wbRequest == null) {
				wbRequest = parseCGIRequest(httpRequest);
			}

			SearchResults results = idx.query(wbRequest);

			SearchResult closest = getClosest(results, wbRequest);

			// TODO loop here looking for closest online/available version?
			// OPTIMIZ maybe assume version is here and redirect now if not
			// exactly the date user requested, before retrieving it...
			resource = store.retrieveResource(closest);

			renderer.renderResource(httpRequest, httpResponse, wbRequest,
					closest, resource, uriConverter);

		} catch (ResourceNotInArchiveException nia) {

			LOGGER.info("NotInArchive\t"
					+ wbRequest.get(WaybackConstants.REQUEST_URL));
			renderer.renderException(httpRequest, httpResponse, wbRequest, nia);

		} catch (WaybackException wbe) {

			renderer.renderException(httpRequest, httpResponse, wbRequest, wbe);

		} catch (Exception e) {
			// TODO show something Wayback'ish to the user rather than letting
			// the container deal?
			e.printStackTrace();
			throw new ServletException(e.getMessage());
		} finally {
			if (resource != null) {
				resource.close();
			}
		}
	}
}
