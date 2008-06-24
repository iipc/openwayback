/* OracleAnnotationFilter
 *
 * $Id$
 *
 * Created on 5:06:29 PM Jun 10, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
package org.archive.wayback.resourceindex.filters;

import java.util.Date;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.accesscontrol.model.Rule;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.Timestamp;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResult filter class which contacts an access-control Oracle, using
 * information from the public comment field to annotate SearchResult objects.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class OracleAnnotationFilter implements ObjectFilter<SearchResult> {
	private AccessControlClient client = null;
	private String oracleUrl = null;
	private String who = null;
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(SearchResult o) {
		if(client != null) {
			String url = o.getAbsoluteUrl();
			Date capDate = Timestamp.parseAfter(o.getCaptureDate()).getDate();
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
