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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

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
import org.archive.wayback.PropertyConfigurable;
import org.archive.wayback.exception.ConfigurationException;
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
public class URLCacher implements PropertyConfigurable {
	private static final Logger LOGGER = Logger.getLogger(
			URLCacher.class.getName());

	private static final String CACHE_PATH = "liveweb.tmp.dir";
	
	protected File tmpDir = null;

	/* (non-Javadoc)
	 * @see org.archive.wayback.PropertyConfigurable#init(java.util.Properties)
	 */
	public void init(Properties p) throws ConfigurationException {
		String cachePath = (String) p.get(CACHE_PATH);
		if (cachePath == null || cachePath.length() <= 0) {
			throw new ConfigurationException("Failed to find " + CACHE_PATH);
		}
		tmpDir = new File(cachePath);
		if(!tmpDir.exists()) {
			if(!tmpDir.mkdirs()) {
				throw new ConfigurationException("Unable to mkdirs(" + 
						tmpDir.getAbsolutePath() + ")");
			}
		} else {
			if(!tmpDir.isDirectory()) {
				throw new ConfigurationException("Something non-dir-ish at(" +
						tmpDir.getAbsolutePath() + ")");
			}
		}
	}

	private File getTempFile() {
		String tmpName;
		try {
			File foo = File.createTempFile("robot-tmp-",null);
			tmpName = foo.getName();
			foo.delete();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			tmpName = "oops";
			e.printStackTrace();
		}
		File tmpFile = new File(tmpDir,tmpName);
		return tmpFile;
	}
	
	
	/**
	 * Retrieve urlString, and store using ARCWriter, returning 
	 * ARCLocation where the document was stored.
	 *
	 * @param writer 
	 * @param urlString
	 * @return ARCLocation where document was stored
	 * @throws LiveDocumentNotAvailableException 
	 * @throws URIException 
	 * @throws IOException if something internal went wrong.
	 */
	public ARCLocation cache(ARCWriter writer, String urlString)
		throws LiveDocumentNotAvailableException, URIException, IOException {
		ARCLocation location = null;
		OutputStream os = null;
		HttpClient http = new HttpClient();
		File file = getTempFile();
		if (file.exists()) {
			file.delete();
		}
		os = new FileOutputStream(file);
		ExtendedGetMethod method = new ExtendedGetMethod(os);
		method.setURI(new LaxURI(urlString, true));
		try {
			http.setHttpConnectionManager(new IPHttpConnectionManager());
			http.getHttpConnectionManager().getParams().setConnectionTimeout(1000);
			http.getHttpConnectionManager().getParams().setSoTimeout(1000);
			http.executeMethod(method);
			os.close();
		} catch (HttpException e) {
			e.printStackTrace();
			file.delete();
			throw new LiveDocumentNotAvailableException(urlString);
		} catch(UnknownHostException e) {
			file.delete();
			throw new LiveDocumentNotAvailableException(urlString);
		} catch (IOException e) {
			e.printStackTrace();
			file.delete();
			throw e;
		}
		try {
			FileInputStream fis = new FileInputStream(file);
			int len = (int) file.length();
			String mime = method.getMime();
			String ip = method.getRemoteIP();
			Date captureDate = method.getCaptureDate();
			
			writer.checkSize();
			String arcPathTmp = writer.getFile().getAbsolutePath();
			final long oldOffset = writer.getPosition();

			writer.write(urlString,mime,ip,captureDate.getTime(),len,fis);
			fis.close();
			
			final long newOffset = writer.getPosition();
			
			LOGGER.info("Stored ("+urlString+") in ("+arcPathTmp+") at ("+newOffset+") old("+oldOffset+") from("+file.getAbsolutePath()+")");
			final String arcPath = arcPathTmp;
			//writer.close();
			
			location = new ARCLocation() {
				private String filename = arcPath;

				private long offset = oldOffset;

				public String getName() {
					return this.filename;
				}

				public long getOffset() {
					return this.offset;
				}
			};

		} catch (FileNotFoundException e) {
			file.delete();
			throw e;
		} catch (IOException e) {
			file.delete();
			throw e;
		} finally {
			file.delete();
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

		for(int k = 1; k < args.length; k++) {
			URLCacher uc = null;
			try {
				Properties p = new Properties();
				p.setProperty(CACHE_PATH, arcDir.getAbsolutePath());
				uc = new URLCacher();
				uc.init(p);
			} catch (ConfigurationException e) {
				e.printStackTrace();
				System.exit(1);
			}
			try {
				url = new URL(args[k]);
			} catch (MalformedURLException e1) {
				e1.printStackTrace();
				continue;
			}
			try {
				uc.cache(writer, url.toString());
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
			// TODO Auto-generated catch block
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
		private String mime = "";
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
				// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
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

}
