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
		return toQueryString(url) + STAR;
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
		int dateLen = 0;
		if(datespec != null) {
			dateLen = datespec.length();
		}
		StringBuilder sb = 
			new StringBuilder(url.length() + dateLen +10);
		if(dateLen > 0) {
			sb.append(datespec);
		}
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
		if(dateLen > 0) {
			sb.append("/");
		}
		sb.append(UrlOperations.stripDefaultPortFromUrl(url));
		return sb.toString();
	}
}
