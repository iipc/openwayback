/* FormRequestParser
 *
 * $Id$
 *
 * Created on 4:45:06 PM Apr 24, 2007.
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
package org.archive.wayback.requestparser;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPoint;

/**
 * RequestParser which attempts to extract data from an HTML form, that is, from
 * HTTP GET request arguments
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FormRequestParser extends WrappedRequestParser {
	/**
	 * @param wrapped the BaseRequestParser being wrapped
	 */
	public FormRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	/**
	 * CGI argument name for Submit button...
	 */
	private final static String SUBMIT_BUTTON = "Submit";

	/*
	 * Stuff whatever GET/POST arguments are sent up into the returned
	 * WaybackRequest object, except the Submit button argument.
	 */
	public WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint accessPoint) throws BetterRequestException {

		WaybackRequest wbRequest = null;
		@SuppressWarnings("unchecked")
		Map<String,String[]> queryMap = httpRequest.getParameterMap();
		if(queryMap.size() > 0) {
			wbRequest = new WaybackRequest();
			
			String base = accessPoint.translateRequestPath(httpRequest);
			if(base.startsWith(REPLAY_BASE)) {
				wbRequest.setReplayRequest();
			} else if(base.startsWith(QUERY_BASE)) {
				wbRequest.setCaptureQueryRequest();
			} else if(base.startsWith(XQUERY_BASE)){
				wbRequest.setCaptureQueryRequest();
				wbRequest.setXMLMode(true);
				
			} else {
				return null;
			}
			wbRequest.setResultsPerPage(getMaxRecords());
			Set<String> keys = queryMap.keySet();
			Iterator<String> itr = keys.iterator();
			while(itr.hasNext()) {
				String key = itr.next();
				if(key.equals(SUBMIT_BUTTON)) {
					continue;
				}
				// just jam everything else in:
				String val = AccessPoint.getMapParam(queryMap,key);
				if(key.equals(WaybackRequest.REQUEST_URL)) {
					String scheme = UrlOperations.urlToScheme(val);
					if(scheme == null) {
						val = UrlOperations.HTTP_SCHEME + val;
					}
				}
				wbRequest.put(key,val);
			}
			String partialTS = wbRequest.getReplayTimestamp();
			if(partialTS != null) {
				if(wbRequest.getStartTimestamp()== null) {
					String startTS = Timestamp.parseBefore(partialTS).getDateStr();
					wbRequest.setStartTimestamp(startTS);
				}
				if(wbRequest.getEndTimestamp() == null) {
					String endTS = Timestamp.parseAfter(partialTS).getDateStr();
					wbRequest.setEndTimestamp(endTS);
				}
			} else {
				if(wbRequest.getStartTimestamp()== null) {
					wbRequest.setStartTimestamp(getEarliestTimestamp());
				}
				if(wbRequest.getEndTimestamp() == null) {
					wbRequest.setEndTimestamp(getLatestTimestamp());
				}
			}
		}
		return wbRequest;
	}
}
