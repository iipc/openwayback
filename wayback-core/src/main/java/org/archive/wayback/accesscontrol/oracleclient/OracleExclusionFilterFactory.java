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
package org.archive.wayback.accesscontrol.oracleclient;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

/**
 * ExclusionFilterFactory for {@link OracleExclusionFilter}.
 * @author brad
 * @see OracleExclusionFilter
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
