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
package org.archive.wayback.proxy;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.RequestParser;
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
public class ProxyRequestParser extends CompositeRequestParser {
	private ProxyReplayRequestParser prrp = new ProxyReplayRequestParser(this);
	protected RequestParser[] getRequestParsers() {
		prrp.init();
		RequestParser[] theParsers = {
				prrp,
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
	            // Get the id from the request. If no id, use the ip-address instead.
	            // Then get the timestamp (or rather datestr) matching this id.
	            String id = httpRequest.getHeader("Proxy-Id");
	            if (id == null)
	                    id = httpRequest.getRemoteAddr();
	            // TODO: This is hacky.
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
