/* OracleExclusionFilterFactory
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol.oracleclient;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.ObjectFilter;

/**
 * ExclusionFilterFactory implementation which connects to an Exclusion Oracle
 * via HTTP to determine which SearchResults can be exposed
 * @author brad
 *
 */
public class OracleExclusionFilterFactory implements ExclusionFilterFactory {

	private String oracleUrl = null;
	private String accessGroup = null;
	private String proxyHostPort = null;
	
	public ExclusionFilter get() {
		OracleExclusionFilter filter = new OracleExclusionFilter(oracleUrl,
				accessGroup, proxyHostPort);
		return filter;
	}

	public void shutdown() {
		// no-op... yet..
	}

	/**
	 * @return String URL where Oracle HTTP server is located
	 */
	public String getOracleUrl() {
		return oracleUrl;
	}

	/**
	 * @param oracleUrl String URL where Oracle HTTP server is located
	 */
	public void setOracleUrl(String oracleUrl) {
		this.oracleUrl = oracleUrl;
	}

	/**
	 * @return String group to use with requests to the Oracle
	 */
	public String getAccessGroup() {
		return accessGroup;
	}

	/**
	 * @param accessGroup String group to use with requests to the Oracle
	 */
	public void setAccessGroup(String accessGroup) {
		this.accessGroup = accessGroup;
	}

	/**
	 * @return the proxyHostPort
	 */
	public String getProxyHostPort() {
		return proxyHostPort;
	}

	/**
	 * @param proxyHostPort the proxyHostPort to set, ex. "localhost:3128"
	 */
	public void setProxyHostPort(String proxyHostPort) {
		this.proxyHostPort = proxyHostPort;
	}

}
