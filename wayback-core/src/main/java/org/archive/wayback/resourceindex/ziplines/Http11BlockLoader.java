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
package org.archive.wayback.resourceindex.ziplines;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.archive.wayback.webapp.PerformanceLogger;

import com.google.common.io.ByteStreams;

/**
 * Class which wraps most of the complexity of an apache commons httpclient
 * MultiThreaderHttpConnectionManager, exposing common configuration elements
 * to Spring configuration.
 * 
 * This class is a near direct copy of RemoteLiveWebCache: refactoring needed.
 * 
 * @author brad
 *
 */
public class Http11BlockLoader implements BlockLoader {
	private static final Logger LOGGER = Logger.getLogger(
			Http11BlockLoader.class.getName());

    private MultiThreadedHttpConnectionManager connectionManager = null;
    private HostConfiguration hostConfiguration = null;
    private HttpClient http = null; 

    /**
     * 
     */
    public Http11BlockLoader() {
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
		HttpClientParams params = new HttpClientParams();
//        params.setParameter(HttpClientParams.RETRY_HANDLER, new NoRetryHandler());
    	http = new HttpClient(params,connectionManager);
    	http.setHostConfiguration(hostConfiguration);
    }

	/**
	 * Fetch a range of bytes from a particular URL. Note that the bytes are
	 * read into memory all at once, so care should be taken with the length
	 * argument.
	 * 
	 * @param url String URL to fetch
	 * @param offset byte start offset of the desired range
	 * @param length number of octets to fetch
	 * @return a new byte[] containing the octets fetched
	 * @throws IOException on HTTP and Socket failures, as well as Timeouts
	 */
	public byte[] getBlock(String url, long offset, int length) 
	throws IOException {

		HttpMethod method = null;
		try {
			method = new GetMethod(url);
		} catch(IllegalArgumentException e) {
			LOGGER.warning("Bad URL for block fetch:" + url);
			throw new IOException("Url:" + url + " does not look like an URL?");
		}
		StringBuilder sb = new StringBuilder(16);
		sb.append(ZiplinedBlock.BYTES_HEADER).append(offset);
		sb.append(ZiplinedBlock.BYTES_MINUS).append((offset + length)-1);
		String rangeHeader = sb.toString();
		method.addRequestHeader(ZiplinedBlock.RANGE_HEADER, rangeHeader);
		//uc.setRequestProperty(RANGE_HEADER, sb.toString());
		long start = System.currentTimeMillis();
	    try {
			LOGGER.fine("Reading block:" + url + "("+rangeHeader+")");
	    	int status = http.executeMethod(method);
	    	if((status == 200) || (status == 206)) {
	    		InputStream is = method.getResponseBodyAsStream();
	    		byte[] block = new byte[length];
	    		ByteStreams.readFully(is, block);
	    		long elapsed = System.currentTimeMillis() - start;
				PerformanceLogger.noteElapsed("CDXBlockLoad",elapsed,url);
	    		return block;
	    		
	    	} else {
	    		throw new IOException("Bad status for " + url);
	    	}
		} finally {
	    	method.releaseConnection();
	    }
	}    

    /**
     * @param hostPort to proxy requests through - ex. "localhost:3128"
     */
    public void setProxyHostPort(String hostPort) {
    	int colonIdx = hostPort.indexOf(':');
    	if(colonIdx > 0) {
    		String host = hostPort.substring(0,colonIdx);
    		int port = Integer.valueOf(hostPort.substring(colonIdx+1));
    		
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
     * @return the HttpConnectionManagerParams maxTotalConnections config
     */
    public int getMaxTotalConnections() {
    	return connectionManager.getParams().getMaxTotalConnections();
    }

    /**
     * @param maxHostConnections the HttpConnectionManagerParams config 
     */
    public void setMaxHostConnections(int maxHostConnections) {
    	connectionManager.getParams().
    		setMaxConnectionsPerHost(hostConfiguration, maxHostConnections);
    }

    /**
     * @return the HttpConnectionManagerParams maxHostConnections config 
     */
    public int getMaxHostConnections() {
    	return connectionManager.getParams().
    		getMaxConnectionsPerHost(hostConfiguration);
    }

    /**
	 * @return the connectionTimeoutMS
	 */
	public int getConnectionTimeoutMS() {
		return connectionManager.getParams().getConnectionTimeout();
	}

	/**
	 * @param connectionTimeoutMS the connectionTimeoutMS to set
	 */
	public void setConnectionTimeoutMS(int connectionTimeoutMS) {
    	connectionManager.getParams().setConnectionTimeout(connectionTimeoutMS);
	}

	/**
	 * @return the socketTimeoutMS
	 */
	public int getSocketTimeoutMS() {
		return connectionManager.getParams().getSoTimeout();
	}

	/**
	 * @param socketTimeoutMS the socketTimeoutMS to set
	 */
	public void setSocketTimeoutMS(int socketTimeoutMS) {
    	connectionManager.getParams().setSoTimeout(socketTimeoutMS);
	}
}
