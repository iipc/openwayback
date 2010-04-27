/* DatelessReplayRequestParser
 *
 * $Id$:
 *
 * Created on Apr 26, 2010.
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

import java.net.MalformedURLException;
import java.net.URL;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.PathRequestParser;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPoint;


/**
 * @author brad
 *
 */
public class DatelessReplayRequestParser extends PathRequestParser {

	/**
	 * @param wrapped the BaseRequestParser being wrapped
	 */
	public DatelessReplayRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	public WaybackRequest parse(String requestPath, AccessPoint accessPoint)
			throws BetterRequestException, BadQueryException {
		/*
		 *
		 * We're trying to catch requests without a datespec, in which case,
		 * we just redirect to the same request, inserting today's datespec,
		 * and then we'll let the normal redirection occur.
		 *
		 * The one tricky point is that we don't want to defeat the
		 * server-relative redirection handling, so we want to do some
		 * inspection to make sure it actually looks like an URL, and not like:
		 * 
		 *   images/foo.gif
		 *   redirect.php?blargity=blargblarg
		 * 
		 * What would be perfect is if the user supplied http:// at the front.
		 * 
		 * So, we'll assume that if we see that, we either match, or throw a 
		 * BadQueryException.
		 *   
		 */
		
		String scheme = UrlOperations.urlToScheme(requestPath);
		if(scheme == null) {
			try {
				URL u = new URL(UrlOperations.HTTP_SCHEME + requestPath);
				// does the authority look legit?
				if(u.getUserInfo() != null) {
					throw new BadQueryException("Unable to handle URLs with user information");
				}
				if(UrlOperations.isAuthority(u.getAuthority())) {
					// ok, we're going to assume this is good:
					String nowTS = Timestamp.currentTimestamp().getDateStr();
					String newUrl = 
						accessPoint.getUriConverter().makeReplayURI(nowTS, requestPath);
					throw new BetterRequestException(newUrl);
				}
			} catch(MalformedURLException e) {
				// eat it silently
			}
		} else {
			// OK, we're going to assume this is a replay request, sans timestamp,
			// ALWAYS redirect:
	
			String nowTS = Timestamp.currentTimestamp().getDateStr();
			String newUrl = 
				accessPoint.getUriConverter().makeReplayURI(nowTS, requestPath);
			throw new BetterRequestException(newUrl);
		}
		return null;
	}

}
