/* DomainPrefixReplayDispatcher
 *
 * $Id$
 *
 * Created on 10:20:49 AM Aug 10, 2007.
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
package org.archive.wayback.domainprefix;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.replay.BaseReplayDispatcher;
import org.archive.wayback.replay.DateRedirectReplayRenderer;
import org.archive.wayback.replay.TransparentReplayRenderer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DomainPrefixReplayDispatcher extends BaseReplayDispatcher  {

	private final static String TEXT_HTML_MIME = "text/html";
	private final static String TEXT_XHTML_MIME = "application/xhtml";

	// TODO: make this configurable
	private final static long MAX_HTML_MARKUP_LENGTH = 1024 * 1024 * 5;
	
	private ReplayRenderer redirect = new DateRedirectReplayRenderer();

	private ReplayRenderer transparent = new TransparentReplayRenderer();
	private DomainPrefixReplayRenderer html = new DomainPrefixReplayRenderer();
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.BaseReplayDispatcher#getRenderer(org.archive.wayback.core.WaybackRequest, org.archive.wayback.core.SearchResult, org.archive.wayback.core.Resource)
	 */
	@Override
	public ReplayRenderer getRenderer(WaybackRequest wbRequest,
			SearchResult result, Resource resource) {
		// if the result is not for the exact date requested, redirect to the
		// exact date. some capture dates are not 14 digits, only compare as 
		// many digits as are in the result date:
		String reqDateStr = wbRequest.get(WaybackConstants.REQUEST_EXACT_DATE);
		String resDateStr = result.get(WaybackConstants.RESULT_CAPTURE_DATE);
		if((resDateStr.length() > reqDateStr.length()) ||
				!resDateStr.equals(reqDateStr.substring(0, resDateStr.length()))) {
			return redirect;
		}
		
		// HTML and XHTML docs smaller than some size get marked up as HTML
		if (resource.getRecordLength() < MAX_HTML_MARKUP_LENGTH) {

			if (-1 != result.get(WaybackConstants.RESULT_MIME_TYPE).indexOf(
					TEXT_HTML_MIME)) {
				return html;
			}
			if (-1 != result.get(WaybackConstants.RESULT_MIME_TYPE).indexOf(
					TEXT_XHTML_MIME)) {
				return html;
			}
		}
		
		// everything else goes transparently:
		return transparent;
	}
}
