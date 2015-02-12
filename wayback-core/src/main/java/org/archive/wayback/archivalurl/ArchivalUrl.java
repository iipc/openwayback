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
	public String getDateSpec() {
		return getDateSpec(wbRequest.getReplayTimestamp());
	}
	
	public String getDateSpec(String datespec) {
		return getDateSpec(wbRequest, datespec);
	}

	/**
	 * Return string rendition of context flags.
	 * @param wbRequest wayback request object providing context flags
	 * @return string with textual notation of context flags, possibly empty.
	 */
	public static String getFlags(WaybackRequest wbRequest) {
		StringBuilder sb = new StringBuilder();
		appendFlags(sb, wbRequest);
		return sb.toString();
	}

	/**
	 * Apped textual notation of context flags to StringBuffer given.
	 * @param sb receives output
	 * @param wbRequest provides context flags information
	 */
	protected static void appendFlags(StringBuilder sb, WaybackRequest wbRequest) {
		if (wbRequest.isCSSContext()) {
			sb.append(ArchivalUrlRequestParser.CSS_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if (wbRequest.isJSContext()) {
			sb.append(ArchivalUrlRequestParser.JS_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if (wbRequest.isIMGContext()) {
			sb.append(ArchivalUrlRequestParser.IMG_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if (wbRequest.isObjectEmbedContext()) {
			sb.append(ArchivalUrlRequestParser.OBJECT_EMBED_WRAPPED_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if (wbRequest.isIdentityContext()) {
			sb.append(ArchivalUrlRequestParser.IDENTITY_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if (wbRequest.isIFrameWrapperContext()) {
			sb.append(ArchivalUrlRequestParser.IFRAME_WRAPPED_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
		if (wbRequest.isFrameWrapperContext()) {
			sb.append(ArchivalUrlRequestParser.FRAME_WRAPPED_CONTEXT);
			sb.append(ArchivalUrlRequestParser.FLAG_DELIM);
		}
	}

	/**
	 * Given a date, create a new datespec + flags
	 * which represent the same options as requested by the WaybackRequest
	 * @param timestamp the 14-digit timestamp to use
	 * @return a String representing the flags on the WaybackRequest for the
	 * specified date
	 */
	public static String getDateSpec(WaybackRequest wbRequest, String datespec) {
		int dateLen = 0;
		if (datespec != null) {
			dateLen = datespec.length();
		}
		StringBuilder sb = new StringBuilder(dateLen + 10);
		if (dateLen > 0) {
			sb.append(datespec);
		}
		appendFlags(sb, wbRequest);
		return sb.toString();
	}
	
	public String toString(String datespec, String url) {
		int dateLen = 0;
		if(datespec != null) {
			dateLen = datespec.length();
		}
		StringBuilder sb = 
			new StringBuilder(url.length() + dateLen +10);
		String dateSpec = getDateSpec(datespec);
		sb.append(dateSpec);
		if(dateSpec.length() > 0) {
			sb.append("/");
		}
		sb.append(UrlOperations.stripDefaultPortFromUrl(url));
		return sb.toString();
	}

	/**
	 * @param wbRequest
	 * @param flagsStr : "js_", "", "cs_", "cs_js_"
	 */
	public static void assignFlags(WaybackRequest wbRequest, String flagsStr) {
		if(flagsStr != null) {
			String[] flags = flagsStr.split(
					ArchivalUrlRequestParser.FLAG_DELIM);
			for(String flag: flags) {
				if(flag.equals(ArchivalUrlRequestParser.CSS_CONTEXT)) {
					wbRequest.setCSSContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.JS_CONTEXT)) {
					wbRequest.setJSContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.IMG_CONTEXT)) {
					wbRequest.setIMGContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.OBJECT_EMBED_WRAPPED_CONTEXT)) {
					wbRequest.setObjectEmbedContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.IDENTITY_CONTEXT)) {
					wbRequest.setIdentityContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.FRAME_WRAPPED_CONTEXT)) {
					wbRequest.setFrameWrapperContext(true);
				} else if(flag.equals(ArchivalUrlRequestParser.IFRAME_WRAPPED_CONTEXT)) {
					wbRequest.setIFrameWrapperContext(true);
				} else if(flag.startsWith(ArchivalUrlRequestParser.CHARSET_MODE)) {
					String modeString = flag.substring(
							ArchivalUrlRequestParser.CHARSET_MODE.length());
					int mode = Integer.parseInt(modeString);
					wbRequest.setCharsetMode(mode);
				}
			}
		}
	}
	
}
