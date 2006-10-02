/* ProxyReplayFilter
 *
 * $Id$
 *
 * Created on 6:08:59 PM Nov 14, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.proxy;

import java.util.List;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.archive.util.InetAddressUtil;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.core.RequestFilter;
import org.archive.wayback.core.WaybackRequest;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ReplayFilter extends RequestFilter {
	
	/**
	 * name of attribute in web.xml for specifying an additional hostname that
	 * should be considered "local" for discriminating between Replays and 
	 * Queries
	 */
	private static final String LOCAL_HOSTNAME = "query.localhostname";

	private List localhostNames = null;
	
	/**
	 * Constructor
	 */
	public ReplayFilter() {
		super();
	}
	public void init(final FilterConfig c) throws ServletException {
        this.localhostNames = InetAddressUtil.getAllLocalHostNames();
		String extraLocalHostname = c.getInitParameter(LOCAL_HOSTNAME);
		if ((extraLocalHostname != null) && (extraLocalHostname.length() > 0)) {
			localhostNames.add(extraLocalHostname);
		}
		super.init(c);
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.core.RequestFilter#parseRequest(javax.servlet.http.HttpServletRequest)
	 */
	protected WaybackRequest parseRequest(HttpServletRequest httpRequest) {
		WaybackRequest wbRequest = null;
		if(isLocalRequest(httpRequest)) {
			return wbRequest;
		}
		String requestServer = httpRequest.getServerName();
		String requestPath = httpRequest.getRequestURI();
		//int port = httpRequest.getServerPort();
		String requestQuery = httpRequest.getQueryString();
		String requestScheme = httpRequest.getScheme();
		if (requestQuery != null) {
			requestPath = requestPath + "?" + requestQuery;
		}

		String requestUrl = requestScheme + "://" + requestServer + requestPath;

		wbRequest = new WaybackRequest();
		wbRequest.put(WaybackConstants.REQUEST_URL,requestUrl);
		wbRequest.put(WaybackConstants.REQUEST_TYPE,
				WaybackConstants.REQUEST_REPLAY_QUERY);

          
		// Get the id from the request. If no id, use the ip-address instead.
        // Then get the timestamp (or rather datestr) matching this id.
        String id = httpRequest.getHeader("Proxy-Id");
        if(id == null) id = httpRequest.getRemoteAddr();
        wbRequest.put(WaybackConstants.REQUEST_EXACT_DATE, 
        		Timestamp.getTimestampForId(httpRequest.getContextPath(),id));
		
        wbRequest.fixup(httpRequest);
        
        return wbRequest;
	}
    protected boolean isLocalRequest(HttpServletRequest httpRequest) {
        return this.localhostNames.contains(httpRequest.getServerName());
    }

}
