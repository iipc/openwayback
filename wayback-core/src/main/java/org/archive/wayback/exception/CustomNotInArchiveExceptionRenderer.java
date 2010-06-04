/* CustomNotInArchiveExceptionRenderer
 *
 * $Id$
 *
 * Created on 1:21:49 PM Jul 8, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
