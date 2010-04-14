/* RemoteLiveWebCache
 *
 * $Id$:
 *
 * Created on Dec 15, 2009.
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
