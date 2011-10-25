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
package org.archive.wayback.archivalurl;

import java.util.logging.Logger;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.webapp.AccessPoint;
import org.archive.wayback.webapp.AccessPointAware;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArchivalUrlResultURIConverter implements ResultURIConverter, 
	AccessPointAware {
	private static final Logger LOGGER =
		Logger.getLogger(ArchivalUrlResultURIConverter.class.getName());
	
	/**
	 * configuration name for URL prefix of replay server
	 */
	private String replayURIPrefix = null;
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		StringBuilder sb = null;

		if(replayURIPrefix == null) {
			sb = new StringBuilder(url.length() + datespec.length());
			sb.append(datespec);
			sb.append("/");
			sb.append(UrlOperations.stripDefaultPortFromUrl(url));
			return sb.toString();
		}
		if(url.startsWith(replayURIPrefix)) {
			return url;
		}
		sb = new StringBuilder(url.length() + datespec.length());
		sb.append(replayURIPrefix);
		sb.append(datespec);
		sb.append("/");
		sb.append(UrlOperations.stripDefaultPortFromUrl(url));
		return sb.toString();
	}

	/**
	 * @param replayURIPrefix the replayURIPrefix to set
	 */
	public void setReplayURIPrefix(String replayURIPrefix) {
		this.replayURIPrefix = replayURIPrefix;
	}

	/**
	 * @return the replayURIPrefix
	 */
	public String getReplayURIPrefix() {
		return replayURIPrefix;
	}

	public void setAccessPoint(AccessPoint accessPoint) {
		if(replayURIPrefix == null) {
			String apReplayPrefix = accessPoint.getReplayPrefix(); 
			LOGGER.warning("No replayURIPrefix configured - using accessPoint:"
					+ apReplayPrefix);
			replayURIPrefix = apReplayPrefix;
		} else {
			LOGGER.info("replayURIPrefix is configured: " + replayURIPrefix);
		}
	}
}
