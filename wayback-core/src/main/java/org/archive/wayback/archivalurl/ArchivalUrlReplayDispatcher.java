/* ArchivalUrlReplayRendererDispatcher
 *
 * $Id$
 *
 * Created on 11:38:02 AM Aug 9, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.archivalurl;

import java.util.List;

import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.DateRedirectReplayRenderer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArchivalUrlReplayDispatcher implements ReplayDispatcher {

	/**
	 * MIME type of documents which should be marked up with javascript to
	 * rewrite URLs inside document
	 */
	private final static String TEXT_HTML_MIME = "text/html";
	private final static String TEXT_XHTML_MIME = "application/xhtml";
	private final static String TEXT_CSS_MIME = "text/css";
	private final static String ASX_MIME = "video/x-ms-asf";
	private final static String ASX_EXTENSION = ".asx";
	

	// TODO: make this configurable
	private final static long MAX_HTML_MARKUP_LENGTH = 1024 * 1024 * 5;
	
	private ReplayRenderer transparent = 
		new ArchivalUrlTransparentReplayRenderer();

	private ReplayRenderer redirect = new DateRedirectReplayRenderer();
	private ArchivalUrlReplayRenderer archivalHTML =
		new ArchivalUrlReplayRenderer();
	private ArchivalUrlCSSReplayRenderer archivalCSS =
		new ArchivalUrlCSSReplayRenderer();
	private ArchivalUrlASXReplayRenderer archivalASX =
		new ArchivalUrlASXReplayRenderer();

	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayDispatcher#getRenderer(org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource)
	 */
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			SearchResult result, Resource resource) {

		// if the result is not for the exact date requested, redirect to the
		// exact date. some capture dates are not 14 digits, only compare as 
		// many digits as are in the result date:
		String reqDateStr = wbRequest.get(WaybackConstants.REQUEST_EXACT_DATE);
		String resDateStr = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		if(!resDateStr.equals(reqDateStr.substring(0, resDateStr.length()))) {
			return redirect;
		}
		
		// only bother attempting  markup on pages smaller than some size:
		if (resource.getRecordLength() < MAX_HTML_MARKUP_LENGTH) {

			String resultMime = result.get(WaybackConstants.RESULT_MIME_TYPE);
			// HTML and XHTML docs get marked up as HTML
			if (-1 != resultMime.indexOf(TEXT_HTML_MIME)) {
				return archivalHTML;
			}
			if (-1 != resultMime.indexOf(TEXT_XHTML_MIME)) {
				return archivalHTML;
			}
			// CSS docs get marked up as CSS
			if (-1 != resultMime.indexOf(TEXT_CSS_MIME)) {
				return archivalCSS;
			}
			if (-1 != resultMime.indexOf(ASX_MIME)) {
				return archivalASX;
			}
			String resultPath = result.get(WaybackConstants.RESULT_URL_KEY);
			resultPath = resultPath.substring(resultPath.indexOf('/'));
			int queryIdx = resultPath.indexOf('?');
			if(queryIdx > 0) {
				resultPath = resultPath.substring(0,queryIdx-1);
			}
			if(resultPath.endsWith(ASX_EXTENSION)) {
				return archivalASX;
			}
		}
		
		// everything else goes transparently:
		return transparent;
	}

	/**
	 * @return
	 * @see org.archive.wayback.archivalurl.ArchivalUrlReplayRenderer#getJsInserts()
	 */
	public List<String> getJsInserts() {
		return archivalHTML.getJsInserts();
	}

	/**
	 * @return
	 * @see org.archive.wayback.archivalurl.ArchivalUrlReplayRenderer#getJspInserts()
	 */
	public List<String> getJspInserts() {
		return archivalHTML.getJspInserts();
	}

	/**
	 * @param jsInserts
	 * @see org.archive.wayback.archivalurl.ArchivalUrlReplayRenderer#setJsInserts(java.util.List)
	 */
	public void setJsInserts(List<String> jsInserts) {
		archivalHTML.setJsInserts(jsInserts);
	}

	/**
	 * @param jspInserts
	 * @see org.archive.wayback.archivalurl.ArchivalUrlReplayRenderer#setJspInserts(java.util.List)
	 */
	public void setJspInserts(List<String> jspInserts) {
		archivalHTML.setJspInserts(jspInserts);
	}

	/**
	 * @return
	 * @see org.archive.wayback.archivalurl.ArchivalUrlReplayRenderer#isServerSideRendering()
	 */
	public boolean isServerSideRendering() {
		return archivalHTML.isServerSideRendering();
	}

	/**
	 * @param isServerSideRendering
	 * @see org.archive.wayback.archivalurl.ArchivalUrlReplayRenderer#setServerSideRendering(boolean)
	 */
	public void setServerSideRendering(boolean isServerSideRendering) {
		archivalHTML.setServerSideRendering(isServerSideRendering);
		archivalCSS.setServerSideRendering(isServerSideRendering);
	}
}
