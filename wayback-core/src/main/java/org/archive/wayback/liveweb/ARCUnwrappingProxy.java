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
