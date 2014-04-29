/*
 *  This file is part of the Wayback archival access software
 *   (https://github.com/internetarchive/wayback).
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
package org.archive.wayback.archivalurl.requestparser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 * PathRequestParser that expects <i>TIMESTAMP/URL</i> path pattern.
 * <p>TIMESTAMP component is %-decoded before passed to 
 * @author Kenji Nagahashi
 *
 */
public abstract class DateUrlPathRequestParser extends PathRequestParser {

	public DateUrlPathRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}
	
	private String urldecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			return s;
		}
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.requestparser.PathRequestParser#parse(java.lang.String, org.archive.wayback.webapp.AccessPoint)
	 */
	@Override
	public WaybackRequest parse(String requestPath, AccessPoint accessPoint)
			throws BetterRequestException, BadQueryException {
		int p = requestPath.indexOf('/');
		if (p < 1)
			return null;
		String ts = urldecode(requestPath.substring(0, p));
		// TODO: skip multiple "/"?
		String url = requestPath.substring(p + 1);
		return parseDateUrl(ts, url);
	}

	/**
	 * parse {@code dateStr} and {@code urlStr} and return {@code WaybackRequest} object.
	 * @param dateStr the first path component, %-decoded.
	 * @param urlStr everything after the first slash, non-decoded.
	 * @return {@code WaybackRequest} with parse result, or {@code null} if either
	 *   {@code dateStr} or {@code urlStr} does not match expected syntax. 
	 */
	protected abstract WaybackRequest parseDateUrl(String dateStr, String urlStr)
			throws BetterRequestException, BadQueryException;
}
