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
package org.archive.wayback.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.webapp.AccessPoint;
import org.archive.wayback.webapp.PerfStats;
import org.archive.wayback.webapp.PerfStats.PerfStatEntry;
import org.archive.wayback.webapp.PerfWritingHttpServletResponse;

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
 */
public class UIResults {
	public final static String FERRET_NAME = "ui-results";
	public static String UI_RESOURCE_BUNDLE_NAME = "WaybackUI";
	/**
	 * name of request scope attribute holding {@code UIResults} instance.
	 */
	public final static String UIRESULTS_ATTRIBUTE = "results";

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
	private PerfWritingHttpServletResponse perfResponse;
	private StringFormatter formatter;
	
	private final static String localHostName;
	
	static {
		String name;
		
		try {
			name = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        	name = "localhost";
        }
		
		localHostName = name;
	}
	
	/**
	 * Constructor for a "generic" UIResults, where little/no context is 
	 * available. Likely used for static UI requests, and more specifically, for
	 * template .jsp files, including header/footer .jsps. These may be called
	 * in multiple contexts, but don't expect much data to be avaialble beyond
	 * the AccessPoint that handled the request.
	 * @param wbRequest WaybackRequest with some or no information
	 * @param uriConverter the ResultURIConveter to use with the AccessPoint
	 * handling the request.
	 */
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
	}

	/**
	 * Constructor for a "exception" UIResults, where little/no context is 
	 * available. Likely used for exception rendering .jsp files.
	 * @param wbRequest WaybackRequest with some or no information, but at 
	 * least the AccessPoint that handled the request.
	 * @param uriConverter the ResultURIConveter to use with the AccessPoint
	 * handling the request.
	 * @param exception WaybackException to be rendered.
	 */
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter,
			WaybackException exception) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
		this.exception = exception;
	}
	/**
	 * Constructor for "Url Query" UIResults, where the request successfully 
	 * matched something from the index. Used to hand off search results and
	 * context to the query rendering .jsp files.
	 * @param wbRequest WaybackRequest with a valid request
	 * @param uriConverter the ResultURIConveter to use with the AccessPoint
	 * handling the request.
	 * @param captureResults CaptureSearchResults object with matching data.
	 */
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter,
			CaptureSearchResults captureResults) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
		this.captureResults = captureResults;
	}
	/**
	 * Constructor for "Url Prefix Query" UIResults, where the request 
	 * successfully matched something from the index. Used to hand off search 
	 * results and context to the query rendering .jsp files.
	 * @param wbRequest WaybackRequest with a valid request
	 * @param uriConverter the ResultURIConveter to use with the AccessPoint
	 * @param urlResults UrlSearchResults object with matching data.
	 */
	public UIResults(WaybackRequest wbRequest, ResultURIConverter uriConverter,
			UrlSearchResults urlResults) {
		this.wbRequest = wbRequest;
		this.uriConverter = uriConverter;
		this.urlResults = urlResults;
	}
	/**
	 * Constructor for "Replay" UIResults, where the request 
	 * successfully matched something from the index, the document was retrieved
	 * from the ResourceStore, and is going to be shown to the user.
	 * @param wbRequest WaybackRequest with some or no information
	 * @param uriConverter the ResultURIConveter to use with the AccessPoint
	 * @param captureResults CaptureSearchResults object with matching data.
	 * @param result the specific CaptureSearchResult being replayed
	 * @param resource the actual Resource being replayed
	 */
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

	/**
	 * @return the original URL as received by Wayback, before forwarding to
	 * a .jsp
	 */
	public String getOriginalRequestURL() {
		return originalRequestURL;
	}

	/*
	 * JSP CONVENIENCE METHODS:
	 */

	/**
	 * Create a self-referencing URL that will perform a query for all copies
	 * of the given URL.
	 * <p>This method builds URL that passes target URL in CGI parameter,
	 * along with other parameters unnecessary for making simple capture
	 * query request. It is not suitable for simple links.
	 * {@link #makePlainCaptureQueryUrl(String)} generates clean and
	 * plain URL.</p>
	 * @param url to search for copies of
	 * @return String url that will make a query for all captures of an URL.
	 */
	public String makeCaptureQueryUrl(String url) {
		WaybackRequest newWBR = wbRequest.clone();
		
		newWBR.setCaptureQueryRequest();
		newWBR.setRequestUrl(url);
		return newWBR.getAccessPoint().getQueryPrefix() + "query?" +
			newWBR.getQueryArguments(1);
	}

	/**
	 * Build a self-referencing URL that will perform a query for all copies
	 * of the given URL (plain, clean URL version).
	 * @param url URL to search for copies of
	 * @return String URL for querying captures of
	 * @version 1.8.1
	 */
	public String makePlainCaptureQueryUrl(String url) {
		// TOOD: want "2014*" instead of "20140101000000-20141231115959*"
		return wbRequest.getAccessPoint().makeCaptureQueryUrl(url,
			wbRequest.getStartTimestamp(), wbRequest.getEndTimestamp());
	}

	/**
	 * Get a String generic AccessPoint config (ala AccessPoint.configs Spring
	 * config)
	 * @param configName key for configuration property
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
	 * Create a replay URL for the given CaptureSearchResult
	 * @param result CaptureSearchResult to replay
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
	 * Create a self-referencing URL that will drive to the given page, 
	 * simplifying rendering pagination
	 * @param pageNum page number of results to link to.
	 * @return String URL which will drive browser to search results for a
	 *         different page of results for the same query
	 */
	public String urlForPage(int pageNum) {
		WaybackRequest wbRequest = getWbRequest();
		return wbRequest.getAccessPoint().getQueryPrefix() + "query?" +
			wbRequest.getQueryArguments(pageNum);
	}

	/**
	 * @return the defined staticPrefix for the AccessPoint%><%@ page import="org.archive.wayback.webapp.PerfWritingHttpServletResponse"
	 */
	public String getStaticPrefix() {
		if (wbRequest != null) {
			if (wbRequest.getAccessPoint() != null) {
				return wbRequest.getAccessPoint().getStaticPrefix();
			}
		}
		return "";
	}

	/**
	 * @return the defined queryPrefix for the AccessPoint
	 */
	public String getQueryPrefix() {
		if (wbRequest != null) {
			if (wbRequest.getAccessPoint() != null) {
				return wbRequest.getAccessPoint().getQueryPrefix();
			}
		}
		return "";
	}

	/**
	 * @return the defined replayPrefix for the AccessPoint
	 */
	public String getReplayPrefix() {
		if (wbRequest != null) {
			if (wbRequest.getAccessPoint() != null) {
				return wbRequest.getAccessPoint().getReplayPrefix();
			}
		}
		return "";
	}

	/*
	 * FORWARD TO A .JSP
	 */
	
