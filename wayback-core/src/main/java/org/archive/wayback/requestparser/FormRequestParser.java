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

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FormRequestParser extends BaseRequestParser {
	/**
	 * CGI argument name for Submit button...
	 */
	private final static String SUBMIT_BUTTON = "Submit";

	/*
	 * Stuff whatever GET/POST arguments are sent up into the returned
	 * WaybackRequest object, except the Submit button argument.
	 */
	public WaybackRequest parse(HttpServletRequest httpRequest, 
			AccessPoint wbContext) {

		WaybackRequest wbRequest = null;
		@SuppressWarnings("unchecked")
		Map<String,String[]> queryMap = httpRequest.getParameterMap();
		if(queryMap.size() > 0) {
			wbRequest = new WaybackRequest();
			
			String base = wbContext.translateRequestPath(httpRequest);
			if(base.startsWith(REPLAY_BASE)) {
				wbRequest.put(WaybackConstants.REQUEST_TYPE,
						WaybackConstants.REQUEST_REPLAY_QUERY);
			} else if(base.startsWith(QUERY_BASE)) {
				wbRequest.put(WaybackConstants.REQUEST_TYPE,
						WaybackConstants.REQUEST_URL_QUERY);
			} else if(base.startsWith(XQUERY_BASE)){
				wbRequest.put(WaybackConstants.REQUEST_TYPE,
						WaybackConstants.REQUEST_URL_QUERY);
				wbRequest.put(WaybackConstants.REQUEST_XML_DATA,"1");
				
			} else {
				return null;
			}
			wbRequest.setResultsPerPage(maxRecords);
			Set<String> keys = queryMap.keySet();
			Iterator<String> itr = keys.iterator();
			while(itr.hasNext()) {
				String key = itr.next();
				if(key.equals(SUBMIT_BUTTON)) {
					continue;
				}
				// just jam everything else in:
				String val = getMapParam(queryMap,key);
				wbRequest.put(key,val);
			}
			if(wbRequest.get(WaybackConstants.REQUEST_START_DATE) == null) {
				wbRequest.put(WaybackConstants.REQUEST_START_DATE, 
						getEarliestTimestamp());
			}
			if(wbRequest.get(WaybackConstants.REQUEST_END_DATE) == null) {
				wbRequest.put(WaybackConstants.REQUEST_END_DATE, 
						getLatestTimestamp());
			}
		}
		if(wbRequest != null) {
			wbRequest.fixup(httpRequest);
		}

		return wbRequest;
	}
}
