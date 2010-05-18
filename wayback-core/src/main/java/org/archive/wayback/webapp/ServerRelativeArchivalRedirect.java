/* ServerRelativeArchivalRedirect
 *
 * $Id$:
 *
 * Created on May 3, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.webapp;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.util.ArchiveUtils;
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
	
	private boolean handleRequestWithCollection(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		boolean handled = false;
		// hope that it's a server relative request, with a valid referrer:
		String referer = httpRequest.getHeader("Referer");
		if(referer != null) {
			UURI uri = UURIFactory.getInstance(referer);
			String path = uri.getPath();
			int secondSlash = path.indexOf('/',1);
			if(secondSlash > -1) {
				String collection = path.substring(0,secondSlash);
				String remainder = path.substring(secondSlash+1);
				int thirdSlash = remainder.indexOf('/');
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
					uri.getAuthority() + contextPath + collection + "/" 
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
		}
		        
		return handled;
	}

	private boolean handleRequestWithoutCollection(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		boolean handled = false;
		// hope that it's a server relative request, with a valid referrer:
		String referer = httpRequest.getHeader("Referer");
		if(referer != null) {
			LOGGER.trace("referer:" + referer);
			UURI uri = UURIFactory.getInstance(referer);
			String path = uri.getPath();

			String remainder = path.substring(1);
			int thirdSlash = remainder.indexOf('/');
			LOGGER.trace("referer:(" + referer + ") remain(" + remainder + ") 3rd("+thirdSlash+")");
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
				LOGGER.trace("Wrong host for ServerRelativeRed(" + 
						httpRequest.getServerName() +")");
				return false;
			}
		}
		if(matchPort != -1) {
			if(matchPort != httpRequest.getLocalPort()) {
				LOGGER.trace("Wrong port for ServerRealtiveRed(" + 
						httpRequest.getServerName() + ")(" + 
						httpRequest.getLocalPort() +") :" + 
						httpRequest.getRequestURI());
				return false;
			}
		}
		return useCollection ? 
				handleRequestWithCollection(httpRequest, httpResponse):
					handleRequestWithoutCollection(httpRequest, httpResponse);
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
}
