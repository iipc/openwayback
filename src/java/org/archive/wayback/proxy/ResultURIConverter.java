/* ProxyResultURIConverter
 *
 * $Id$
 *
 * Created on 4:19:21 PM Nov 15, 2005.
 *
 * Copyright (C) 2005 Internet Archive.
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

import java.util.Properties;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ConfigurationException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResultURIConverter implements ReplayResultURIConverter {
	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayResultURIConverter#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayResultURIConverter#makeReplayURI(org.archive.wayback.core.ResourceResult)
	 */

	public String makeReplayURI(SearchResult result) {
		String finalUrl = result.get(WaybackConstants.RESULT_URL); 
		if(!finalUrl.startsWith("http://")) {
			finalUrl = "http://" + finalUrl;
		}
		return finalUrl;
	}

	/**
	 * @return Returns the replayUriPrefix.
	 */
	public String getReplayUriPrefix() {
		return "";
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayResultURIConverter#makeRedirectReplayURI(org.archive.wayback.core.SearchResult, java.lang.String)
	 */
	public String makeRedirectReplayURI(SearchResult result, String url) {
		String finalUrl = url;
		try {
			
			UURI origURI = UURIFactory.getInstance(url);
			if(!origURI.isAbsoluteURI()) {
				String resultUrl = result.get(WaybackConstants.RESULT_URL);
				UURI absResultURI = UURIFactory.getInstance(resultUrl);
				UURI finalURI = absResultURI.resolve(url);
				finalUrl = finalURI.getEscapedURI();
			}
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!finalUrl.startsWith("http://")) {
			finalUrl = "http://" + finalUrl;
		}
		return finalUrl;
	}
}
