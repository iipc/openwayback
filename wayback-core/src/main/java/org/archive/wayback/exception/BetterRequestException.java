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
package org.archive.wayback.exception;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.memento.MementoUtils;

/**
 * Exception class for queries which can be better expressed as another URL, or
 * should, for one reason or another, be requested at a different URL. Likely
 * cause would be to redirect the client so the Browser location reflects the
 * exact request being served.
 * <p>TODO: define a sub-class specific to timestamp-redirect for ArchivalUrl
 * scheme for cleaner implementation of Memento header support.</p>
 * @author brad
 */
public class BetterRequestException extends WaybackException {

	private static final long serialVersionUID = 1L;
	protected static final String ID = "betterRequest";
	private String betterURI;
	private int status = HttpServletResponse.SC_FOUND;
	Map<String, String> extraHeaders;

	/**
	 * Constructor
	 * @param betterURI
	 * @param status
	 * 
	 */
	public BetterRequestException(String betterURI, int status) {
		super("Better URI for query");
		this.betterURI = betterURI;
		this.status = status;
		id = ID;
		extraHeaders = new HashMap<String, String>();
	}

	/**
	 * Constructor
	 * @param betterURI
	 * 
	 */
	public BetterRequestException(String betterURI) {
		super("Better URI for query");
		this.betterURI = betterURI;
		id = ID;
		extraHeaders = new HashMap<String, String>();
	}

	/**
	 * @param name
	 * @param value
	 * @deprecated 1.8.1 2014-09-09, no replacement
	 * (define sub-class and override {@link #generateResponse(HttpServletResponse, WaybackRequest)})
	 */
	public void addHeader(String name, String value) {
		extraHeaders.put(name, value);
	}

	/**
	 * @param name
	 * @return
	 * @deprecated 1.8.1 2014-09-09, no replacement
	 * (define sub-class and overide {@link #generateResponse(HttpServletResponse, WaybackRequest)})
	 */
	public boolean hasHeader(String name) {
		return extraHeaders.containsKey(name);
	}

	/**
	 * @return Returns the betterURI.
	 */
	public String getBetterURI() {
		return betterURI;
	}

	public int getStatus() {
		return status;
	}

	public void generateResponse(HttpServletResponse response,
			WaybackRequest wbRequest) {
		response.setStatus(status);

		String redirectURI = betterURI;

		if ((wbRequest != null) && betterURI.startsWith("/") &&
				wbRequest.hasMementoAcceptDatetime()) {
			redirectURI = MementoUtils.getMementoPrefix(wbRequest
				.getAccessPoint()) + betterURI;
		}

		response.setHeader("Location", redirectURI);

		if (extraHeaders.size() > 0) {
			for (Map.Entry<String, String> entry : extraHeaders.entrySet()) {
				response.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}

}
