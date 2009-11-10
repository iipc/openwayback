/* AccessPoint
 *
 * $Id$
 *
 * Created on 5:37:31 PM Apr 20, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-webapp.
 *
 * wayback-webapp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-webapp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-webapp; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.archive.wayback.ExceptionRenderer;
import org.archive.wayback.QueryRenderer;
import org.archive.wayback.ReplayDispatcher;
import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.RequestParser;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.UIResults;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AuthenticationControlException;
import org.archive.wayback.exception.BaseExceptionRenderer;
import org.archive.wayback.exception.BetterRequestException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.util.operator.BooleanOperator;
import org.springframework.beans.factory.BeanNameAware;

/**
 * Retains all information about a particular Wayback configuration
 * within a ServletContext, including holding references to the
 * implementation instances of the primary Wayback classes:
 * 
 * 		RequestParser
 *		ResourceIndex(via WaybackCollection)
 *		ResourceStore(via WaybackCollection)
 *		QueryRenderer
 *		ReplayDispatcher
 *		ExceptionRenderer
 *		ResultURIConverter
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class AccessPoint implements RequestContext, BeanNameAware {

	private static final Logger LOGGER = Logger.getLogger(
			AccessPoint.class.getName());
	
	private boolean useServerName = false;
	private boolean useAnchorWindow = false;
	private boolean exactSchemeMatch = true;
	private boolean exactHostMatch = false;

	private int contextPort = 0;
	private String contextName = null;
	private String beanName = null;
	private WaybackCollection collection = null;
	private ReplayDispatcher replay = null;
	private ExceptionRenderer exception = new BaseExceptionRenderer();
	private QueryRenderer query = null;
	private RequestParser parser = null;
	private ResultURIConverter uriConverter = null;
	private Properties configs = null;
	private ExclusionFilterFactory exclusionFactory = null;
	private BooleanOperator<WaybackRequest> authentication = null;
	private String urlRoot = null;
	private Locale locale = null;
	private List<String> filePatterns = null;
	private List<String> filePrefixes = null;
	
	/**
	 * @return List of file patterns that will be matched when querying the 
	 * ResourceIndex
	 */
	public List<String> getFilePatterns() {
		return filePatterns;
	}

	/**
	 * @param filePatterns List of file Patterns (regular expressions) that
	 * 		will be matched when querying the ResourceIndex - only SearchResults
	 *      matching one of these patterns will be returned.
	 */
	public void setFilePatterns(List<String> filePatterns) {
		this.filePatterns = filePatterns;
	}

	/**
	 * @return List of file String prefixes that will be matched when querying 
	 * 		the ResourceIndex
	 */
	public List<String> getFilePrefixes() {
		return filePrefixes;
	}

	/**
	 * @param filePrefixes List of String file prefixes that will be matched
	 * 		when querying the ResourceIndex - only SearchResults from files 
	 * 		with a prefix matching one of those in this List will be returned.
	 */
	public void setFilePrefixes(List<String> filePrefixes) {
		this.filePrefixes = filePrefixes;
	}

	/**
	 * @return the contextName
	 */
	public String getContextName() {
		return contextName;
	}

	/**
	 * @return the replay
	 */
	public ReplayDispatcher getReplay() {
		return replay;
	}

	/**
	 * @return the query
	 */
	public QueryRenderer getQuery() {
		return query;
	}

	/**
	 * @return the parser
	 */
	public RequestParser getParser() {
		return parser;
	}

	/**
	 * @return the uriConverter
	 */
	public ResultURIConverter getUriConverter() {
		return uriConverter;
	}

	/**
	 * @return explicit Locale to use within this AccessPoint.
	 */
	public Locale getLocale() {
		return locale;
	}

	/**
	 * @param locale explicit Locale to use for requests within this 
	 * 		AccessPoint. If not set, will attempt to use the one specified by
	 * 		each requests User Agent via HTTP headers
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	/**
	 * 
	 */
	public AccessPoint() {
		
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	public void setBeanName(String beanName) {
		this.beanName = beanName;
		this.contextName = "";
		int idx = beanName.indexOf(":");
		if(idx > -1) {
			contextPort = Integer.valueOf(beanName.substring(0,idx));
			contextName = beanName.substring(idx + 1);
		} else {
			try {
				this.contextPort = Integer.valueOf(beanName);
			} catch(NumberFormatException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @return the name of the bean in the Spring configuration which defined
	 * 			this AccessPoint.
	 */
	public String getBeanName() {
		return beanName;
	}
	/**
	 * @param httpRequest HttpServletRequest which is being handled 
	 * @return the prefix of paths received by this server that are handled by
	 * this WaybackContext, including the trailing '/'
	 */
	public String getContextPath(HttpServletRequest httpRequest) {
		String httpContextPath = httpRequest.getContextPath();
		if(contextName.length() == 0) {
			return httpContextPath + "/";
		}
		return httpContextPath + "/" + contextName + "/";
	}

	/**
	 * Remove any leading ServletContext and AccessPoint name path elements
	 * from the incoming request path, returning the result as a String
	 * 
	 * @param httpRequest HttpServletRequest which is being handled 
	 * @param includeQuery if true, include any query arguments
	 * @return the portion of the request following the path to this context
	 * without leading '/'
	 */
	private String translateRequest(HttpServletRequest httpRequest, 
			boolean includeQuery) {

		String origRequestPath = httpRequest.getRequestURI();
		if(includeQuery) {
			String queryString = httpRequest.getQueryString();
			if (queryString != null) {
				origRequestPath += "?" + queryString;
			}
		}
		String contextPath = getContextPath(httpRequest);
		if (!origRequestPath.startsWith(contextPath)) {
			if(contextPath.startsWith(origRequestPath)) {
				// missing trailing '/', just omit:
				return "";
			}
			return null;
		}
		return origRequestPath.substring(contextPath.length());
	}
	
	/**
	 * Remove any leading ServletContext and AccessPoint name path elements
	 * from the incoming request path, returning the result as a String

	 * @param httpRequest HttpServletRequest which is being handled 
	 * @return the portion of the request following the path to this context, 
	 * including any query information,without leading '/'
	 */
	public String translateRequestPathQuery(HttpServletRequest httpRequest) {
		return translateRequest(httpRequest,true);
	}	

	/**
	 * @param httpRequest HttpServletRequest which is being handled 
	 * @return the portion of the request following the path to this context, 
	 * excluding any query information, without leading '/'
	 */
	public String translateRequestPath(HttpServletRequest httpRequest) {
		return translateRequest(httpRequest,false);
	}	
	
	/**
	 * Construct an absolute URL that points to the root of the context that
	 * received the request, including a trailing "/".
	 * 
	 * @return String absolute URL pointing to the Context root where the
	 *         request was received.
	 */
	private String getAbsoluteContextPrefix(HttpServletRequest httpRequest, 
			boolean useRequestServer) {
		
		StringBuilder prefix = new StringBuilder();
		prefix.append(WaybackConstants.HTTP_URL_PREFIX);
		String waybackPort = null;
		if(useRequestServer) {
			prefix.append(httpRequest.getLocalName());
			waybackPort = String.valueOf(httpRequest.getLocalPort());
		} else {
			prefix.append(httpRequest.getServerName());
			waybackPort = String.valueOf(httpRequest.getServerPort());
		}
		if (!waybackPort.equals(WaybackConstants.HTTP_DEFAULT_PORT)) {
			prefix.append(":").append(waybackPort);
		}
		String contextPath = getContextPath(httpRequest);
		prefix.append(contextPath);
		return prefix.toString();
	}
	
	/**
	 * @param httpRequest HttpServletRequest which is being handled 
	 * @return absolute URL pointing to the base of this WaybackContext, using
	 * Server and port information from the HttpServletRequest argument.
	 */
	public String getAbsoluteServerPrefix(HttpServletRequest httpRequest) {
		return getAbsoluteContextPrefix(httpRequest, true);
	}

	/**
	 * @param httpRequest HttpServletRequest which is being handled 
	 * @return absolute URL pointing to the base of this WaybackContext, using
	 * Canonical server and port information.
	 */
	public String getAbsoluteLocalPrefix(HttpServletRequest httpRequest) {
		if(urlRoot != null) {
			return urlRoot;
		}
		return getAbsoluteContextPrefix(httpRequest, useServerName);
	}

	private boolean dispatchLocal(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) 
	throws ServletException, IOException {
		
		String translated = "/" + translateRequestPathQuery(httpRequest);

		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.setContextPrefix(getAbsoluteLocalPrefix(httpRequest));
		wbRequest.setAccessPoint(this);
		wbRequest.fixup(httpRequest);
		UIResults uiResults = new UIResults(wbRequest,uriConverter);
		try {
			uiResults.forward(httpRequest, httpResponse, translated);
			return true;
		} catch(IOException e) {
			// TODO: figure out if we got IO because of a missing dispatcher
		}
		return false;
	}
	
	/**
	 * @param httpRequest HttpServletRequest which is being handled 
	 * @param httpResponse HttpServletResponse which is being handled 
	 * @return true if the request was actually handled
	 * @throws ServletException per usual
	 * @throws IOException per usual
	 */
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) 
	throws ServletException, IOException {

		WaybackRequest wbRequest = null;
		boolean handled = false;

		try {
			wbRequest = parser.parse(httpRequest, this);

			if(wbRequest != null) {
				handled = true;

				// TODO: refactor this code into RequestParser implementations
				wbRequest.setAccessPoint(this);
				wbRequest.setContextPrefix(getAbsoluteLocalPrefix(httpRequest));
				wbRequest.fixup(httpRequest);
				// end of refactor

				if(authentication != null) {
					if(!authentication.isTrue(wbRequest)) {
						throw new AuthenticationControlException("Not authorized");
					}
				}

				if(exclusionFactory != null) {
					wbRequest.setExclusionFilter(exclusionFactory.get());
				}
				// TODO: refactor this into RequestParser implementations, so a
				// user could alter requests to change the behavior within a
				// single AccessPoint. For now, this is a simple way to expose
				// the feature to configuration.
				wbRequest.setExactScheme(exactSchemeMatch);

				if(wbRequest.isReplayRequest()) {

					handleReplay(wbRequest,httpRequest,httpResponse);
					
				} else {

					wbRequest.setExactHost(exactHostMatch);
					handleQuery(wbRequest,httpRequest,httpResponse);
				}
			} else {
				handled = dispatchLocal(httpRequest,httpResponse);
			}

		} catch(BetterRequestException e) {
			httpResponse.sendRedirect(e.getBetterURI());
			handled = true;

		} catch(WaybackException e) {
			logNotInArchive(e,wbRequest);
			exception.renderException(httpRequest, httpResponse, wbRequest, e, 
					uriConverter);
		}

		return handled;
	}

	private void handleReplay(WaybackRequest wbRequest, 
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) 
	throws IOException, ServletException, WaybackException {
		Resource resource = null;
		try {
			SearchResults results = collection.getResourceIndex().query(wbRequest);
			if(!(results instanceof CaptureSearchResults)) {
				throw new ResourceNotAvailableException("Bad results...");
			}
			CaptureSearchResults captureResults = (CaptureSearchResults) results;
	
			// TODO: check which versions are actually accessible right now?
			CaptureSearchResult closest = captureResults.getClosest(wbRequest, 
					useAnchorWindow);
			resource = collection.getResourceStore().retrieveResource(closest);
			ReplayRenderer renderer = replay.getRenderer(wbRequest, closest, resource);
			renderer.renderResource(httpRequest, httpResponse, wbRequest,
					closest, resource, uriConverter, captureResults);
		} finally {
			if(resource != null) {
				resource.close();
			}
		}
	}

	private void handleQuery(WaybackRequest wbRequest, 
			HttpServletRequest httpRequest, HttpServletResponse httpResponse) 
	throws ServletException, IOException, WaybackException {

		SearchResults results = collection.getResourceIndex().query(wbRequest);
		if(results instanceof CaptureSearchResults) {
			CaptureSearchResults cResults = (CaptureSearchResults) results;
			cResults.markClosest(wbRequest);
			query.renderCaptureResults(httpRequest,httpResponse,wbRequest,
					cResults,uriConverter);

		} else if(results instanceof UrlSearchResults) {
			UrlSearchResults uResults = (UrlSearchResults) results;
			query.renderUrlResults(httpRequest,httpResponse,wbRequest,
					uResults,uriConverter);
		} else {
			throw new WaybackException("Unknown index format");
		}
	}
	
	/**
	 * Release any resources associated with this AccessPoint, including
	 * stopping any background processing threads
	 * 
	 * @throws IOException per usual
	 */
	public void shutdown() throws IOException {
		if(collection != null) {
			collection.shutdown();
		}
	}
	
	private void logNotInArchive(WaybackException e, WaybackRequest r) {
		// TODO: move this into ResourceNotInArchiveException constructor
		if(e instanceof ResourceNotInArchiveException) {
			String url = r.getRequestUrl();
			StringBuilder sb = new StringBuilder(100);
			sb.append("NotInArchive\t");
			sb.append(contextName).append("\t");
			sb.append(contextPort).append("\t");
			sb.append(url);
			
			LOGGER.info(sb.toString());
		}
	}

	/**
	 * @param contextPort the contextPort to set
	 */
	public void setContextPort(int contextPort) {
		this.contextPort = contextPort;
	}

	/**
	 * @param contextName the contextName to set
	 */
	public void setContextName(String contextName) {
		this.contextName = contextName;
	}

	/**
	 * @param replay the replay to set
	 */
	public void setReplay(ReplayDispatcher replay) {
		this.replay = replay;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(QueryRenderer query) {
		this.query = query;
	}

	/**
	 * @param parser the parser to set
	 */
	public void setParser(RequestParser parser) {
		this.parser = parser;
	}

	/**
	 * @param uriConverter the uriConverter to set
	 */
	public void setUriConverter(ResultURIConverter uriConverter) {
		this.uriConverter = uriConverter;
	}


	/**
	 * @return the contextPort
	 */
	public int getContextPort() {
		return contextPort;
	}

	/**
	 * @return the configs
	 */
	public Properties getConfigs() {
		return configs;
	}

	/**
	 * @param configs the configs to set
	 */
	public void setConfigs(Properties configs) {
		this.configs = configs;
	}

	/**
	 * @return the useServerName
	 */
	public boolean isUseServerName() {
		return useServerName;
	}

	/**
	 * @param useServerName the useServerName to set
	 */
	public void setUseServerName(boolean useServerName) {
		this.useServerName = useServerName;
	}

	/**
	 * @return the useAnchorWindow
	 */
	public boolean isUseAnchorWindow() {
		return useAnchorWindow;
	}

	/**
	 * @param useAnchorWindow the useAnchorWindow to set
	 */
	public void setUseAnchorWindow(boolean useAnchorWindow) {
		this.useAnchorWindow = useAnchorWindow;
	}
	
	/**
	 * @return the exactSchemeMatch
	 */
	public boolean isExactSchemeMatch() {
		return exactSchemeMatch;
	}

	/**
	 * @param exactSchemeMatch the exactSchemeMatch to set
	 */
	public void setExactSchemeMatch(boolean exactSchemeMatch) {
		this.exactSchemeMatch = exactSchemeMatch;
	}

	/**
	 * @return the ExclusionFilterFactory in use with this AccessPoint
	 */
	public ExclusionFilterFactory getExclusionFactory() {
		return exclusionFactory;
	}

	/**
	 * @param exclusionFactory all requests to this AccessPoint will create an
	 * 		exclusionFilter from this factory when handling requests
	 */
	public void setExclusionFactory(ExclusionFilterFactory exclusionFactory) {
		this.exclusionFactory = exclusionFactory;
	}

	/**
	 * @return the configured AuthenticationControl operator in use with this
	 * 		AccessPoint.
	 */
	public BooleanOperator<WaybackRequest> getAuthentication() {
		return authentication;
	}

	/**
	 * @param authentication the BooleanOperator which determines if incoming
	 * 		requests are allowed to connect to this AccessPoint.
	 */
	public void setAuthentication(BooleanOperator<WaybackRequest> authentication) {
		this.authentication = authentication;
	}

	/**
	 * @return the WaybackCollection used by this AccessPoint
	 */
	public WaybackCollection getCollection() {
		return collection;
	}

	/**
	 * @param collection the WaybackCollection to use with this AccessPoint
	 */
	public void setCollection(WaybackCollection collection) {
		this.collection = collection;
	}

	/**
	 * @return the ExceptionRenderer in use with this AccessPoint
	 */
	public ExceptionRenderer getException() {
		return exception;
	}

	/**
	 * @param exception the ExceptionRender to use with this AccessPoint
	 */
	public void setException(ExceptionRenderer exception) {
		this.exception = exception;
	}

	/**
	 * @return the String url prefix to use when generating self referencing 
	 * 			URLs
	 */
	public String getUrlRoot() {
		return urlRoot;
	}

	/**
	 * @param urlRoot explicit URL prefix to use when creating self referencing
	 * 		URLs
	 */
	public void setUrlRoot(String urlRoot) {
		this.urlRoot = urlRoot;
	}

	/**
	 * @return the exactHostMatch
	 */
	public boolean isExactHostMatch() {
		return exactHostMatch;
	}

	/**
	 * @param exactHostMatch if true, then only SearchResults exactly matching
	 * 		the requested hostname will be returned from this AccessPoint. If
	 * 		false, then hosts which canonicalize to the same host as requested
	 * 		hostname will be returned (www.)
	 */
	public void setExactHostMatch(boolean exactHostMatch) {
		this.exactHostMatch = exactHostMatch;
	}
}
