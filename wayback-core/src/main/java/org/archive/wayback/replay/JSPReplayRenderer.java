/* JSPReplayRenderer
 *
 * $Id$:
 *
 * Created on May 7, 2010.
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

package org.archive.wayback.replay;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

/**
 * ReplayRenderer implementation which just forwards responsibility for 
 * rendering a resource to a .jsp file.
 * 
 * @author brad
 *
 */
public class JSPReplayRenderer implements ReplayRenderer {
	private String targetJsp = null;
	private boolean wrap = true;

	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results)
			throws ServletException, IOException, WaybackException {
		UIResults uiResults =
			new UIResults(wbRequest, uriConverter, results, result, resource);
		if(wrap) {
			uiResults.forwardWrapped(httpRequest, httpResponse, 
					targetJsp, wbRequest.getAccessPoint().getWrapperJsp());
		} else {
			uiResults.forward(httpRequest, httpResponse, 
					targetJsp);
		}
	}
	
	/**
	 * @return the context-relative path to the .jsp responsible for rendering
	 * the resource
	 */
	public String getTargetJsp() {
		return targetJsp;
	}

	/**
	 * @param targetJsp the context-relative path to the .jsp responsible for
	 * rendering the resource
	 */
	public void setTargetJsp(String targetJsp) {
		this.targetJsp = targetJsp;
	}

	/**
	 * @return true if the jsp should be wrapped in the stardard UI template
	 * wrapper jsp for the AccessPoint.
	 */
	public boolean isWrap() {
		return wrap;
	}

	/**
	 * @param wrap if true then the jsp configured for this page will be 
	 * wrapped in the standard template used for the current AccessPoint, if 
	 * false then the jsp configured is responsible for rendering the entire
	 * content.
	 */
	public void setWrap(boolean wrap) {
		this.wrap = wrap;
	}

}
