/* ReplayURI
 *
 * $Id$
 *
 * Created on 5:20:43 PM Nov 1, 2005.
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
package org.archive.wayback;

import java.util.Properties;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public abstract class ResultURIConverter implements PropertyConfigurable {
	protected WaybackRequest wbRequest;
	private Properties config = null;
	public void init(Properties p) throws ConfigurationException {
		config = p;
	}
	/**
	 * @param wbRequest The wbRequest to set.
	 */
	public void setWbRequest(WaybackRequest wbRequest) {
		this.wbRequest = wbRequest;
	}
	protected String getConfigOrContextRelative(String configName, String rel) {
		String configValue = config.getProperty(configName);
		if(configValue != null) {
			return configValue;
		}
		return wbRequest.getDefaultWaybackPrefix() + rel;
	}
	private boolean isAbsolute(String url) {
		return url.startsWith(WaybackConstants.HTTP_URL_PREFIX);
	}
	protected String resolveUrl(String url, String baseUrl) {
		if(!isAbsolute(baseUrl)) {
			baseUrl = WaybackConstants.HTTP_URL_PREFIX + baseUrl;
		}
		UURI absBaseURI;
		UURI resolvedURI = null;
		try {
			absBaseURI = UURIFactory.getInstance(baseUrl);
			resolvedURI = UURIFactory.getInstance(absBaseURI, url);
		} catch (URIException e) {
			e.printStackTrace();
			return url;
		}
		return resolvedURI.getEscapedURI();
	}
	protected String searchResultToDateStr(SearchResult result) {
		return result.get(WaybackConstants.RESULT_CAPTURE_DATE); 		
	}
	protected String searchResultToUrl(SearchResult result) {
		return result.get(WaybackConstants.RESULT_URL);
	}
	protected String searchResultToAbsoluteUrl(SearchResult result) {
		String abs = searchResultToUrl(result);
		if(abs.startsWith(WaybackConstants.HTTP_URL_PREFIX)) return abs;
		return WaybackConstants.HTTP_URL_PREFIX + abs;
	}


	/**
	 * return an absolute URL that will replay URL url at time datespec.
	 * 
	 * @param datespec
	 * @param url
	 * @return absolute replay URL
	 */
	public abstract String makeReplayURI(final String datespec,final String url);
	
	/** Convert a SearchResult into a replayable URL
	 * 
	 * @param result
	 * @return user-viewable String URL that will replay the ResourceResult
	 */
	public String makeReplayURI(final SearchResult result) {
		return makeReplayURI(searchResultToDateStr(result),
				searchResultToAbsoluteUrl(result));
	}
	/** create a URL that will drive the client back to the URL argument in 
	 * replay mode, possibly resolving the URL against this SearchResult
	 * @param result
	 * @param url
	 * @return String URL to send client to replay this url
	 */
	public String makeRedirectReplayURI(final SearchResult result, String url) {
		String finalUrl = resolveUrl(searchResultToUrl(result),url);
		return makeReplayURI(searchResultToDateStr(result),finalUrl);
	}

	/** create a URL that will drive the client back to the URL argument in 
	 * replay mode, resolving the URL against baseUrl
	 * @param result
	 * @param url
	 * @param baseUrl 
	 * @return String URL to send client to replay this url
	 */
	public String makeRedirectReplayURI(final SearchResult result, String url, 
			String baseUrl) {
		return makeReplayURI(searchResultToDateStr(result),
				resolveUrl(url,baseUrl));
	}
	/**
	 * @param result 
	 * @return the URL prefix for the replay service, which when another URL
	 * is appended, will drive back to the replay service
	 */
	public String getReplayUriPrefix(final SearchResult result) {
		return makeReplayURI(searchResultToDateStr(result),"");
	}
}
