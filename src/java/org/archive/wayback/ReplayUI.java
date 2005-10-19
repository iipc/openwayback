/* ReplayUI
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

package org.archive.wayback;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.core.WaybackLogic;

/**
 * Uses ResourceIndex, ResourceStore via WaybackLogic to transform a WMRequest
 * into a user-viewable response, either the resource requested, a redirect to a
 * better request, or an error message as to why the request failed.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public interface ReplayUI {

	/**
	 * Initialize this ReplayUI. Pass in the specific configurations via
	 * Properties.
	 * 
	 * @param p
	 *            Generic properties bag for configurations
	 * @throws IOException
	 */
	public void init(final Properties p) throws IOException;

	/**
	 * @param request
	 * @param result
	 * @return user-viewable String URL that will replay the ResourceResult
	 */
	public String makeReplayURI(final HttpServletRequest request,
			final ResourceResult result);

	/**
	 * Process a Wayback Replay request, returning the resource requested, a
	 * redirect to a better/correct URL for the resource, or an error message.
	 * 
	 * @param wayback
	 * @param wmRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void handle(final WaybackLogic wayback, final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	/**
	 * Return a Resource to the user, performing whatever markup or alteration
	 * is required.
	 * 
	 * @param wmRequest
	 * @param result
	 * @param resource
	 * @param request
	 * @param response
	 * @param results
	 * @throws IOException
	 * @throws ServletException
	 */
	public void replayResource(final WMRequest wmRequest,
			final ResourceResult result, final Resource resource,
			final HttpServletRequest request,
			final HttpServletResponse response, final ResourceResults results)
			throws IOException, ServletException;

	/**
	 * Return an error page to the User indicating that the Resource they
	 * requested is not stored in the archive.
	 * 
	 * @param wmRequest
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showNotInArchive(final WMRequest wmRequest,
			final HttpServletRequest request, final HttpServletResponse response)
			throws IOException, ServletException;

	/**
	 * Return an error page to the User indicating that the Resource they
	 * requested is stored in the archive, but is not presently available.
	 * 
	 * @param wmRequest
	 * @param request
	 * @param response
	 * @param message
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showResourceNotAvailable(final WMRequest wmRequest,
			final HttpServletRequest request,
			final HttpServletResponse response, final String message)
			throws IOException, ServletException;

	/**
	 * Return an error page to the User indicating that the ResourceIndex is
	 * presently not available.
	 * 
	 * @param wmRequest
	 * @param request
	 * @param response
	 * @param message
	 * @throws IOException
	 * @throws ServletException
	 */
	public void showIndexNotAvailable(final WMRequest wmRequest,
			final HttpServletRequest request,
			final HttpServletResponse response, final String message)
			throws IOException, ServletException;
}
