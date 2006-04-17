/* ResultURIConverter
 *
 * $Id$
 *
 * Created on 5:24:36 PM Nov 1, 2005.
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
package org.archive.wayback.archivalurl;

import java.util.Properties;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.ReplayResultURIConverter;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.exception.ConfigurationException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResultURIConverter implements ReplayResultURIConverter {
	/**
	 * configuration name for URL prefix of replay server
	 */
	private final static String REPLAY_URI_PREFIX_PROPERTY = "replayuriprefix";
	/**
	 * Url prefix of replay server
	 */
	private String replayUriPrefix;
	/* (non-Javadoc)
	 * @see org.archive.wayback.ReplayResultURIConverter#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		// TODO Auto-generated method stub
		replayUriPrefix = (String) p.get( REPLAY_URI_PREFIX_PROPERTY);
		if (replayUriPrefix == null || replayUriPrefix.length() <= 0) {
			throw new ConfigurationException("Failed to find " + 
					REPLAY_URI_PREFIX_PROPERTY);
		}
		if(!replayUriPrefix.endsWith("/")) {
			replayUriPrefix += "/";
		}
	}


	public String makeReplayURI(SearchResult result) {
		return replayUriPrefix
				+ result.get(WaybackConstants.RESULT_CAPTURE_DATE) + "/" +
				result.get(WaybackConstants.RESULT_URL);
	}

	/**
	 * @return Returns the replayUriPrefix.
	 */
	public String getReplayUriPrefix() {
		return replayUriPrefix;
	}

	public String makeRedirectReplayURI(SearchResult result, String url) {
		String finalUrl = url;
		try {
			if(!url.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
				String resultUrl = result.get(WaybackConstants.RESULT_URL);
				UURI absResultURI = UURIFactory.getInstance(
						WaybackConstants.HTTP_URL_PREFIX  + resultUrl );
				UURI origURI = UURIFactory.getInstance(absResultURI, url);
				finalUrl = origURI.getEscapedURI();
			}
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 
		return replayUriPrefix
			+ result.get(WaybackConstants.RESULT_CAPTURE_DATE) + "/" + finalUrl;
	}
}
