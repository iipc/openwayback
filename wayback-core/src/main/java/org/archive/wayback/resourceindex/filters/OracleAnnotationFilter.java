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
package org.archive.wayback.resourceindex.filters;

import java.util.Date;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.accesscontrol.model.Rule;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResult filter class which contacts an access-control Oracle, using
 * information from the public comment field to annotate SearchResult objects.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class OracleAnnotationFilter implements ObjectFilter<CaptureSearchResult> {
	private AccessControlClient client = null;
	private String oracleUrl = null;
	private String who = null;
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult o) {
		if(client != null) {
			String url = o.getOriginalUrl();
			Date capDate = o.getCaptureDate();
			try {
				Rule r = client.getRule(url, capDate, new Date(), who);
				if(r != null) {
					String publicComment = r.getPublicComment();
					o.put("ANOTATION", publicComment);
				}
			} catch (RuleOracleUnavailableException e) {
				e.printStackTrace();
				// should not happen: we forcibly disable robots retrievals 
			}
		}
		return FILTER_INCLUDE;
	}

	public AccessControlClient getClient() {
		return client;
	}
	public void setClient(AccessControlClient client) {
		client.setRobotLookupsEnabled(false);
		this.client = client;
	}

	public String getWho() {
		return who;
	}

	public void setWho(String who) {
		this.who = who;
	}

	public String getOracleUrl() {
		return oracleUrl;
	}

	public void setOracleUrl(String oracleUrl) {
		this.oracleUrl = oracleUrl;
		setClient(new AccessControlClient(oracleUrl));
	}

}
