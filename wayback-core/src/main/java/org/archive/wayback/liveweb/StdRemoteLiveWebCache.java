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
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
 * This class fetches resource from live web. 
 * It works with standard proxy server e.g. Squid.
 * 
 * @author Mohamed Elsayed
 * @see LiveWebCache
 * @see ArcRemoteLiveWebCache
 */
public class StdRemoteLiveWebCache implements LiveWebCache
{
    private static final Logger LOGGER = Logger.getLogger(
			StdRemoteLiveWebCache.class.getName() );

    protected MultiThreadedHttpConnectionManager connectionManager;
    protected HostConfiguration hostConfiguration;
    protected HttpClient httpClient; 
    protected String requestPrefix;
    private CloseableHttpResponse response;
    private ArcResource ar;
    
    /**
     * StdRemoteLiveWebCache constructor initializes and configures connection objects.
     */
    public StdRemoteLiveWebCache() 
    {
    	connectionManager = new MultiThreadedHttpConnectionManager();
    	hostConfiguration = new HostConfiguration();
	HttpClientParams params = new HttpClientParams();
        params.setParameter( HttpClientParams.RETRY_HANDLER, 
                new NoRetryHandler() );
    	httpClient = new HttpClient( params, connectionManager );
    	httpClient.setHostConfiguration( hostConfiguration );
    }
    
    /**
     * Gets resource object from the live web. Configure timeout to 10 seconds.
     *  
     * @param url to fetch from the live web.
     * @param maxCacheMS maximum age of resource to return - optionally honored
     * @param bUseOlder if true, return documents older than maxCacheMS if
     *                  a more recent copy is not available.
     * 
     * @return Resource for url
     * 
     * @throws LiveDocumentNotAvailableException if the resource cannot be
     *         retrieved from the live web, but all proxying and caching 
     *         mechanisms functioned properly
     * @throws LiveWebCacheUnavailableException if there was a problem either
     * 		   accessing the live web, in proxying to the live web, or in
     * 		   maintaining the cache for the live web
     * @throws LiveWebTimeoutException if there is no response from the live
     * 		   web cache before a timeout occurred.
     * @throws IOException for the usual reasons
     * 
     * @see org.archive.wayback.liveweb.LiveWebCache#getCachedResource(java.net.URL, long, boolean)
     * @inheritDoc org.archive.wayback.liveweb.LiveWebCache#getCachedResource
     */
    @Override
    public Resource getCachedResource( URL url, long maxCacheMS, 
            boolean bUseOlder ) 
            throws LiveDocumentNotAvailableException,
            LiveWebCacheUnavailableException, LiveWebTimeoutException, IOException 
    {
        String urlStr = url.toExternalForm();
        
        if (requestPrefix != null)
                urlStr = requestPrefix + urlStr;
        
        HttpHost proxy = new HttpHost( hostConfiguration.getProxyHost(),
                hostConfiguration.getProxyPort() );
        
        // Set socketTimeout and connectionTimeout to 10 seconds.
        RequestConfig reqConf = RequestConfig.custom().setProxy( proxy )
                .setSocketTimeout( 10000 )
                .setConnectTimeout( 10000 )
                .setConnectionRequestTimeout( 10000 )
                .build();
        CloseableHttpClient httpclient = HttpClients.custom().
                setDefaultRequestConfig(reqConf).build();
        HttpGet httpGet = new HttpGet( urlStr );
        
        try 
        {
            // The following line gets robots.txt from live web
            response= httpclient.execute( httpGet );
            
            String httpHeaderStr = "";
            String bodyStr = "";
            
            /* If it fails to get robots.txt (http status code is 404),
               then display contents and don't throw exception
               (socketTimeOutException or connectTimeOutException)
            */
            if ( response.getStatusLine().getStatusCode() == 404 )
            {
                httpHeaderStr = "HTTP/1.0 200 OK\n";
                bodyStr = String.format( "%s\n%s\n",
                        "User-agent: *", "Allow: /" );
            }
            else if ( response.getStatusLine().getStatusCode() == 200 )
            {
                // The following line represents first line in http header
                httpHeaderStr = String.format( "%s %d %s\n", 
                       response.getStatusLine().getProtocolVersion(),
                       response.getStatusLine().getStatusCode(),
                       response.getStatusLine().getReasonPhrase() );

                // Get robots.txt contents and store it into bodyStr
               HttpEntity entity = response.getEntity();
               bodyStr = EntityUtils.toString(entity);
            }
            
            // Get Http Header and store complete http header in httpHeaderStr
            for ( Header header : response.getAllHeaders() )
                httpHeaderStr += header.toString() + "\n";
            
            httpHeaderStr += "\n";
            int length = httpHeaderStr.length() + bodyStr.length();
            
            /*
                Using httpHeaderStr and bodyStr to construct responseStr.
                First line in responseStr should exist.
            */
            
            // TODO: the following line should be enhanced, 
            //       especially the first line in responseStr. 
            String responseStr = String.format( "%s %s %d\n%s%s", urlStr, 
                    "0.0.0.0 10000000000000 text/plain", length,
                    httpHeaderStr, bodyStr );
            
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    responseStr.getBytes() );
            
            // TODO: Should not use ARCRecord
            ARCRecord r = new ARCRecord( bais, "id", 0L, false, false, true );
            ar = ( ArcResource ) ResourceFactory.ARCArchiveRecordToResource( r, null );
            
            if ( ar.getStatusCode() == 502 ) 
            {
                    throw new LiveDocumentNotAvailableException( urlStr );
            } 
            else if ( ar.getStatusCode() == 504 ) 
            {
                    throw new LiveWebTimeoutException( "Timeout:" + urlStr );
            }

            return ar;
        }
        catch( ResourceNotAvailableException e ) 
        {
            throw new LiveDocumentNotAvailableException( urlStr );
        }
        catch( NoHttpResponseException e ) 
        {
            throw new LiveWebCacheUnavailableException( "No Http Response for " +
                    urlStr );
        }
        catch( ConnectException e ) 
        {
            throw new LiveWebCacheUnavailableException( e.getLocalizedMessage() +
                    " : " + urlStr );
        }
        catch ( SocketException e ) 
        {
            throw new LiveWebCacheUnavailableException( e.getLocalizedMessage() +
                    " : " + urlStr );
        }
        catch ( SocketTimeoutException e ) 
        {
            throw new LiveWebTimeoutException( e.getLocalizedMessage() + " : " +
                    urlStr );
        }
        catch( ConnectTimeoutException e ) 
        {
            throw new LiveWebTimeoutException( e.getLocalizedMessage() + " : " +
                    urlStr );
        }
        finally 
        {
            response.close();
        }
    }
    
    /**
     * Sets proxy and port (proxy:port).
     * 
     * @param hostPort to proxy requests through - ex. "localhost:3128"
     */
    public void setProxyHostPort( String hostPort ) 
    {
    	int colonIdx = hostPort.indexOf( ':' );
    	if(colonIdx > 0) 
        {
            String host = hostPort.substring( 0,colonIdx );
            int port = Integer.valueOf( hostPort.substring( colonIdx+1 ) );
            hostConfiguration.setProxy( host, port );
    	}
    }
    
    /** 
     * 
     * @see org.archive.wayback.liveweb.LiveWebCache#shutdown()	 
     */
    @Override
    public void shutdown() 
    {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
}