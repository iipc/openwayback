/* TimelineReplayResultURIConverter
 *
 * $Id$
 *
 * Created on 4:27:40 PM Apr 24, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
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
package org.archive.wayback.timeline;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.ConfigurationException;

/**
 * Exotic implementation of ResultURIConverter that is aware of 3 contexts
 * for Replay URIs: Frameset, Timeline, and Inline. In order to allow the rest
 * of the application to remain unaware of these various contexts, this
 * class allows extraction of adapters which contain the extra context for
 * each of these modes. The default mode is Frameset, meaning the base 
 * implementations use this context.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class TimelineReplayResultURIConverter implements ResultURIConverter {
	private final static String META_MODE_YES = "yes";
	
	private final static String REPLAY_URI_PREFIX_PROPERTY = "replayuriprefix";
	private final static String META_URI_PREFIX_PROPERTY = "metauriprefix";
	private final static String FRAMESET_URI_PREFIX_PROPERTY = "frameseturiprefix";
	private final static String TIMELINE_URI_PREFIX_PROPERTY = "timelineuriprefix";
	/**
	 * Url prefix of replay server
	 */
	private String replayUriPrefix;
	private String metaUriPrefix;
	private String framesetUriPrefix;
	private String timelineUriPrefix;

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		replayUriPrefix = (String) p.get( REPLAY_URI_PREFIX_PROPERTY);
		if (replayUriPrefix == null || replayUriPrefix.length() <= 0) {
			throw new ConfigurationException("Failed to find " + 
					REPLAY_URI_PREFIX_PROPERTY);
		}
		metaUriPrefix = (String) p.get( META_URI_PREFIX_PROPERTY);
		if (metaUriPrefix == null || metaUriPrefix.length() <= 0) {
			throw new ConfigurationException("Failed to find " + 
					META_URI_PREFIX_PROPERTY);
		}
		framesetUriPrefix = (String) p.get( FRAMESET_URI_PREFIX_PROPERTY);
		if (framesetUriPrefix == null || framesetUriPrefix.length() <= 0) {
			throw new ConfigurationException("Failed to find " + 
					FRAMESET_URI_PREFIX_PROPERTY);
		}
		timelineUriPrefix = (String) p.get( TIMELINE_URI_PREFIX_PROPERTY);
		if (timelineUriPrefix == null || timelineUriPrefix.length() <= 0) {
			throw new ConfigurationException("Failed to find " + 
					TIMELINE_URI_PREFIX_PROPERTY);
		}
	}

	// generic method which all other invocations boil down to.
	private String makeURI(String prefix, String dateStr, String url, 
			String resolution, String metaMode) {

		StringBuilder buf = new StringBuilder(100);
		buf.append(prefix);
		buf.append("?q=exactdate:").append(dateStr);
		if(resolution != null && resolution.length() > 0) {
			buf.append("%20").append(WaybackConstants.REQUEST_RESOLUTION);
			buf.append(":").append(resolution);
		}
		if(metaMode != null && (metaMode.length() > 0) && 
				metaMode.equals(META_MODE_YES)) {
			
			buf.append("%20").append(WaybackConstants.REQUEST_META_MODE);
			buf.append(":").append(META_MODE_YES);
		}
		buf.append("%20type:urlclosestquery%20url:").append(url);
		return buf.toString();
	}

	private String searchResultToUrl(SearchResult result) {
		String finalUrl = result.get(WaybackConstants.RESULT_URL); 
 		if(!finalUrl.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
			finalUrl = WaybackConstants.HTTP_URL_PREFIX + finalUrl;
		}
        try {
        	finalUrl = URLEncoder.encode(finalUrl,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// should not be able to happen -- with hard-coded UTF-8, anyways..
			e.printStackTrace();
		}
		return finalUrl;
	}
	private String searchResultToDateStr(SearchResult result) {
		return result.get(WaybackConstants.RESULT_CAPTURE_DATE); 		
	}
	private String resolveUrlToResult(SearchResult result, String url) {
		String finalUrl = url;
		try {
			if(!url.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
				UURI absResultURI = UURIFactory.getInstance(
						searchResultToUrl(result));
				UURI origURI = UURIFactory.getInstance(absResultURI, url);
				finalUrl = origURI.getEscapedURI();
			}
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return finalUrl;
	}
	protected String makeReplayURI(SearchResult result, String prefix, 
			String resolution, String metaMode ) {

		return makeURI(prefix,searchResultToDateStr(result),
				searchResultToUrl(result), resolution, metaMode);
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(org.archive.wayback.core.SearchResult)
	 */
	public String makeReplayURI(SearchResult result) {
		return makeReplayURI(result,framesetUriPrefix,null,null);
	}

	
	protected String makeRedirectReplayURI(SearchResult result, String url,
			String prefix, String resolution, String metaMode) {
		return makeURI(prefix,searchResultToDateStr(result),
				resolveUrlToResult(result,url),resolution,metaMode);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeRedirectReplayURI(org.archive.wayback.core.SearchResult, java.lang.String)
	 */
	public String makeRedirectReplayURI(SearchResult result, String url) {
		return makeRedirectReplayURI(result,url,framesetUriPrefix,null,null);
	}

	/**
	 * @param result
	 * @param url
	 * @param baseUrl
	 * @param prefix
	 * @param resolution
	 * @param metaMode
	 * @return String URI to replay URL
	 */
	public String makeRedirectReplayURI(SearchResult result, String url,
			String baseUrl,	String prefix, String resolution, String metaMode) {
		String finalUrl = baseUrl;
		try {
			if(!url.startsWith(WaybackConstants.HTTP_URL_PREFIX)) {
				UURI absResultURI = UURIFactory.getInstance(WaybackConstants.HTTP_URL_PREFIX + baseUrl);
				UURI origURI = UURIFactory.getInstance(absResultURI, url);
				finalUrl = origURI.getEscapedURI();
			}
		} catch (URIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return makeURI(prefix,searchResultToDateStr(result),finalUrl,
				resolution,metaMode);
	}
	/* (non-Javadoc)
	 * @see org.archive.wayback.ResultURIConverter#makeRedirectReplayURI(org.archive.wayback.core.SearchResult, java.lang.String, java.lang.String)
	 */
	public String makeRedirectReplayURI(SearchResult result, String url,
			String baseUrl) {
		return makeRedirectReplayURI(result,url,baseUrl,framesetUriPrefix,
				null,null);
	}
	
	/**
	 * @param prefix
	 * @param result
	 * @param resolution
	 * @param metaMode
	 * @return String URI
	 */
	public String getReplayUriPrefix(String prefix, SearchResult result, 
			String resolution, String metaMode) {
		return makeURI(prefix,searchResultToDateStr(result),"",
				resolution,metaMode);
	}
	public String getReplayUriPrefix(final SearchResult result) {
		return getReplayUriPrefix(framesetUriPrefix,result,null,null);
	}

	private ResultURIConverter getAdapter(WaybackRequest request,String prefix) {
		String resolution = request.get(WaybackConstants.REQUEST_RESOLUTION);
		String metaMode = request.get(WaybackConstants.REQUEST_META_MODE);
		return new ResultURIConverterAdapter(this,prefix,resolution,metaMode);
	}
	
	/**
	 * @param request
	 * @return adaptor with frameset context 
	 */
	public ResultURIConverter getFramesetAdapter(WaybackRequest request) {
		return getAdapter(request,framesetUriPrefix);
	}
	/**
	 * @param request
	 * @return adaptor with timeline context
	 */
	public ResultURIConverter getTimelineAdapter(WaybackRequest request) {
		return getAdapter(request,timelineUriPrefix);
	}
	/**
	 * @param request
	 * @return adaptor with inline context
	 */
	public ResultURIConverter getInlineAdapter(WaybackRequest request) {
		return getAdapter(request,replayUriPrefix);
	}
	/**
	 * @param request
	 * @return adaptor with meta context
	 */
	public ResultURIConverter getMetaAdapter(WaybackRequest request) {
		return getAdapter(request,metaUriPrefix);
	}

	protected class ResultURIConverterAdapter implements ResultURIConverter {
		
		private TimelineReplayResultURIConverter converter;
		private String prefix;
		private String resolution;
		private String metaMode;
		private ResultURIConverterAdapter(TimelineReplayResultURIConverter converter, 
				String prefix, String resolution, String metaMode) {
			this.converter = converter;
			this.prefix = prefix;
			this.resolution = resolution;
			this.metaMode = metaMode;
		}
		
		/* (non-Javadoc)
		 * @see org.archive.wayback.ResultURIConverter#makeReplayURI(org.archive.wayback.core.SearchResult)
		 */
		public String makeReplayURI(SearchResult result) {
			return converter.makeReplayURI(result,prefix,resolution,metaMode);
		}
		/* (non-Javadoc)
		 * @see org.archive.wayback.ResultURIConverter#makeRedirectReplayURI(org.archive.wayback.core.SearchResult, java.lang.String)
		 */
		public String makeRedirectReplayURI(SearchResult result, String url) {
			return converter.makeRedirectReplayURI(result,url,prefix,
					resolution,metaMode);
		}
		/* (non-Javadoc)
		 * @see org.archive.wayback.ResultURIConverter#makeRedirectReplayURI(org.archive.wayback.core.SearchResult, java.lang.String, java.lang.String)
		 */
		public String makeRedirectReplayURI(SearchResult result, String url,
				String baseUrl) {
			
			return converter.makeRedirectReplayURI(result, url, baseUrl, 
					prefix, resolution, metaMode);
		}
		/* (non-Javadoc)
		 * @see org.archive.wayback.ResultURIConverter#getReplayUriPrefix()
		 */
		public String getReplayUriPrefix(final SearchResult result) {
			return converter.getReplayUriPrefix(prefix,result,resolution,metaMode);
		}
		/* (non-Javadoc)
		 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
		 */
		public void init(Properties p) throws ConfigurationException {
			// no-operation: never created/initialized by a WaybackLogic
		}
		
		
	}	
}
