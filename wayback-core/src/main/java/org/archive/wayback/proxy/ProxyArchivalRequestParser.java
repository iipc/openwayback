/* ProxyArchivalRequestParser
 *
 * $Id$
 *
 * Created on 4:01:04 PM Apr 6, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
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

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.RequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDatePrefixQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.PathPrefixDateRangeQueryRequestParser;
import org.archive.wayback.archivalurl.requestparser.ReplayRequestParser;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.requestparser.CompositeRequestParser;
import org.archive.wayback.requestparser.FormRequestParser;
import org.archive.wayback.requestparser.OpenSearchRequestParser;
import org.archive.wayback.util.bdb.BDBMap;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class ProxyArchivalRequestParser  extends CompositeRequestParser {
	private ProxyReplayRequestParser prrp = new ProxyReplayRequestParser(this);
	protected RequestParser[] getRequestParsers() {
		prrp.init();
		RequestParser[] theParsers = {
				prrp,
				new PathDatePrefixQueryRequestParser(this),
				new PathDateRangeQueryRequestParser(this),
				new PathPrefixDatePrefixQueryRequestParser(this),
				new PathPrefixDateRangeQueryRequestParser(this),
				new ReplayRequestParser(this),
				new OpenSearchRequestParser(this),
				new FormRequestParser(this) 
				};
		return theParsers;
	}
	public List<String> getLocalhostNames() {
		return prrp.getLocalhostNames();
	}
	public void setLocalhostNames(List<String> localhostNames) {
		prrp.setLocalhostNames(localhostNames);
	}

	public WaybackRequest parse(HttpServletRequest httpRequest,
            AccessPoint wbContext) throws BadQueryException, BetterRequestException {
	
	    WaybackRequest wbRequest = super.parse(httpRequest, wbContext);
	    if (wbRequest != null) {
            String id = httpRequest.getHeader("Proxy-Id");
            if (id == null)
                    id = httpRequest.getRemoteAddr();

            // Get the id from the request. 
	    	// If no id, use the ip-address instead.
	    	// Check if the parser parsed a replay request and found a
	    	// timestamp. If so, then we need to store the timestamp and
	    	// redirect, which is done with a BetterRequestException:
	    	if(wbRequest.isReplayRequest()) {
	    		String replayTimestamp = wbRequest.getReplayTimestamp();
	    		if(replayTimestamp != null) {
	    			BDBMap.addTimestampForId(httpRequest.getContextPath(),
	    					id, replayTimestamp);
	    		}
    			throw new BetterRequestException(wbRequest.getRequestUrl());
	    	}
	    	
            // Then get the timestamp (or rather datestr) matching this id.
            // TODO: This is hacky - need generic way to store session data
            String replayDateStr = BDBMap.getTimestampForId(
            		httpRequest.getContextPath(), id);
            wbRequest.setReplayTimestamp(replayDateStr);
            wbRequest.setAnchorTimestamp(replayDateStr);
	    }
	    return wbRequest;
	}
	/**
	 * @return the addDefaults
	 */
	public boolean isAddDefaults() {
		return prrp.isAddDefaults();
	}

	/**
	 * @param addDefaults the addDefaults to set
	 */
	public void setAddDefaults(boolean addDefaults) {
		prrp.setAddDefaults(addDefaults);
	}
}
