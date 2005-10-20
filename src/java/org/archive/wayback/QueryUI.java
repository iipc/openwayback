/* QueryUI
 * 
 * Created on Oct 18, 2005
 *
 * Copyright (C) 2005 Internet Archive.
 * 
 * This file is part of the Wayback Machine (archive-access.sourceforge.net).
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

package org.archive.wayback;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.core.WaybackLogic;

/**
 * Uses ResourceStore, ResourceIndex via WaybackLogic, to transform a WMRequest
 * into first a set of ResourceResults, then into an format suitable for
 * end-users.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public interface QueryUI {
	/**
	 * Initialize this QueryUI. Pass in the specific configurations via
	 * Properties.
	 * 
	 * @param p
	 *            Generic properties bag for configurations
	 * @throws IOException
	 */
	public void init(final Properties p) throws IOException;

	/**
	 * Process a Wayback Machine Query request.
	 * 
	 * @param wayback
	 *            WaybackLogic object
	 * @param wmRequest
	 *            pre-parsed WMRequest object
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws IOException
	 * @throws ServletException
	 */
	public void handle(final WaybackLogic wayback, final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	/**
	 * Show results for a wayback request containing results for a single Url.
	 * 
	 * @param wayback
	 *            WaybackLogic object
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param wmRequest
	 *            pre-parsed WMRequest object
	 * @param results
	 *            returns from ResourceIndex
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showQueryResults(WaybackLogic wayback,
			HttpServletRequest request, HttpServletResponse response,
			final WMRequest wmRequest, final ResourceResults results)
			throws IOException, ServletException;

	/**
	 * Show results for a wayback request containing results for multiple Urls.
	 * 
	 * @param wayback
	 *            WaybackLogic object
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @param wmRequest
	 *            pre-parsed WMRequest object
	 * @param results
	 *            returns from ResourceIndex
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showPathQueryResults(WaybackLogic wayback,
			HttpServletRequest request, HttpServletResponse response,
			final WMRequest wmRequest, final ResourceResults results)
			throws IOException, ServletException;

	/**
	 * Show error page for no results for a wayback request.
	 * 
	 * @param wmRequest
	 *            pre-parsed WMRequest object
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showNoMatches(final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	/**
	 * Show error page for inability to communicate with ResourceIndex.
	 * 
	 * @param wmRequest
	 *            pre-parsed WMRequest object
	 * @param request
	 *            HttpServletRequest
	 * @param response
	 *            HttpServletResponse
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showIndexNotAvailable(final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	/**
	 * Return an error page tot he User indicating that an unexpected error
	 * occurred.
	 * 
	 * @param wmRequest
	 * @param request
	 * @param response
	 * @param message
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showWaybackException(WMRequest wmRequest,
			HttpServletRequest request, HttpServletResponse response,
			String message) throws IOException, ServletException;
	
}
