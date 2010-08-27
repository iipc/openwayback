/* ARCUnwrappingProxy
 *
 * $Id$:
 *
 * Created on Dec 10, 2009.
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

package org.archive.wayback.liveweb;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;
import org.archive.wayback.util.ByteOp;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

/**
 * 
 * ServletRequestContext which proxies to an ARCRecordingProxy, and unwraps 
 * the "application/x-arc-record" MIME response into the inner HTTP response,
 * sending all HTTP headers AS-IS, and the HTTP Entity.
 * 
 * Can be used to use an ARCRecordingProxy with a UserAgent expecting real
 * HTTP responses, not "application/x-arc-record". A web browser for example.
 * 
 * @author brad
 *
 */
public class ARCUnwrappingProxy extends AbstractRequestHandler {
	
	private static final Logger LOGGER = 
		Logger.getLogger(ARCUnwrappingProxy.class.getName());
    private MultiThreadedHttpConnectionManager connectionManager = null;
    private HostConfiguration hostConfiguration = null;
    /**
     * 
     */
    public ARCUnwrappingProxy() {
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
    }

	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		StringBuffer sb = httpRequest.getRequestURL();
		String query = httpRequest.getQueryString();
		if(query != null) {
			sb.append("?").append(query);
		}
        HttpMethod method = new GetMethod(sb.toString());
        boolean got200 = false;
        try {
        	HttpClient http = new HttpClient(connectionManager);
        	http.setHostConfiguration(hostConfiguration);

        	int status = http.executeMethod(method);
        	if(status == 200) {
        		ARCRecord r = 
        			new ARCRecord(new GZIPInputStream(
        					method.getResponseBodyAsStream()),
        					"id",0L,false,false,true);
        		Resource res = null;
        		try {
					res = ResourceFactory.ARCArchiveRecordToResource(r, null);
				} catch (ResourceNotAvailableException e) {
					LOGGER.severe(e.getMessage());
					throw new IOException(e);
				}
        		httpResponse.setStatus(res.getStatusCode());

        		Map<String,String> headers = res.getHttpHeaders();
        		Iterator<String> keys = headers.keySet().iterator();
        		while(keys.hasNext()) {
        			String key = keys.next();
        			if(!key.equalsIgnoreCase("Connection") 
        					&& !key.equalsIgnoreCase("Content-Length")
        					&& !key.equalsIgnoreCase("Transfer-Encoding")) {
	        			String value = headers.get(key);
	        			httpResponse.addHeader(key, value);
        			}
        		}

        		ByteOp.copyStream(res, httpResponse.getOutputStream());
        		got200 = true;
        	}
        } finally {
        	method.releaseConnection();

        }
        
		return got200;
	}

    /**
     * @param hostPort location of ARCRecordingProxy ServletRequestContext, ex:
     *   "localhost:3128"
     */
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if(colonIdx > 0) {
    		String host = hostPort.substring(0,colonIdx);
    		int port = Integer.valueOf(hostPort.substring(colonIdx+1));
    		hostConfiguration.setProxy(host, port);
    	}
    }
}
