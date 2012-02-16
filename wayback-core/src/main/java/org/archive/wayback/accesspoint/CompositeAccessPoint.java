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

package org.archive.wayback.accesspoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;
import org.archive.wayback.webapp.AccessPoint;

public class CompositeAccessPoint extends AccessPoint {
	
	protected final static String REQUEST_CONTEXT_PREFIX = 
		"webapp-request-context-path-prefix";
	
	protected enum Status
	{
		ConfigNotFound,
		ConfigHandled,
		ConfigNotHandled,
	}
	
	private HashMap<String, AccessPointAdapter> accessPointCache;
	
	public CompositeAccessPoint()
	{
		accessPointCache = new HashMap<String, AccessPointAdapter>();
	}
	
	@Override
	public boolean handleRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {
		
		String configName = request.getRequestURI();
		
		if (!configName.isEmpty() && (configName.charAt(0) == '/')) {
			configName = configName.substring(1);
		}
		
		int slash = configName.indexOf('/');
		
		if (slash >= 0) {
			configName = configName.substring(0, slash);
		}
		
		request.setAttribute(REQUEST_CONTEXT_PREFIX, "/" + configName + "/");
		
		Status status = handleRequest(configName, request, response);
		return (status == Status.ConfigHandled);
	}
	
	protected Status handleRequest(String realAccessPoint, HttpServletRequest request,
			HttpServletResponse response) throws ServletException,
			IOException {
		
		// First, check cached accessPoint
		AccessPointAdapter adapter = accessPointCache.get(realAccessPoint);
		
		if ((adapter == null) && (accessPointConfigs != null)) {		
			AccessPointConfig config = accessPointConfigs.getAccessPointConfigs().get(realAccessPoint);
		
			if (config != null) {
				adapter = new AccessPointAdapter(this, config);
				accessPointCache.put(realAccessPoint, adapter);
			}
		}
		
		if (adapter == null) {
			return Status.ConfigNotFound;			
		}
		
		boolean handled = adapter.handleRequest(request, response);
		return (handled ? Status.ConfigHandled : Status.ConfigNotHandled);
	}
	
	private String oracleUrl;
	private ArrayList<ExclusionFilterFactory> staticExclusions;
	
	private ContextResultURIConverterFactory uriConverterFactory;
		
	public ContextResultURIConverterFactory getUriConverterFactory() {
		return uriConverterFactory;
	}

	public void setUriConverterFactory(ContextResultURIConverterFactory uriConverterFactory) {
		this.uriConverterFactory = uriConverterFactory;
	}

	public ArrayList<ExclusionFilterFactory> getStaticExclusions() {
		return staticExclusions;
	}

	public void setStaticExclusions(
			ArrayList<ExclusionFilterFactory> staticExclusions) {
		this.staticExclusions = staticExclusions;
	}

	private Map<String, Object> userProps;
	
	private AccessPointConfigs accessPointConfigs;
	
	public String getOracleUrl() {
		return oracleUrl;
	}
	public void setOracleUrl(String oracleUrl) {
		this.oracleUrl = oracleUrl;
	}
	public Map<String, Object> getUserProps() {
		return userProps;
	}

	public void setUserProps(Map<String, Object> userProps) {
		this.userProps = userProps;
	}

	public AccessPointConfigs getAccessPointConfigs() {
		return accessPointConfigs;
	}

	public void setAccessPointConfigs(AccessPointConfigs accessPointConfigs) {
		this.accessPointConfigs = accessPointConfigs;
	}
	
	public AccessPointConfig findConfigForFile(String file)
	{
		for (AccessPointConfig config : accessPointConfigs.getAccessPointConfigs().values()) {
			for (String prefix : config.getFileIncludePrefixes()) {
				if (file.startsWith(prefix)) {
					return config;
				}
			}
		}
		
		return null;
	}
}
