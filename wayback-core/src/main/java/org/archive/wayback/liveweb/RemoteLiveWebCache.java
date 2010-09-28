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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;

/**
 * @author brad
 *
 */
public class RemoteLiveWebCache implements LiveWebCache {

    private MultiThreadedHttpConnectionManager connectionManager = null;
    private HostConfiguration hostConfiguration = null;
    private HttpClient http = null; 
    /**
     * 
     */
    public RemoteLiveWebCache() {
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
    	http = new HttpClient(connectionManager);
    	http.setHostConfiguration(hostConfiguration);
    }

	/* (non-Javadoc)
	 * @see org.archive.wayback.liveweb.LiveWebCache#getCachedResource(java.net.URL, long, boolean)
	 */
	public Resource getCachedResource(URL url, long maxCacheMS,
			boolean bUseOlder) throws LiveDocumentNotAvailableException,
			LiveWebCacheUnavailableException, IOException {
		String urlString = url.toExternalForm();
	    HttpMethod method = new GetMethod(urlString);
	    try {
	    	int status = http.executeMethod(method);
	    	if(status == 200) {
	    		ByteArrayInputStream bais = new ByteArrayInputStream(method.getResponseBody());
	    		ARCRecord r = new ARCRecord(
	    				new GZIPInputStream(bais),
	    				"id",0L,false,false,true);
	    		ArcResource ar = (ArcResource) 
	    			ResourceFactory.ARCArchiveRecordToResource(r, null);
	    		if(ar.getStatusCode() == 502) {
	    			throw new LiveDocumentNotAvailableException(urlString);
	    		}
	    		return ar;
	    		
	    	} else {
	    		throw new LiveWebCacheUnavailableException(urlString);
	    	}
	    } catch (ResourceNotAvailableException e) {
    		throw new LiveDocumentNotAvailableException(urlString);
	    } catch (ConnectException e) {
    		throw new LiveWebCacheUnavailableException(e.getLocalizedMessage() 
    				+ " : " + urlString);
		} finally {
	    	method.releaseConnection();
	    }
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.liveweb.LiveWebCache#shutdown()
	 */
	public void shutdown() {
		// TODO Auto-generated method stub
	}
    

    /**
     * @param hostPort to proxy requests through - ex. "localhost:3128"
     */
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if(colonIdx > 0) {
    		String host = hostPort.substring(0,colonIdx);
    		int port = Integer.valueOf(hostPort.substring(colonIdx+1));
    		
//            http.getHostConfiguration().setProxy(host, port);
    		hostConfiguration.setProxy(host, port);
    	}
    }
    /**
     * @param maxTotalConnections the HttpConnectionManagerParams config
     */
    public void setMaxTotalConnections(int maxTotalConnections) {
    	connectionManager.getParams().
    		setMaxTotalConnections(maxTotalConnections);
    }
    /**
     * @param maxHostConnections the HttpConnectionManagerParams config 
     */
    public void setMaxHostConnections(int maxHostConnections) {
    	connectionManager.getParams().
    		setMaxConnectionsPerHost(hostConfiguration, maxHostConnections);
    }
}
