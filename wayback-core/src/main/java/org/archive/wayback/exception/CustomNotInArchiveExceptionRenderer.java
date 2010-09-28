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
package org.archive.wayback.exception;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 * @deprecated
 */
public class CustomNotInArchiveExceptionRenderer extends BaseExceptionRenderer  {
	private HashMap<String,Object> hosts = null;
	private String jspHandler = null;
	
	
//	public String getExceptionHandler(HttpServletRequest httpRequest,
//			HttpServletResponse httpResponse, WaybackRequest wbRequest,
//			WaybackException exception) {
//		String jspPath = getCustomHandler(exception,wbRequest);
//		if(jspPath == null) {
//			jspPath = super.getExceptionHandler(httpRequest, httpResponse,
//					wbRequest, exception);
//		}
//		return jspPath;
//	}


	/**
	 * @param exception
	 * @param wbRequest
	 * @return
	 */
	private String getCustomHandler(WaybackException exception,
			WaybackRequest wbRequest) {
		if((exception instanceof ResourceNotInArchiveException)
				&& wbRequest.isReplayRequest()) {
			String url = wbRequest.getRequestUrl();
			String host = UrlOperations.urlToHost(url);
			if(hosts.containsKey(host)) {
				return jspHandler;
			}
		}
		return null;
	}


	public String getJspHandler() {
		return jspHandler;
	}


	public void setJspHandler(String jspHandler) {
		this.jspHandler = jspHandler;
	}
	public List<String> getHosts() {
		return null;
	}
	public void setHosts(List<String> hosts) {
		this.hosts = new HashMap<String,Object>();
		for(String host : hosts) {
			this.hosts.put(host, null);
		}
	}
}
