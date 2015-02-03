/*
 * Copyright 2014 Bibliotheca Alexandrina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.archive.wayback.liveweb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.logging.Logger;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;
import org.archive.wayback.exception.LiveWebCacheUnavailableException;
import org.archive.wayback.exception.LiveWebTimeoutException;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.resourcestore.resourcefile.ArcResource;
import org.archive.wayback.resourcestore.resourcefile.ResourceFactory;

/**
 *
 * @author Mohamed Elsayed
 */

public class RemoteLiveWebCache2 implements LiveWebCache
{
    private static final Logger LOGGER = Logger.getLogger(
			RemoteLiveWebCache.class.getName());

    protected MultiThreadedHttpConnectionManager connectionManager = null;
    protected HostConfiguration hostConfiguration = null;
    protected HttpClient http = null; 
    protected String requestPrefix = null;
    
    public RemoteLiveWebCache2() 
    {
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
	HttpClientParams params = new HttpClientParams();
        params.setParameter(HttpClientParams.RETRY_HANDLER, new NoRetryHandler());
    	http = new HttpClient(params, connectionManager);
    	http.setHostConfiguration(hostConfiguration);
    }
    
    public Resource getCachedResource(URL url, long maxCacheMS, boolean bUseOlder) 
            throws LiveDocumentNotAvailableException, LiveWebCacheUnavailableException, LiveWebTimeoutException, IOException 
    {
        String urlString = url.toExternalForm();
        
        if (requestPrefix != null)
                urlString = requestPrefix + urlString;
        
        HttpHost proxy = new HttpHost(hostConfiguration.getProxyHost(), hostConfiguration.getProxyPort());
        DefaultProxyRoutePlanner routePlanner;
        routePlanner = new DefaultProxyRoutePlanner(proxy);
        CloseableHttpClient httpclient = HttpClients.custom().setRoutePlanner(routePlanner).build();
        HttpGet httpGet = new HttpGet(urlString);
        CloseableHttpResponse response = httpclient.execute(httpGet);
        
        try 
        {
            String headerAsString = String.format("%s %d %s\n", response.getStatusLine().getProtocolVersion(),
                                                                response.getStatusLine().getStatusCode(),
                                                                response.getStatusLine().getReasonPhrase());
            
            for(Header header : response.getAllHeaders())
                headerAsString += header.toString() + "\n";
            
            headerAsString += "\n";
            
            HttpEntity entity = response.getEntity();
            String bodyAsString = EntityUtils.toString(entity);

            int length = headerAsString.length() + bodyAsString.length();
            String responseAsString = urlString + " 0.0.0.0 10000000000000 text/plain " + 
                                    length + "\n" + headerAsString + bodyAsString;
            
            ByteArrayInputStream bais = new ByteArrayInputStream(responseAsString.getBytes());
            ARCRecord r = new ARCRecord(bais, "id", 0L, false, false, true);
            ArcResource ar = (ArcResource) ResourceFactory.ARCArchiveRecordToResource(r, null);
            
            if(ar.getStatusCode() == 502) {
                    throw new LiveDocumentNotAvailableException(urlString);
            } else if(ar.getStatusCode() == 504) {
                    throw new LiveWebTimeoutException("Timeout:" + urlString);
            }

            // Do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity);
            return ar;
        }
        catch (ResourceNotAvailableException e) 
        {
            throw new LiveDocumentNotAvailableException(urlString);
        }
        catch (NoHttpResponseException e) 
        {
            throw new LiveWebCacheUnavailableException("No Http Response for " + urlString);
        }
        catch (ConnectException e) 
        {
            throw new LiveWebCacheUnavailableException(e.getLocalizedMessage() + " : " + urlString);
        }
        catch (SocketException e) 
        {
            throw new LiveWebCacheUnavailableException(e.getLocalizedMessage() + " : " + urlString);
        }
        catch (SocketTimeoutException e) 
        {
            throw new LiveWebTimeoutException(e.getLocalizedMessage() + " : " + urlString);
        }
        catch(ConnectTimeoutException e) 
        {
            throw new LiveWebTimeoutException(e.getLocalizedMessage() + " : " + urlString);
        }
        finally 
        {
            response.close();
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

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
