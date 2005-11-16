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

import java.text.ParseException;
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
	private List localhostNames = null;
	
	public ReplayFilter() {
		super();
	}
	public void init(final FilterConfig c) throws ServletException {
        this.localhostNames = InetAddressUtil.getAllLocalHostNames();		
		super.init(c);
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.core.RequestFilter#parseRequest(javax.servlet.http.HttpServletRequest)
	 */
	@Override
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

		String referer = httpRequest.getHeader("REFERER");
		if (referer == null) {
			referer = "";
		}
		wbRequest.put(WaybackConstants.REQUEST_REFERER_URL,referer);

		try {
			wbRequest.put(WaybackConstants.REQUEST_EXACT_DATE,
					Timestamp.currentTimestamp().getDateStr());
		} catch (ParseException e) {
			// Shouldn't happen...
			e.printStackTrace();
		}

		
		
		return wbRequest;
	}
    protected boolean isLocalRequest(HttpServletRequest httpRequest) {
        return this.localhostNames.contains(httpRequest.getServerName());
    }

}
