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
package org.archive.wayback.proxy;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.TextDocument;
import org.archive.wayback.replay.TextReplayRenderer;

/**
 *
 *
 * @author brad
 */
public class ProxyHTMLMarkupReplayRenderer extends TextReplayRenderer {

	private boolean insertAtStart = false;
	/**
	 * @param httpHeaderProcessor
	 */
	public ProxyHTMLMarkupReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
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
		CharSequence toInsert = buildInsertText(page, httpRequest,
				httpResponse, wbRequest, results, result, resource);
		// insert the new content:
		if (insertAtStart) {
			page.insertAtStartOfBody(toInsert);
		} else {
			page.insertAtEndOfBody(toInsert);
		}
	}

	public boolean isInsertAtStart() {
		return insertAtStart;
	}

	public void setInsertAtStart(boolean insertAtStart) {
		this.insertAtStart = insertAtStart;
	}
}
