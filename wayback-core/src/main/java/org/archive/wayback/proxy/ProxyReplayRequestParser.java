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

import org.archive.util.InetAddressUtil;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.requestparser.BaseRequestParser;
import org.archive.wayback.requestparser.WrappedRequestParser;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ProxyReplayRequestParser extends WrappedRequestParser {

	private List<String> localhostNames = null;
	private boolean addDefaults = true;

	/**
	 * @param wrapped
	 */
	public ProxyReplayRequestParser(BaseRequestParser wrapped) {
		super(wrapped);
	}

	/**
	 * 
	 */
	public void init() {
		if(localhostNames == null) {
			localhostNames = InetAddressUtil.getAllLocalHostNames();
		} else {
			if(addDefaults) {
				localhostNames.addAll(InetAddressUtil.getAllLocalHostNames());
			}
		}
	}
	private boolean isLocalRequest(HttpServletRequest httpRequest) {
		return this.localhostNames.contains(httpRequest.getServerName());
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.requestparser.BaseRequestParser#parse(javax.servlet.http.HttpServletRequest, org.archive.wayback.webapp.WaybackContext)
	 */
	@Override
	public WaybackRequest parse(HttpServletRequest httpRequest,
			AccessPoint wbContext) throws BadQueryException {

		if (isLocalRequest(httpRequest)) {
			// local means query: let the following RequestParsers have a go 
			// at it.
			return null;
		}

		WaybackRequest wbRequest = null;
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
		wbRequest.setRequestUrl(requestUrl);
		wbRequest.setReplayRequest();
		wbRequest.setResultsPerPage(getMaxRecords());
		return wbRequest;
	}
	public List<String> getLocalhostNames() {
		return localhostNames;
	}
	public void setLocalhostNames(List<String> localhostNames) {
		this.localhostNames = localhostNames;
	}

	/**
	 * @return the addDefaults
	 */
	public boolean isAddDefaults() {
		return addDefaults;
	}

	/**
	 * @param addDefaults the addDefaults to set
	 */
	public void setAddDefaults(boolean addDefaults) {
		this.addDefaults = addDefaults;
	}
}
