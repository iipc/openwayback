/* ArchivalUrl
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

package org.archive.wayback.archivalurl;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.url.UrlOperations;

/**
 * @author brad
 *
 */
public class ArchivalUrl {
	public final static String STAR = "*";
	private WaybackRequest wbRequest;
//	public ArchivalUrl(String path) {
//		
//	}
	public ArchivalUrl(WaybackRequest wbRequest) {
		this.wbRequest = wbRequest;
	}

	public String toString() {
		if(wbRequest.isReplayRequest()) {
			return toReplayString(wbRequest.getRequestUrl());
		} else if(wbRequest.isCaptureQueryRequest()) {
			return toQueryString(wbRequest.getRequestUrl()); 
		}
		return toPrefixQueryString(wbRequest.getRequestUrl()); 			
	}
	public String toPrefixQueryString(String url) {
		return toQueryString("url" + STAR);
	}
	public String toQueryString(String url) {
		String datespec = STAR;
		if((wbRequest.getStartTimestamp() != null) && 
				(wbRequest.getEndTimestamp() != null)) {
			datespec = String.format("%s-%s%s",
					wbRequest.getStartTimestamp(),wbRequest.getEndTimestamp(),
					STAR);
		}
		return toString(datespec,url);
	}

	public String toReplayString(String url) {
		return toString(wbRequest.getReplayTimestamp(),url);
	}

	public String toString(String datespec, String url) {
		StringBuilder sb = 
			new StringBuilder(url.length() + datespec.length()+10);
		sb.append(datespec);
		if(wbRequest.isCSSContext()) {
			sb.append(ArchivalUrlRequestParser.CSS_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if(wbRequest.isJSContext()) {
			sb.append(ArchivalUrlRequestParser.JS_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if(wbRequest.isIMGContext()) {
			sb.append(ArchivalUrlRequestParser.IMG_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if(wbRequest.isIdentityContext()) {
			sb.append(ArchivalUrlRequestParser.IDENTITY_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		sb.append("/");
		sb.append(UrlOperations.stripDefaultPortFromUrl(url));
		return sb.toString();
	}
}
