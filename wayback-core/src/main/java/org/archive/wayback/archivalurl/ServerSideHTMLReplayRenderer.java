/* ServerSideHTMLReplayRenderer
 *
 * $Id$
 *
 * Created on 2:21:16 PM Jul 18, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback.archivalurl;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.TextDocument;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.replay.HttpHeaderProcessor;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ServerSideHTMLReplayRenderer extends TextReplayRenderer {
	/**
	 * @param httpHeaderProcessor
	 */
	public ServerSideHTMLReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
		super(httpHeaderProcessor);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.HTMLReplayRenderer#updatePage(org.archive.wayback.replay.HTMLPage, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.CaptureSearchResult, org.archive.wayback.core.Resource, org.archive.wayback.ResultURIConverter, org.archive.wayback.core.CaptureSearchResults)
	 */
	@Override
	protected void updatePage(TextDocument page, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource resource,
			ResultURIConverter uriConverter, CaptureSearchResults results) 
		throws ServletException, IOException {

		List<String> jspInserts = getJspInserts();

		StringBuilder toInsert = new StringBuilder(300);

		page.resolveAllPageUrls();
		if(jspInserts != null) {
			Iterator<String> itr = jspInserts.iterator();
			while(itr.hasNext()) {
				toInsert.append(page.includeJspString(itr.next(), httpRequest, 
						httpResponse, wbRequest, results, result, resource));
			}
		}

		// insert the new content:
		page.insertAtStartOfBody(toInsert.toString());
	}
}
