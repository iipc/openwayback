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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.util.bdb.BDBMap;

public class IPProxyConfigSelector implements ProxyConfigSelector {
	
	protected String proxyInfoJsp = "/WEB-INF/replay/ProxyInfo.jsp";

	public String resolveConfig(HttpServletRequest request) {
		String context = request.getContextPath();
    	BDBMap bdbMap = BDBMap.getContextMap(context);
    	
    	String key = genKey(request);
        String coll = bdbMap.get(key);
        return coll;
	}
	
	protected String genKey(HttpServletRequest request)
	{
		return request.getRemoteAddr() + "$coll";		
	}

	public boolean selectConfigHandler(HttpServletRequest request,
			HttpServletResponse response, ProxyAccessPoint proxy) throws IOException {
		
		request.setAttribute("proxyAccessPoint", proxy);
		
		RequestDispatcher dispatcher = request.getRequestDispatcher(proxyInfoJsp);
		
		try {
			dispatcher.forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

	public void handleSwitch(HttpServletRequest request,
			HttpServletResponse response, ProxyAccessPoint proxy)
			throws IOException {
		
		String config = request.getParameter("config");
		
		if (config == null) {
			selectConfigHandler(request, response, proxy);
			return;
		}
		
		setConfig(request, config);
				
		String referrer = request.getHeader("Referer");
		if (referrer == null) {
			referrer = proxy.getReplayPrefix();
		}
		response.sendRedirect(referrer);		
	}
	
	protected void setConfig(HttpServletRequest request, String config)
	{
		String context = request.getContextPath();
	    BDBMap bdbMap = BDBMap.getContextMap(context);
	    	
		String key = genKey(request);
		bdbMap.put(key, config);
	}

	public void handleProxyPac(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		
		String uri = httpRequest.getRequestURI();
		int pacStrIndex = uri.indexOf(ProxyAccessPoint.PROXY_PAC_PATH);
		
		if (pacStrIndex >= 0) {
			String config = uri.substring(1, pacStrIndex);
			//System.out.println("config: " + config);
			setConfig(httpRequest, config);
		}
	}

	public String getProxyInfoJsp() {
		return proxyInfoJsp;
	}

	public void setProxyInfoJsp(String proxyInfoJsp) {
		this.proxyInfoJsp = proxyInfoJsp;
	}
}
