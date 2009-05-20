/* UIResults
 *
 * $Id$
 *
 * Created on 4:05:51 PM Feb 1, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.core;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.webapp.AccessPoint;

/**
 * Simple class which acts as the go-between between Java request handling code
 * and .jsp files which actually draw various forms of results for end user
 * consumption. Designed to be flexible enough to handle forward various types
 * of data to the eventual .jsp files, and provides a handful of convenience
 * method to simplify .jsp code.
 * 
 * 5 main "forms" of this object:
 * 1) Generic: has WaybackRequest, uriConverter
 * 2) Exception: has WaybackRequest, uriConverter, WaybackException
 * 3) CaptureQuery: has WaybackRequest, uriConverter, CaptureSearchResults
 * 4) UrlQuery: has WaybackRequest, uriConverter, UrlSearchResults
 * 5) Replay: has WaybackRequest, uriConverter, CaptureSearchResult, 
 * 				CaptureSearchResults, Resource
 *
 * There are constructors to create each of these forms from the appropriate
 * component objects.
 * 
 * There is also a common method "forward()" which will store the UIResults
 * object into an HttpServletRequest, for later retrieval by .jsp files.
 *
 * There are static methods to extract each of these from an HttpServletRequest,
 * which will also verify that the appropriate internal objects are present.
 * These methods are intended to be used by the target .jsp files.
 * 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UIResults {
	private final static String FERRET_NAME = "ui-results";
	// usually present
	private WaybackRequest wbRequest;
	// usually present	
	private ResultURIConverter uriConverter;
	// target .jsp (or static file) we forwarded to
	private String contentJsp = null;
	// original URL that was received, prior to the forwarding 
	private String originalRequestURL = null;

	// present for CaptureQuery and Replay requests
	private CaptureSearchResults captureResults = null;
	// present for UrlQuery requests
	private UrlSearchResults urlResults = null;
	// Present for Replay requests, the "closest" result
	private CaptureSearchResult result = null;
	// Present for Replay requests, the actual Resource being replayed
	private Resource resource = null;
	// Present for... requests that resulted in an expected Exception.
	private WaybackException exception = null;
	
	public UIResults(WaybackRequest wbRequest,ResultURIConverter uriConverter) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
	}
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter,
			WaybackException exception) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
		this.exception = exception;
	}
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter,
			CaptureSearchResults captureResults) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
		this.captureResults = captureResults;
	}
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter,
			UrlSearchResults urlResults) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
		this.urlResults = urlResults;
	}
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter,
			CaptureSearchResults captureResults, CaptureSearchResult result,
			Resource resource) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
		this.captureResults = captureResults;
		this.result = result;
		this.resource = resource;
	}

	/*
	 * GENERAL GETTERS:
	 */
	/**
	 * @return the uriConverter
	 */
	public ResultURIConverter getUriConverter() {
		return uriConverter;
	}

	/**
	 * @return Returns the wbRequest.
	 */
	public WaybackRequest getWbRequest() {
		if(wbRequest == null) {
			wbRequest = new WaybackRequest();
		}
		return wbRequest;
	}
	/**
	 * @return the ResultURIConverter
	 */
	public ResultURIConverter getURIConverter() {
		return uriConverter;
	}
	/**
	 * @return the captureResults
	 */
	public CaptureSearchResults getCaptureResults() {
		return captureResults;
	}
	/**
	 * @return the urlResults
	 */
	public UrlSearchResults getUrlResults() {
		return urlResults;
	}
	/**
	 * @return the result
	 */
	public CaptureSearchResult getResult() {
		return result;
	}
	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}
	/**
	 * @return the exception
	 */
	public WaybackException getException() {
		return exception;
	}
	/**
	 * @return the contentJsp
	 */
	public String getContentJsp() {
		return contentJsp;
	}

	public String getOriginalRequestURL() {
		return originalRequestURL;
	}

	/*
	 * JSP CONVENIENCE METHODS:
	 */

	/**
	 * @param url
	 * @return String url that will make a query for all captures of an URL.
	 */
	public String makeCaptureQueryUrl(String url) {
		WaybackRequest newWBR = wbRequest.clone();
		
		newWBR.setCaptureQueryRequest();
		newWBR.setRequestUrl(url);
		return newWBR.getContextPrefix() + "query?" +
			newWBR.getQueryArguments(1);
	}
	/**
	 * @param configName
	 * @return String configuration for the context, if present, otherwise null
	 */
	public String getContextConfig(final String configName) {
		String configValue = null;
		AccessPoint context = getWbRequest().getAccessPoint();
		if(context != null) {
			Properties configs = context.getConfigs();
			if(configs != null) {
				configValue = configs.getProperty(configName);
			}
		}
		return configValue;
	}
	/**
	 * @param result
	 * @return URL string that will replay the specified Resource Result.
	 */
	public String resultToReplayUrl(CaptureSearchResult result) {
		if(uriConverter == null) {
			return null;
		}
		String url = result.getOriginalUrl();
		String captureDate = result.getCaptureTimestamp();
		return uriConverter.makeReplayURI(captureDate,url);
	}

	/**
	 * @param pageNum
	 * @return String URL which will drive browser to search results for a
	 *         different page of results for the same query
	 */
	public String urlForPage(int pageNum) {
		WaybackRequest wbRequest = getWbRequest();
		return wbRequest.getContextPrefix() + "query?" +
			wbRequest.getQueryArguments(pageNum);
	}

	/*
	 * FORWARD TO A .JSP
	 */
	
	/**
	 * Store this UIResults in the HttpServletRequest argument.
	 * @param httpRequest
	 * @param contentJsp 
	 */
	public void storeInRequest(HttpServletRequest httpRequest, 
			String contentJsp) {
		this.contentJsp = contentJsp;
		this.originalRequestURL = httpRequest.getRequestURL().toString();
		httpRequest.setAttribute(FERRET_NAME, this);
	}

	/**
	 * @param request
	 * @param response
	 * @param targt
	 * @throws ServletException
	 * @throws IOException
	 */
	public void forward(HttpServletRequest request,
			HttpServletResponse response, final String target)
			throws ServletException, IOException {

		this.contentJsp = target;
		this.originalRequestURL = request.getRequestURL().toString();
		request.setAttribute(FERRET_NAME, this);
		RequestDispatcher dispatcher = request.getRequestDispatcher(target);
		if(dispatcher == null) {
			throw new IOException("No dispatcher for " + target);
		}
		dispatcher.forward(request, response);
	}
	
	/*
	 * EXTRACT FROM HttpServletRequest
	 */
	/**
	 * @param httpRequest
	 * @return generic UIResult with info from httpRequest applied.
	 */
	public static UIResults getGeneric(HttpServletRequest httpRequest) {
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if(results == null) {
			WaybackRequest wbRequest = new WaybackRequest();
			wbRequest.fixup(httpRequest);
			results = new UIResults(wbRequest, null);
		}
		return results;
	}

	/**
	 * @param httpRequest
	 * @return UIResults from httpRequest 
	 * @throws ServletException 
	 */
	public static UIResults extractException(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if(results == null) {
			throw new ServletException("No attribute..");
		}
		if(results.exception == null) {
			throw new ServletException("No WaybackException..");
		}
		if(results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if(results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		return results;
	}
	/**
	 * @param httpRequest
	 * @return UIResults from httpRequest 
	 * @throws ServletException 
	 */
	public static UIResults extractCaptureQuery(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if(results == null) {
			throw new ServletException("No attribute..");
		}
		if(results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if(results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		if(results.captureResults == null) {
			throw new ServletException("No CaptureSearchResults..");
		}
		return results;
	}
	/**
	 * @param httpRequest
	 * @return UIResults from httpRequest 
	 * @throws ServletException 
	 */
	public static UIResults extractUrlQuery(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if(results == null) {
			throw new ServletException("No attribute..");
		}
		if(results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if(results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		if(results.urlResults == null) {
			throw new ServletException("No UrlSearchResults..");
		}
		return results;
	}
	/**
	 * @param httpRequest
	 * @return UIResults from httpRequest 
	 * @throws ServletException 
	 */
	public static UIResults extractReplay(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if(results == null) {
			throw new ServletException("No attribute..");
		}
		if(results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if(results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		if(results.captureResults == null) {
			throw new ServletException("No CaptureSearchResults..");
		}
		if(results.result == null) {
			throw new ServletException("No CaptureSearchResult..");
		}
		if(results.resource == null) {
			throw new ServletException("No Resource..");
		}
		return results;
	}
	


	/*
	 * STATIC CONVENIENCE METHODS
	 */
	
	
	private static void replaceAll(StringBuffer s,
			final String o, final String n) {
		int olen = o.length();
		int nlen = n.length();
		int found = s.indexOf(o);
		while(found >= 0) {
			s.replace(found,found + olen,n);
			found = s.indexOf(o,found + nlen);
		}
	}
	
	/**
	 * return a string appropriate for inclusion as an XML tag
	 * @param tagName
	 * @return encoded tagName
	 */
	public static String encodeXMLEntity(final String tagName) {
		StringBuffer encoded = new StringBuffer(tagName);
		//replaceAll(encoded,";","&semi;");
		replaceAll(encoded,"&","&amp;");
		replaceAll(encoded,"\"","&quot;");
		replaceAll(encoded,"'","&apos;");
		replaceAll(encoded,"<","&lt;");
		replaceAll(encoded,">","&gt;");
		return encoded.toString();
	}
	
	/**
	 * return a string appropriate for inclusion as an XML tag
	 * @param content
	 * @return encoded content
	 */
	public static String encodeXMLContent(final String content) {
		StringBuffer encoded = new StringBuffer(content);
		
		replaceAll(encoded,"&","&amp;");
		replaceAll(encoded,"\"","&quot;");
		replaceAll(encoded,"'","&apos;");
		replaceAll(encoded,"<","&lt;");
		replaceAll(encoded,">","&gt;");
		
		return encoded.toString();
	}

	/**
	 * return a string appropriate for inclusion as an XML tag
	 * @param content
	 * @return encoded content
	 */
	public static String encodeXMLEntityQuote(final String content) {
		StringBuffer encoded = new StringBuffer(content);
		replaceAll(encoded,"amp","&#38;#38;");
		replaceAll(encoded,"apos","&#39;");
		replaceAll(encoded,"<","&#38;#60;");
		replaceAll(encoded,"gt","&#62;");
		replaceAll(encoded,"quot","&#34;");
		return encoded.toString();
	}	
	/*
	 * DEPRECATED
	 */
	
	/**
	 * @return URL that points to the root of the current WaybackContext
	 * @deprecated
	 */
	public String getContextPrefix() {
		return getWbRequest().getContextPrefix();
	}
	
	/**
	 * @return StringFormatter localized to user request
	 * @deprecated
	 */
	public StringFormatter getFormatter() {
		return getWbRequest().getFormatter();
	}

	/**
	 * @return URL that points to the root of the Server
	 * @deprecated
	 */
	public String getServerPrefix() {
		return getWbRequest().getServerPrefix();
	}

	/**
	 * @param contentJsp the contentJsp to set
	 * @deprecated
	 */
	public void setContentJsp(String contentJsp) {
		this.contentJsp = contentJsp;
	}

	/**
	 * @param url
	 * @param timestamp
	 * @return String url that will replay the url at timestamp
	 * @deprecated
	 */
	public String makeReplayUrl(String url, String timestamp) {
		if(uriConverter == null) {
			return null;
		}
		return uriConverter.makeReplayURI(timestamp, url);
	}
}
