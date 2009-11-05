/* JSPExecutor
 *
 * $Id$
 *
 * Created on 4:00:41 PM Apr 13, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
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
package org.archive.wayback.replay;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.WaybackRequest;

/**
 * Class which encapsulates all Replay context information needed to execute
 * a .jsp file in the "context" of a particular replay request.
 * 
 * This class then manages converting a jsp path into the String it produces.
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class JSPExecutor {

	private HttpServletRequest httpRequest = null;
	private HttpServletResponse httpResponse = null;
	private UIResults uiResults = null;
	
	public JSPExecutor(ResultURIConverter uriConverter,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			WaybackRequest wbRequest, CaptureSearchResults results, 
			CaptureSearchResult result, Resource resource) {

		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse; 
		uiResults = 
			new UIResults(wbRequest, uriConverter, results, result, resource);
	}
	
	
	public String jspToString(String jspPath) 
	throws ServletException, IOException {

		StringHttpServletResponseWrapper wrappedResponse = 
			new StringHttpServletResponseWrapper(httpResponse);
		uiResults.forward(httpRequest, wrappedResponse, jspPath);
		return wrappedResponse.getStringResponse();
	}

}
