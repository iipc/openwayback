/* DomainPrefixResultURIConverter
 *
 * $Id$
 *
 * Created on 10:20:35 AM Aug 10, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.domainprefix;

import java.net.URI;
import java.net.URISyntaxException;

import org.archive.wayback.ResultURIConverter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class DomainPrefixResultURIConverter implements ResultURIConverter {

	private String hostPort = "localhost:8081";
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(java.lang.String, java.lang.String)
	 */
	public String makeReplayURI(String datespec, String url) {
		String replayURI = "";
		try {
			URI uri = new URI(url);
			StringBuilder sb = new StringBuilder(90);
			sb.append("http://");
			sb.append(datespec).append(".");
			sb.append(uri.getHost()).append(".");
			sb.append(hostPort);
			sb.append(uri.getPath());
			String query = uri.getQuery();
			if(query != null && query.length() > 0) {
				sb.append("?").append(query);
			}
			replayURI = sb.toString();

		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return replayURI;
	}

	/**
	 * @return the hostPort
	 */
	public String getHostPort() {
		return hostPort;
	}

	/**
	 * @param hostPort the hostPort to set
	 */
	public void setHostPort(String hostPort) {
		this.hostPort = hostPort;
	}

}
