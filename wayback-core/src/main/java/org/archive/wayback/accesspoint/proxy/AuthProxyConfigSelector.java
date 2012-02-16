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
import java.io.UnsupportedEncodingException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.archive.wayback.replay.StringHttpServletResponseWrapper;

public class AuthProxyConfigSelector implements ProxyConfigSelector {
	
	public final static String PROXY_REFERRER_KEY = "wayback-wombat-proxy-referrer";
	
	private String proxyInfoJsp = "/WEB-INF/replay/ProxyInfo.jsp";
	
	private String authMsg = "Please enter the collection number to see Wayback content from that collection. (You can leave the password blank)";
	
	public String getProxyInfoJsp() {
		return proxyInfoJsp;
	}

	public void setProxyInfoJsp(String proxyInfoJsp) {
		this.proxyInfoJsp = proxyInfoJsp;
	}

	public String getAuthMsg() {
		return authMsg;
	}

	public void setAuthMsg(String authMsg) {
		this.authMsg = authMsg;
	}

	public String resolveConfig(HttpServletRequest request) {
		String authenticate = request.getHeader("Proxy-Authorization");

		if (authenticate != null) {
			String auth = decodeBasic(authenticate);
			if (auth != null) {
				int userEnd = auth.indexOf(':');
				return auth.substring(0, userEnd);
			}
		}
		
		return null;
	}

	public boolean selectConfigHandler(HttpServletRequest request, HttpServletResponse response, ProxyAccessPoint proxy) throws IOException
	{
		response.setStatus(HttpServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED); //407
		response.setHeader("Proxy-Authenticate", "Basic realm=\"" + authMsg + "\"");
		response.setContentType("text/html");
		
		//TODO: Better way to pass this to jsp?
		request.setAttribute("proxyAccessPoint", proxy);		
		
		StringHttpServletResponseWrapper wrappedResponse = 
			new StringHttpServletResponseWrapper(response);
		RequestDispatcher dispatcher = request.getRequestDispatcher(proxyInfoJsp);
		
		try {
			dispatcher.forward(request, wrappedResponse);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		PrintWriter writer = response.getWriter();
		writer.println(wrappedResponse.getStringResponse());
		return true;
	}
	
	private String decodeBasic(String authHeaderValue) {
		if(authHeaderValue != null) {
			if(authHeaderValue.startsWith("Basic ")) {
				String b64 = authHeaderValue.substring(6);
				byte[] decoded = Base64.decodeBase64(b64.getBytes());
				try {
					return new String(decoded,"utf-8");
				} catch (UnsupportedEncodingException e) {
					// really?...
					return new String(decoded);
				}
			}
		}
		return null;

	}

	public void handleSwitch(HttpServletRequest request,
			HttpServletResponse response, ProxyAccessPoint proxy) throws IOException {

		// Check reset cookie...
		HttpSession sess = request.getSession();
		String referrer = (String)sess.getAttribute(PROXY_REFERRER_KEY);
		
		// If referrer not set, we're sending the switch request
		if (referrer == null) {
			String httpReferrer = request.getHeader("Referer");
			if (httpReferrer == null) {
				httpReferrer = proxy.getReplayPrefix();
			}
			sess.setAttribute(PROXY_REFERRER_KEY, httpReferrer);
			
			selectConfigHandler(request, response, proxy);
		} else {
			sess.removeAttribute(PROXY_REFERRER_KEY);
			response.sendRedirect(referrer);
		}		
	}

	public void handleProxyPac(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		//No Special Handling for Proxy Pac request
	}
}