//	/**
//	 * Store this UIResults in the HttpServletRequest argument.
//	 * @param httpRequest the HttpServletRequest to store this UIResults in.
//	 * @param contentJsp th 
//	 */
//	public void storeInRequest(HttpServletRequest httpRequest, 
//			String contentJsp) {
//		this.contentJsp = contentJsp;
//		this.originalRequestURL = httpRequest.getRequestURL().toString();
//		httpRequest.setAttribute(FERRET_NAME, this);
//	}

	/**
	 * Store this UIResults object in the given HttpServletRequest, then
	 * forward the request to target, in this case, an image, html file, .jsp,
	 * any file which can return a complete document. Specifically, this means
	 * that if target is a .jsp, it must render it's own header and footer.
	 * @param request the HttpServletRequest
	 * @param response the HttpServletResponse
	 * @param target the String path to the .jsp to handle drawing the data, 
	 * relative to the contextRoot (ex. "/WEB-INF/query/foo.jsp")
	 * @throws ServletException for usual reasons...
	 * @throws IOException for usual reasons...
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
	 * Extract a generic UIResults from the HttpServletRequest. Probably used
	 * by a header/footer template .jsp file.
	 * @param httpRequest the HttpServletRequest where the UIResults was 
	 * ferreted away
	 * @return generic UIResult with info from httpRequest applied.
	 */
	public static UIResults getGeneric(HttpServletRequest httpRequest) {
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if (results == null) {
			results = new UIResults(httpRequest);
		}
		return results;
	}

	/**
	 * construct bare minimum UIResults.
	 * @param httpRequest
	 */
	public UIResults(HttpServletRequest httpRequest) {
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.extractHttpRequestInfo(httpRequest);
		this.wbRequest = wbRequest;
		this.uriConverter = null;
	}

	/**
	 * constructor for JSP.
	 * <p>be sure to set required objects through setter.</p>
	 */
	public UIResults() {
	}

	/**
	 * initializes WaybackRequest from HttpServletRequest.
	 * <p>for rendering top page only.</p>
	 * @param request
	 */
	public void setRequest(HttpServletRequest request) {
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.extractHttpRequestInfo(request);
		this.wbRequest = wbRequest;
	}

	/**
	 * Extract an Exception UIResults from the HttpServletRequest. Probably used
	 * by a .jsp responsible for actual drawing errors for the user.private
	 * @param httpRequest the HttpServletRequest where the UIResults was 
	 * ferreted away
	 * @return Exception UIResult with info from httpRequest applied.
	 * @throws ServletException if expected information is not available. Likely
	 * means a programming bug, or a configuration problem.
	 */
	public static UIResults extractException(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if (results == null) {
			throw new ServletException("No attribute..");
		}
		if (results.exception == null) {
			throw new ServletException("No WaybackException..");
		}
		if (results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if (results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		return results;
	}
	/**
	 * Extract a CaptureQuery UIResults from the HttpServletRequest. Probably 
	 * used by a .jsp responsible for actually drawing search results for the 
	 * user.
	 * @param httpRequest the HttpServletRequest where the UIResults was 
	 * ferreted away
	 * @return CaptureQuery UIResult with info from httpRequest applied.
	 * @throws ServletException if expected information is not available. Likely
	 * means a programming bug, or a configuration problem.
	 */
	public static UIResults extractCaptureQuery(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if (results == null) {
			throw new ServletException("No attribute..");
		}
		if (results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if (results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		if (results.captureResults == null) {
			throw new ServletException("No CaptureSearchResults..");
		}
		return results;
	}
	/**
	 * Extract a UrlQuery UIResults from the HttpServletRequest. Probably 
	 * used by a .jsp responsible for actually drawing search results for the 
	 * user.
	 * @param httpRequest the HttpServletRequest where the UIResults was 
	 * ferreted away
	 * @return UrlQuery UIResult with info from httpRequest applied.
	 * @throws ServletException if expected information is not available. Likely
	 * means a programming bug, or a configuration problem.
	 */
	public static UIResults extractUrlQuery(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if (results == null) {
			throw new ServletException("No attribute..");
		}
		if (results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if (results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		if (results.urlResults == null) {
			throw new ServletException("No UrlSearchResults..");
		}
		return results;
	}
	/**
	 * Extract a Replay UIResults from the HttpServletRequest. Probably 
	 * used by a .jsp insert, responsible for rendering content into replayed 
	 * Resources to enhance the Replay experience.
	 * @param httpRequest the HttpServletRequest where the UIResults was 
	 * ferreted away
	 * @return Replay UIResult with info from httpRequest applied.
	 * @throws ServletException if expected information is not available. Likely
	 * means a programming bug, or a configuration problem.
	 */
	public static UIResults extractReplay(HttpServletRequest httpRequest)
		throws ServletException {
		
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if (results == null) {
			throw new ServletException("No attribute..");
		}
		if (results.wbRequest == null) {
			throw new ServletException("No WaybackRequest..");
		}
		if (results.uriConverter == null) {
			throw new ServletException("No ResultURIConverter..");
		}
		if (results.captureResults == null) {
			throw new ServletException("No CaptureSearchResults..");
		}
		if (results.result == null) {
			throw new ServletException("No CaptureSearchResult..");
		}
		if (results.resource == null) {
			throw new ServletException("No Resource..");
		}
		return results;
	}
	
	/**
	 * @return the uriConverter
	 * @deprecated use getURIConverter()
	 */
	public ResultURIConverter getUriConverter() {
		return uriConverter;
	}

	/*
	 * STATIC CONVENIENCE METHODS
	 */

	private static void replaceAll(StringBuffer s,
			final String o, final String n) {
		int olen = o.length();
		int nlen = n.length();
		int found = s.indexOf(o);
		while (found >= 0) {
			s.replace(found,found + olen,n);
			found = s.indexOf(o,found + nlen);
		}
	}
	
	/**
	 * return a string appropriate for inclusion as an XML tag
	 * @param tagName raw string to be encoded
	 * @return encoded tagName
	 * @deprecated use getFormatter().escapeHtml(String)
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
	 * @param content to escape
	 * @return encoded content
	 * @deprecated use getFormatter().escapeHtml(String)
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
	 * @param content to encode
	 * @return encoded content
	 * @deprecated use getFormatter().escapeHtml(String)
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
	 * @deprecated use getWbRequest().getContextPrefix()
	 */
	public String getContextPrefix() {
		return getWbRequest().getContextPrefix();
	}
	/**
	 * return {@code Locale} for request being processed.
	 * @return Locale, never {@code null}
	 * @see WaybackRequest#getLocale()
	 * @see AccessPoint#getLocale()
	 */
	public Locale getLocale() {
		Locale l = wbRequest.getLocale();
		if (l == null) {
			l = Locale.getAvailableLocales()[0];
		}
		return l;
	}
	/**
	 * return StringFormatter set-up for locale of request being
	 * processed.
	 * <p>deprecation recalled 2014-05-06.</p>
	 * @return StringFormatter localized to user request
	 */
	public StringFormatter getFormatter() {
		if (formatter == null) {
			ResourceBundle b = ResourceBundle.getBundle(UI_RESOURCE_BUNDLE_NAME);
			formatter = new StringFormatter(b, getLocale());
		}
		return formatter;
	}

	/**
	 * @return URL that points to the root of the Server
	 * @deprecated use getWbRequest().getServerPrefix()
	 */
	public String getServerPrefix() {
		return getWbRequest().getServerPrefix();
	}

	/**
	 * @param contentJsp the contentJsp to set
	 * @deprecated use forward()
	 */
	public void setContentJsp(String contentJsp) {
		this.contentJsp = contentJsp;
	}

	/**
	 * @param url to replay
	 * @param timestamp to replay
	 * @return String url that will replay the url at timestamp
	 * @deprecated use resultToReplayUrl(CaptureSearchResult) or 
	 * getURIConverter.makeReplayURI()
	 */
	public String makeReplayUrl(String url, String timestamp) {
		if(uriConverter == null) {
			return null;
		}
		return uriConverter.makeReplayURI(timestamp, url);
	}
	
	/**
	 * return hostname of this server.
	 * @return String
	 */
	public String getServerName() {
		return localHostName;
	}
	
	public static String getLocalHostName() {
		return localHostName;
	}
	
	/**
	 * return hostname portion of request URL.
	 * @return String
	 */
	public String getTargetSite() {
		try {
			String urlstr = wbRequest.getRequestUrl();
			if (urlstr == null) return null;
			URL url = new URL(urlstr);
			return url.getHost();
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 *
	 * @return local hostname.
	 * @deprecated 1.8.1, use {@link #getServerName()}.
	 *   this method is nothing more than that.
	 */
	public String enableAnalytics() {
		if (perfResponse != null) {
			perfResponse.enablePerfCookie();
		}
		
		return localHostName;
	}
	
	/**
	 * Total elapsed time for {@code Total}.
	 * @return
	 * @deprecated 1.8.1, use {@code PerfStats.getTotal("Total")}.
	 */
	public static long getTotalCount() {
		PerfStatEntry entry = PerfStats.get("Total");
		return ((entry != null) ? entry.getTotal() : 0);
	}

	/**
	 *
	 * @param perfResponse
	 * @deprecated 1.8.1, no replacement. this method has no real effect.
	 */
	public void setPerfResponse(PerfWritingHttpServletResponse perfResponse) {
		this.perfResponse = perfResponse;
    }
}
