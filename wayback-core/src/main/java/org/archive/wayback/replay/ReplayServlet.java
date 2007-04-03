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
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.core.WaybackServlet;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.WaybackException;

/**
 * Servlet implementation for Wayback Replay requests.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class ReplayServlet extends WaybackServlet {
	private static final Logger LOGGER = Logger.getLogger(ReplayServlet.class
			.getName());

	private static final String WMREQUEST_ATTRIBUTE = "wmrequest.attribute";

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 */
	public ReplayServlet() {
		super();
	}

	public void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		WaybackRequest wbRequest = (WaybackRequest) httpRequest
				.getAttribute(WMREQUEST_ATTRIBUTE);

		ReplayRenderer renderer;
		try {
			renderer = wayback.getReplayRenderer();
		} catch (ConfigurationException e1) {
			throw new ServletException(e1);
		}
		Resource resource = null;
		try {

			if (wbRequest == null) {
				wbRequest = wayback.getQueryParser().parseQuery(httpRequest);
			}
			// maybe redirect to a better URI for the request given:
			wbRequest.checkBetterRequest();
			
			ResourceIndex idx = wayback.getResourceIndex();
			ResourceStore store = wayback.getResourceStore();
			ResultURIConverter uriConverter = wayback.getURIConverter();
			uriConverter.setWbRequest(wbRequest);

			SearchResults results = idx.query(wbRequest);

			// TODO: check which versions are actually accessible right now?
			SearchResult closest = results.getClosest(wbRequest);
			resource = store.retrieveResource(closest);

			renderer.renderResource(httpRequest, httpResponse, wbRequest,
					closest, resource, uriConverter);

		} catch (ResourceNotInArchiveException nia) {

			LOGGER.info("NotInArchive\t"
					+ wbRequest.get(WaybackConstants.REQUEST_URL));
			renderer.renderException(httpRequest, httpResponse, wbRequest, nia);

		} catch (BetterRequestException bre) {

			httpResponse.sendRedirect(bre.getBetterURI());

		} catch (WaybackException wbe) {

			renderer.renderException(httpRequest, httpResponse, wbRequest, wbe);

		} catch (Exception e) {
			// TODO show something Wayback'ish to the user rather than letting
			// the container deal?
			throw new ServletException(e);
		} finally {
			if (resource != null) {
				resource.close();
			}
		}
	}
}
