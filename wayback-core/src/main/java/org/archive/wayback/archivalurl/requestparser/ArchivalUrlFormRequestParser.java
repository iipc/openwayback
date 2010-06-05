/* ArchivalUrlFormRequestParser
 *
 * $Id$:
 *
 * Created on Jun 4, 2010.
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

package org.archive.wayback.archivalurl.requestparser;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.archivalurl.ArchivalUrl;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.FormRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 * @author brad
 *
 */
public class ArchivalUrlFormRequestParser extends FormRequestParser {
	/**
	 * @param wrapped BaseRequestParser to wrap
	 */
	public ArchivalUrlFormRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}
	public WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint accessPoint) throws BetterRequestException {
		WaybackRequest wbRequest = super.parse(httpRequest, accessPoint);
		if(wbRequest != null) {
			String replayTimestamp = wbRequest.getReplayTimestamp();
			if((replayTimestamp != null) && replayTimestamp.length() == 0) {
				// lets call it a star query:
				// TODO: should we clone?
				wbRequest.setStartTimestamp(null);
				wbRequest.setEndTimestamp(null);
			}
			String requestPath = 
				accessPoint.translateRequestPathQuery(httpRequest);
			ArchivalUrl aUrl = new ArchivalUrl(wbRequest);
			String bestPath = aUrl.toString();
			if(!bestPath.equals(requestPath)) {
				String betterURI = (wbRequest.isReplayRequest() ? 
						accessPoint.getReplayPrefix() : 
							accessPoint.getQueryPrefix()) 
						+ bestPath;
				throw new BetterRequestException(betterURI);
			}
		}
		return wbRequest;
	}
}
