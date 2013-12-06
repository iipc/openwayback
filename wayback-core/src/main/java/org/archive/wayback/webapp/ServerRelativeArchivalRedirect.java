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
package org.archive.wayback.webapp;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.url.UsableURI;
import org.archive.url.UsableURIFactory;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

/**
 * @author brad
 *
 */
public class ServerRelativeArchivalRedirect extends AbstractRequestHandler {
	private static final Logger LOGGER = Logger.getLogger(
			ServerRelativeArchivalRedirect.class.getName());
	boolean useCollection = false;
	private String matchHost = null;
	private int matchPort = -1;
	private String replayPrefix;
	
	private boolean handleRequestWithCollection(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		boolean handled = false;
		// hope that it's a server relative request, with a valid referrer:
		String referer = httpRequest.getHeader("Referer");
		if(referer != null) {
			UsableURI uri = UsableURIFactory.getInstance(referer);
			
			// Check that the Referer is our current wayback path
			// before attempting to use referer as base archival url
			
			if (((matchHost != null) && !matchHost.equals(uri.getHost())) ||
				((matchPort != -1) && (uri.getPort() != -1) && (matchPort != uri.getPort()))) {
				LOGGER.info("Server-Relative-Redirect: Skipping, Referer " + uri.getHost() + ":" + uri.getPort() + " not from matching wayback host:port\t");
				return false;
			}	
			
			String path = uri.getPath();
			int secondSlash = path.indexOf('/',1);
			if(secondSlash > -1) {
				String collection = path.substring(0, secondSlash);
				collection = modifyCollection(collection);
				String remainder = path.substring(secondSlash+1);
				int thirdSlash = remainder.indexOf('/');
				if(thirdSlash > -1) {
					String datespec = remainder.substring(0,thirdSlash);
					if (!datespec.isEmpty() && !Character.isDigit(datespec.charAt(0))) {
						datespec = null;
					}
					
					String url = ArchiveUtils.addImpliedHttpIfNecessary(
					remainder.substring(thirdSlash+1));
					String thisPath = httpRequest.getRequestURI();
					String queryString = httpRequest.getQueryString();
					if (queryString != null) {
						thisPath += "?" + queryString;
					}
					
					String resolved = UrlOperations.resolveUrl(url, thisPath);
					String contextPath = httpRequest.getContextPath();
					StringBuilder sb = new StringBuilder(uri.getScheme());
					sb.append("://"); 
					sb.append(uri.getAuthority());
					sb.append(contextPath);
					sb.append(collection);
					sb.append("/");
					if (datespec != null) {
						sb.append(datespec);
						sb.append("/");
					}
					sb.append(resolved);
					String finalUrl = sb.toString();
					
					// cross your fingers!!!
					LOGGER.info("Server-Relative-Redirect:\t" + referer + "\t" 
					+ thisPath + "\t" + finalUrl);
					
					// Gotta make sure this is properly cached, or
					// weird things happen:
					httpResponse.addHeader("Vary", "Referer");
					httpResponse.sendRedirect(finalUrl);
					handled = true;
				
				}
			}
		}
		        
		return handled;
	}

	// Default just return the referrer's collection, but allow subclasses to provide a custom replacement
	protected String modifyCollection(String collection) {
		return collection;
    }

	private boolean handleRequestWithoutCollection(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		boolean handled = false;
		// hope that it's a server relative request, with a valid referrer:
		String referer = httpRequest.getHeader("Referer");
		if(referer != null) {
			LOGGER.fine("referer:" + referer);
			UsableURI uri = UsableURIFactory.getInstance(referer);
			String path = uri.getPath();

			String remainder = path.substring(1);
			int thirdSlash = remainder.indexOf('/');
			LOGGER.fine("referer:(" + referer + ") remain(" + remainder + ") 3rd("+thirdSlash+")");
			if(thirdSlash > -1) {
				String datespec = remainder.substring(0,thirdSlash);
				String url = ArchiveUtils.addImpliedHttpIfNecessary(
						remainder.substring(thirdSlash+1));
				String thisPath = httpRequest.getRequestURI();
				String queryString = httpRequest.getQueryString();
				if (queryString != null) {
					thisPath += "?" + queryString;
				}
				
				String resolved = UrlOperations.resolveUrl(url, thisPath);
				String contextPath = httpRequest.getContextPath();
				String finalUrl = uri.getScheme() + "://" + 
					uri.getAuthority() + contextPath + "/" 
					+ datespec + "/" + resolved;
				// cross your fingers!!!
				LOGGER.info("Server-Relative-Redirect:\t" + referer + "\t" 
						+ thisPath + "\t" + finalUrl);

				// Gotta make sure this is properly cached, or
				// weird things happen:
				httpResponse.addHeader("Vary", "Referer");
				httpResponse.sendRedirect(finalUrl);
				handled = true;
			
			}
		}
		        
		return handled;
	}

	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		if(matchHost != null) {
			
			if(!matchHost.equals(httpRequest.getServerName())) {
				LOGGER.fine("Wrong host for ServerRelativeRed(" + 
						httpRequest.getServerName() +")");
				return false;
			}
		}
		if(matchPort != -1) {
			if(matchPort != httpRequest.getLocalPort()) {
				LOGGER.fine("Wrong port for ServerRealtiveRed(" + 
						httpRequest.getServerName() + ")(" + 
						httpRequest.getLocalPort() +") :" + 
						httpRequest.getRequestURI());
				return false;
			}
		}
		boolean handled = useCollection ? 
				handleRequestWithCollection(httpRequest, httpResponse):
					handleRequestWithoutCollection(httpRequest, httpResponse);
		if(!handled) {
			if(replayPrefix != null) {
				String thisPath = httpRequest.getRequestURI();
				String queryString = httpRequest.getQueryString();
				if (queryString != null) {
					thisPath += "?" + queryString;
				}
				//TODO: rethink this fallback, for now adding https support as well
				if(thisPath.startsWith("/http://") || thisPath.startsWith("/https://")) {
					// assume a replay request:
					StringBuilder sb = new StringBuilder(thisPath.length() + replayPrefix.length() + 16);
					sb.append(replayPrefix);
					sb.append(Timestamp.currentTimestamp().getDateStr());
					sb.append(thisPath);
					httpResponse.sendRedirect(sb.toString());
					handled = true;
				}
			}
		}
		return handled;
	}

	/**
	 * @return the useCollection
	 */
	public boolean isUseCollection() {
		return useCollection;
	}
	/**
	 * @param useCollection the useCollection to set
	 */
	public void setUseCollection(boolean useCollection) {
		this.useCollection = useCollection;
	}
	/**
	 * @return the matchHost
	 */
	public String getMatchHost() {
		return matchHost;
	}
	/**
	 * @param matchHost the matchHost to set
	 */
	public void setMatchHost(String matchHost) {
		this.matchHost = matchHost;
	}
	/**
	 * @return the matchPort
	 */
	public int getMatchPort() {
		return matchPort;
	}
	/**
	 * @param matchPort the matchPort to set
	 */
	public void setMatchPort(int matchPort) {
		this.matchPort = matchPort;
	}

	/**
	 * @return the replayPrefix
	 */
	public String getReplayPrefix() {
		return replayPrefix;
	}

	/**
	 * @param replayPrefix the replayPrefix to set
	 */
	public void setReplayPrefix(String replayPrefix) {
		this.replayPrefix = replayPrefix;
	}
}
