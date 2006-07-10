/* MetaReplayRenderer
 *
 * $Id$
 *
 * Created on 5:53:19 PM Jul 5, 2006.
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
package org.archive.wayback.timeline;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.BaseReplayRenderer;
import org.archive.wayback.replay.UIReplayResult;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class MetaReplayRenderer extends BaseReplayRenderer {
	private final String RESULT_META_JSP = "ResultMeta.jsp";
	/**
	 * @param httpRequest
	 * @param httpResponse
	 * @param wbRequest
	 * @param result
	 * @param resource
	 * @param uriConverter
	 * @throws ServletException
	 * @throws IOException
	 */
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			SearchResult result, Resource resource,
			ResultURIConverter uriConverter) throws ServletException,
			IOException {

		String finalJspPath = jspPath + "/" + RESULT_META_JSP;

		UIReplayResult uiResult = new UIReplayResult(httpRequest, wbRequest,
				result, resource, uriConverter);

		httpRequest.setAttribute("ui-result", uiResult);

		RequestDispatcher dispatcher = httpRequest
				.getRequestDispatcher(finalJspPath);

		dispatcher.forward(httpRequest, httpResponse);
	}	
}
