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

package org.archive.wayback.accesspoint.proxy;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.accesspoint.CompositeAccessPoint;
import org.archive.wayback.webapp.AccessPoint;

public class ProxyAccessPoint extends CompositeAccessPoint {
	
	private static final Logger LOGGER =
		Logger.getLogger(ProxyAccessPoint.class.getName());
	
	public final static String SWITCH_COLLECTION_PATH = "http://wayback-proxy/switchCollection";
	public final static String PROXY_PAC_PATH = "/proxy.pac";
	
	private List<String> directHosts;
	private AccessPoint nonProxyAccessPoint;
	
	private ResultURIConverter archivalToProxyConverter;
	
	private ProxyConfigSelector configSelector;
	
	private String proxyHostPort;
	private String httpsProxyHostPort;
	
	@Override
	public boolean isProxyEnabled()
	{
		return (configSelector != null);
	}	

	public ProxyConfigSelector getConfigSelector() {
		return configSelector;
	}

	public void setConfigSelector(ProxyConfigSelector configSelector) {
		this.configSelector = configSelector;
	}

	public List<String> getDirectHosts() {
		return directHosts;
	}

	public void setDirectHosts(List<String> directHosts) {
		this.directHosts = directHosts;
	}
	
	public AccessPoint getNonProxyAccessPoint() {
		return nonProxyAccessPoint;
	}

	public void setNonProxyAccessPoint(AccessPoint nonProxyAccessPoint) {
		this.nonProxyAccessPoint = nonProxyAccessPoint;
	}

	public String getProxyHostPort() {
		return proxyHostPort;
	}

	public void setProxyHostPort(String proxyHostPort) {
		this.proxyHostPort = proxyHostPort;
	}

	public String getHttpsProxyHostPort() {
		return httpsProxyHostPort;
	}

	public void setHttpsProxyHostPort(String httpsProxyHostPort) {
		this.httpsProxyHostPort = httpsProxyHostPort;
	}

	// Special converter for fomratting archival url that comes in to proxy mode
	// if not specified, default uriConverter from AccessPoint may be used
	public ResultURIConverter getArchivalToProxyConverter() {
		return archivalToProxyConverter;
	}

	public void setArchivalToProxyConverter(
	        ResultURIConverter archivalToProxyConverter) {
		this.archivalToProxyConverter = archivalToProxyConverter;
	}

	@Override
	public boolean handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {
		
//		boolean isProxyReq = false;
	
// Proxy-Connection header is not reliable to differentiate between proxy and non-proxy
// so not using it anymore.. Define access points explicitly and proxy or non-proxy
// on different ports
		
//		if (request.getHeader("Proxy-Connection") != null) {
//			isProxyReq = true;
		
		
		if (!isProxyEnabled()) {
			return handleNonProxy(request, response);
		} else {
			return handleProxy(request, response);
		}
	}
	
	protected boolean handleNonProxy(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		String uri = request.getRequestURI();
															
		if (uri.endsWith(PROXY_PAC_PATH)) {
			this.writeProxyPac(request, response);
			return true;
		}

		return baseHandleRequest(request, response);
	}
		
	protected boolean handleProxy(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{			
		StringBuffer urlBuff = request.getRequestURL();
		String url = urlBuff.toString();
		
		boolean isProxyHost = url.contains(getReplayPrefix());
		
		// Check the nonProxyAccessPoint prefix
		if (!isProxyHost && (nonProxyAccessPoint != null)) {
			isProxyHost = url.contains(nonProxyAccessPoint.getReplayPrefix());
		}
								
		// Special reset link
		if (url.equals(SWITCH_COLLECTION_PATH)) {
			configSelector.handleSwitch(request, response, this);
			return true;
		}
		
		String realAccessPoint = configSelector.resolveConfig(request);
		
		if (realAccessPoint != null) {
			
			// See if the archival url form was included and redirect to strip it
			if (isProxyHost) {
				String prefix = "/" + realAccessPoint + "/";
				String uri = request.getRequestURI();
				
				
				if (uri.length() > prefix.length()) {		
					String requestUrl = uri.substring(prefix.length());
					
					// If matches this config, simply redirect and strip
					if (uri.startsWith(prefix)) {
						
						String paths[] = requestUrl.split("/", 2);
						if (paths.length < 2) {
							return false;
						}
						String timestamp = paths[0];
						String replayUrl = paths[1];
						// Calendar -> redirect to collection-less proxy query
						String redirUrl = null;
						
						if (timestamp.contains("*")) {
							redirUrl = "/" + requestUrl;
						} else {
							ResultURIConverter converter = getArchivalToProxyConverter();
							if (converter == null) {
								converter = this.getUriConverter();
							}
							redirUrl = converter.makeReplayURI(timestamp, replayUrl);
						}
						
						response.sendRedirect(redirUrl);
					
						return true;
					}
				}
				
				//If archival url with any *different* config, force a selection
				//if (ReplayRequestParser.WB_REQUEST_REGEX.matcher(requestUrl).matches()) {
				//	return configSelector.selectConfigHandler(request, response, this);					
				//}
			}	
			
			Status status = handleRequest(realAccessPoint, request, response);
			
			switch (status) {
			case ConfigHandled:
				return true;
				
			case ConfigNotHandled:
				return false;
				
			case ConfigNotFound:
				break;
			}
		}
		
		return configSelector.selectConfigHandler(request, response, this);
	}
	
	protected boolean baseHandleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		if (nonProxyAccessPoint != null) {
			return nonProxyAccessPoint.handleRequest(request, response);
		} else {
			return super.handleRequest(request, response);
		}
	}	
	
	protected void writeProxyPac(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		
		//TODO: Is this needed anymore (was only for Ipauth testing)
		if (configSelector != null) {		
			configSelector.handleProxyPac(httpRequest, httpResponse);
		}
		
		String proxyPath = getProxyHostPort();

		if (proxyPath == null) {
			String hostName = httpRequest.getServerName();
			int port = httpRequest.getServerPort();
			proxyPath = hostName + ":" + port;
		}
		
		String httpsProxyPath = getHttpsProxyHostPort();
		
		if (httpsProxyPath == null) {
			httpsProxyPath = proxyPath;
		}
			
		httpResponse.setContentType("application/x-ns-proxy-autoconfig");
		
		LOGGER.fine("updating proxy .pac");
		
		PrintWriter writer = httpResponse.getWriter();
		writer.println("function FindProxyForURL (url, host) {");
		
		for (String host : directHosts) {
			writer.println("  if (shExpMatch(host, \"" + host + "\")) { return \"DIRECT\"; }");
		}
		
		// For https support
		writer.println("  if (url.substring(0, 6) == \"https:\") { return \"PROXY " + httpsProxyPath + "\"; }\n");
		
		writer.println("  return \"PROXY " + proxyPath + "\";\n}");
	}
}
