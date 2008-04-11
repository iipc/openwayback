/* URLCacher
 *
 * $Id$
 *
 * Created on 5:30:31 PM Mar 12, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.liveweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URIException;
import org.archive.io.arc.ARCLocation;
import org.archive.io.arc.ARCWriter;
import org.archive.net.LaxURI;
import org.archive.wayback.exception.LiveDocumentNotAvailableException;

/**
 * Class for performing an HTTP GET request, and storing all related info
 * required to create a valid ARC Record. This info is also actually stored in
 * an ARC file via an ARCWriter. This should leverage more Heritrix fetcher code
 * but because the Heritrix settings system is tightly coupled with the fetcher
 * code, we'll try to limp by with this class until it gets untangled. 
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class URLCacher {
	private static final Logger LOGGER = Logger.getLogger(
			URLCacher.class.getName());
	
	private static final String CACHE_PATH = "liveweb.tmp.dir";
	
	protected File tmpDir = null;
	@SuppressWarnings("unchecked")
	private final ThreadLocal tl = new ThreadLocal() {
        protected synchronized Object initialValue() {
    		HttpClient http = new HttpClient();
    		IPHttpConnectionManager manager = new IPHttpConnectionManager();
    		manager.getParams().setConnectionTimeout(10000);
    		manager.getParams().setSoTimeout(10000);
    		http.setHttpConnectionManager(manager);
			return http;
        }
    };
    private HttpClient getHttpClient() {
        return (HttpClient) tl.get();
    }

	private File getTmpFile() {
		String tmpName;
		File tmpFile;
		try {
			tmpFile = File.createTempFile("robot-tmp-",null);
			tmpName = tmpFile.getName();
			tmpFile.delete();
		} catch (IOException e) {
			tmpName = "oops" + Thread.currentThread().getName();
			e.printStackTrace();
		}
		tmpFile = new File(tmpDir,tmpName);
		if (tmpFile.exists()) {
			tmpFile.delete();
		}
		return tmpFile;
	}
	
	protected ExtendedGetMethod urlToFile(String urlString, File file) 
		throws LiveDocumentNotAvailableException, URIException, IOException {
		
		HttpClient http = getHttpClient();
		OutputStream os = new FileOutputStream(file);
		ExtendedGetMethod method = new ExtendedGetMethod(os);
		LaxURI lURI = new LaxURI(urlString,true);
		method.setURI(lURI);
		try {
			int code = http.executeMethod(method);
			os.close();
			// TODO: Constant 200
			if(code != 200) {
				throw new LiveDocumentNotAvailableException(urlString);
			}
		} catch (HttpException e) {
			e.printStackTrace();
			throw new LiveDocumentNotAvailableException(urlString);
		} catch(UnknownHostException e) {
			LOGGER.info("Unknown host for URL " + urlString);
			throw new LiveDocumentNotAvailableException(urlString);
		} catch(ConnectTimeoutException e) {
			LOGGER.info("Connection Timeout for URL " + urlString);
			throw new LiveDocumentNotAvailableException(urlString);			
		} catch(NoRouteToHostException e) {
			LOGGER.info("No route to host for URL " + urlString);
			throw new LiveDocumentNotAvailableException(urlString);						
		} catch(ConnectException e) {
			LOGGER.info("ConnectException URL " + urlString);
			throw new LiveDocumentNotAvailableException(urlString);						
		}
		LOGGER.info("Stored " + urlString + " in " + file.getAbsolutePath());
		return method;
	}
	
	private ARCLocation storeFile(File file, ARCWriter writer, String url,
			ExtendedGetMethod method) throws IOException {
		
		FileInputStream fis = new FileInputStream(file);
		int len = (int) file.length();
		String mime = method.getMime();
		String ip = method.getRemoteIP();
		Date captureDate = method.getCaptureDate();
			
		writer.checkSize();
		final long arcOffset = writer.getPosition();
		final String arcPath = writer.getFile().getAbsolutePath();

		writer.write(url,mime,ip,captureDate.getTime(),len,fis);
		writer.checkSize();
//		long newSize = writer.getPosition();
//		long oSize = writer.getFile().length();
		LOGGER.info("Wrote " + url + " at " + arcPath + ":" + arcOffset);
		fis.close();
		
		return new ARCLocation() {
			private String filename = arcPath;
			private long offset = arcOffset;

			public String getName() { return this.filename; }

			public long getOffset() { return this.offset;   }
		};
	}

	/**
	 * Retrieve urlString, and store using ARCWriter, returning 
	 * ARCLocation where the document was stored.
	 *
	 * @param cache 
	 * @param urlString
	 * @return ARCLocation where document was stored
	 * @throws LiveDocumentNotAvailableException 
	 * @throws URIException 
	 * @throws IOException if something internal went wrong.
	 */
	public ARCLocation cache(ARCCacheDirectory cache, String urlString)
		throws LiveDocumentNotAvailableException, IOException, URIException {

		// localize URL
		File tmpFile = getTmpFile();
		ExtendedGetMethod method;
		try {
			method = urlToFile(urlString,tmpFile);
		} catch (LiveDocumentNotAvailableException e) {
			LOGGER.info("Attempted to get " + urlString + " failed...");
			tmpFile.delete();
			throw e;
		} catch (URIException e) {
			tmpFile.delete();
			throw e;
		} catch (IOException e) {
			tmpFile.delete();
			throw e;
		}
		
		// store URL
		ARCLocation location = null;
		ARCWriter writer = null;
		try {
			writer = cache.getWriter();
			location = storeFile(tmpFile, writer, urlString, method);
		} catch(IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			if(writer != null) {
				cache.returnWriter(writer);
			}
			tmpFile.delete();
		}
		return location;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int DEFAULT_MAX_ARC_FILE_SIZE = 1024 * 1024 * 100;
		File arcDir = new File(args[0]);
		URL url;
		if(!arcDir.isDirectory()) {
			arcDir.mkdir();
		}
		File [] files = {arcDir};
		boolean compress = true;
		ARCWriter writer = new ARCWriter(new AtomicInteger(), 
				Arrays.asList(files), "test", compress,
				DEFAULT_MAX_ARC_FILE_SIZE);
		Properties p = new Properties();
		p.setProperty(ARCCacheDirectory.LIVE_WEB_ARC_DIR, args[0]);
		p.setProperty(ARCCacheDirectory.LIVE_WEB_ARC_PREFIX, "test");
		p.setProperty(CACHE_PATH, arcDir.getAbsolutePath());

		URLCacher uc = new URLCacher();
		ARCCacheDirectory cache = new ARCCacheDirectory();
//		try {
////			cache.init(p);
////			uc.init(p);
//		} catch (ConfigurationException e) {
//			e.printStackTrace();
//			System.exit(1);
//		}
		for(int k = 1; k < args.length; k++) {
			try {
				url = new URL(args[k]);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				continue;
			}
			try {
				uc.cache(cache, url.toString());
			} catch (URIException e) {
				e.printStackTrace();
			} catch (LiveDocumentNotAvailableException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Get method which stores the entire HTTP response: message, headers & body
	 * in the OutputStream provided, and also provides access to the data needed
	 * to generate an ARC record: IP, Date and Mime
	 */
	private class ExtendedGetMethod extends HttpMethodBase {

		private String remoteIP = "";
		private Date captureDate = null;
		private String mime = "unk";
		private OutputStream os = null;
		
		/**
		 * Constructor
		 * 
		 * @param os
		 */
		public ExtendedGetMethod(OutputStream os) {
			super();
			this.os = os;
		}
		
		/* (non-Javadoc)
		 * @see org.apache.commons.httpclient.HttpMethodBase#getName()
		 */
		public String getName() {
			return "GET";
		}

		protected void processStatusLine(HttpState state, HttpConnection conn) {
			captureDate = new Date();
			IPStoringHttpConnection bhc = (IPStoringHttpConnection) conn;
			remoteIP = bhc.getRemoteIP();
			try {
				String statusLine = this.getStatusLine().toString() + "\r\n"; 
				os.write(statusLine.getBytes());
			} catch (IOException e) {
				// TODO hrm..?
				e.printStackTrace();
			}
		}

		protected void processResponseBody(HttpState state, HttpConnection conn) {
			try {
				
				// copy the HTTP Headers...
				Header headers[] = this.getResponseHeaders();
				for (int i = 0; i < headers.length; i++) {
					if(headers[i].getName().equals("Content-Type")) {
						mime = headers[i].getValue();
					}
					os.write(headers[i].toExternalForm().getBytes());
				}
				os.write(new String("\r\n").getBytes());
				
				// now copy the whole response body:
				
				InputStream is = this.getResponseStream();
				final int BUFFER_SIZE = 1024 * 4;
				byte[] buffer = new byte[BUFFER_SIZE];
				while (true) {
					int x = is.read(buffer);
					if (x == -1) {
						break;
					}
					os.write(buffer, 0, x);
				}
				//is.close();
				os.close();

			} catch (IOException e) {
				// TODO don't eat it
				e.printStackTrace();
			}
		}

		/**
		 * @return Returns the captureDate.
		 */
		public Date getCaptureDate() {
			return captureDate;
		}

		/**
		 * @return Returns the mime.
		 */
		public String getMime() {
			return mime;
		}
		
		/**
		 * @return Returns the remoteIP.
		 */
		public String getRemoteIP() {
			return remoteIP;
		}

	}

	/**
	 * HttpConnectionManager that returns IPHttpConnection objects, for
	 * accessing the IP address
	 */
	private class IPHttpConnectionManager extends SimpleHttpConnectionManager {
		public HttpConnection getConnection(HostConfiguration hostConfiguration) {
			IPStoringHttpConnection conn = new IPStoringHttpConnection(hostConfiguration);
	        conn.setHttpConnectionManager(this);
	        conn.getParams().setDefaults(this.getParams());
	        return conn;
		}

		public HttpConnection getConnectionWithTimeout(
				HostConfiguration hostConfiguration, long timeout) {
			// TODO: is this  lying? have we really set the time out?
			IPStoringHttpConnection conn = new IPStoringHttpConnection(hostConfiguration);
	        conn.setHttpConnectionManager(this);
	        conn.getParams().setDefaults(this.getParams());
	        return conn;
		}

		public HttpConnection getConnection(
				HostConfiguration hostConfiguration, long timeout) {

			return new IPStoringHttpConnection(hostConfiguration);
		}
	    public void releaseConnection(HttpConnection conn) {
	        // ensure connection is closed
	        conn.close();
	        InputStream lastResponse = conn.getLastResponseInputStream();
	        if (lastResponse != null) {
	            conn.setLastResponseInputStream(null);
	            try {
	                lastResponse.close();
	            } catch (IOException ioe) {
	                //FIX ME: badness - close to force reconnect.
	                conn.close();
	            }
	        }
	    }
	}

	/**
	 * HttpConnection that allows access to the IP address which was
	 * used for the connection.
	 */
	private class IPStoringHttpConnection extends HttpConnection {

		/**
		 * @param hc
		 */
		public IPStoringHttpConnection(HostConfiguration hc) {
			super(hc);
		}
		/**
		 * @return the remote IP address that was connected to, as a String  
		 */
		public String getRemoteIP() {
			return getSocket().getInetAddress().getHostAddress();
		}
	}

	/**
	 * @return the tmpDir
	 */
	public String getTmpDir() {
		if(tmpDir == null) {
			return null;
		}
		return tmpDir.getAbsolutePath();
	}

	/**
	 * @param tmpDir the tmpDir to set
	 */
	public void setTmpDir(String tmpDir) {
		this.tmpDir = new File(tmpDir);
		if(!this.tmpDir.exists()) {
			this.tmpDir.mkdirs();
		}
	}

}
