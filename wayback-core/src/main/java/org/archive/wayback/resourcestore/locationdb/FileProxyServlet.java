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
package org.archive.wayback.resourcestore.locationdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.archive.util.anvl.ANVLRecord;
import org.archive.wayback.util.http.HttpRequestMessage;
import org.archive.wayback.util.http.HttpResponse;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

/**
 * ServletRequestContext interface which uses a ResourceFileLocationDB to 
 * reverse proxy an incoming HTTP request for a file by name to it's actual 
 * back-end location. This will also forward HTTP byte range requests to the
 * final location.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileProxyServlet extends AbstractRequestHandler {
	private static final Logger LOGGER = Logger.getLogger(FileProxyServlet.class
			.getName());

	private static final int BUF_SIZE = 4096;
	private static final String RANGE_HTTP_HEADER = "Range";
	private static final String DEFAULT_CONTENT_TYPE = "application/x-gzip";
	private static final String HEADER_BYTES_PREFIX = "bytes=";
	private static final String HEADER_BYTES_SUFFIX= "-";

	private static final String FILE_REGEX = "/([^/]*)$";
	private static final String FILE_OFFSET_REGEX = "/([^/]*)/(\\d*)$";
	
	private static final Pattern FILE_PATTERN = 
		Pattern.compile(FILE_REGEX);
	private static final Pattern FILE_OFFSET_PATTERN = 
		Pattern.compile(FILE_OFFSET_REGEX);

	private static final long serialVersionUID = 1L;

	private ResourceFileLocationDB locationDB = null;

	private int socketTimeoutMs = 5000;

	private int connectTimeoutMs = 1000;
	
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		ResourceLocation location = parseRequest(httpRequest);
		if(location == null) {
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
			"no/invalid name");
		} else {

			String urls[] = locationDB.nameToUrls(location.getName());

			if(urls == null || urls.length == 0) {
				
				LOGGER.warning("No locations for " + location.getName());
				httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Unable to locate("+ location.getName() +")");
			} else {
				
				DataSource ds = null;
				for(String urlString : urls) {
					try {
						ds = locationToDataSource(urlString, 
								location.getOffset());
						if(ds != null) {
							break;
						}
					} catch(IOException e) {
						LOGGER.warning("failed proxy of " + urlString + " " +
								e.getLocalizedMessage());
					}
				}
				if(ds == null) {
					LOGGER.warning("No successful locations for " +
							location.getName());
					httpResponse.sendError(HttpServletResponse.SC_BAD_GATEWAY,
							"failed proxy of ("+ location.getName() +")");
					
				} else {
					httpResponse.setStatus(HttpServletResponse.SC_OK);
					// BUGBUG: this will be broken for non compressed data...
					httpResponse.setContentType(ds.getContentType());
					httpResponse.setBufferSize(BUF_SIZE);
					ds.copyTo(httpResponse.getOutputStream());
				}
			}
		}
		return true;
	}

	private DataSource locationToDataSource(String location, long offset)
		throws IOException {
		DataSource ds = null;
		if(location.startsWith("http://")) {
			URL url = new URL(location);
			String hostname = url.getHost();
			int port = url.getPort();
			if(port == -1) {
				port = 80;
			}
			byte GET[] = "GET".getBytes();
			byte HTTP11[] = "HTTP/1.1".getBytes();
			InetAddress addr = InetAddress.getByName(hostname);
			HttpRequestMessage requestMessage = new HttpRequestMessage(
					GET,url.getFile().getBytes(),HTTP11);
			ANVLRecord headers = new ANVLRecord();
			headers.addLabelValue("Host", hostname);
			
			
			if(offset != 0) {
				headers.addLabelValue(RANGE_HTTP_HEADER, 
						HEADER_BYTES_PREFIX + String.valueOf(offset) + 
							HEADER_BYTES_SUFFIX);
			}
			InetSocketAddress sockAddr = new InetSocketAddress(addr,port);
			Socket socket = new Socket();
			socket.setSoTimeout(socketTimeoutMs);
			socket.setReceiveBufferSize(BUF_SIZE);

			socket.connect(sockAddr, connectTimeoutMs);
			OutputStream socketOut = socket.getOutputStream();
			InputStream socketIn = socket.getInputStream();
			socketOut.write(requestMessage.getBytes(true));
			socketOut.write(headers.getUTF8Bytes());
			socketOut.flush();
			HttpResponse response = HttpResponse.load(socketIn);
			String contentType = response.getHeaders().asMap().get("Content-Type");
			if(contentType == null) {
				contentType = "application/unknown";
			}
			String xferEncoding = response.getHeaders().asMap().get("Transfer-Encoding");
			
			if(xferEncoding != null) {
				if(xferEncoding.equals("chunked")) {
					socketIn = new ChunkedInputStream(socketIn);
				}
			}

			ds = new URLDataSource(socketIn,contentType);

		} else {
			// assume a local file path:
			File f = new File(location);
			if(f.isFile() && f.canRead()) {
				long size = f.length();
				if(size < offset) {
					throw new IOException("short file " + location + " cannot" +
							" seek to offset " + offset);
				}
				RandomAccessFile raf = new RandomAccessFile(f,"r");
				raf.seek(offset);
				// BUGBUG: is it compressed?
				ds = new FileDataSource(raf,DEFAULT_CONTENT_TYPE);
				
			} else {
				throw new IOException("No readable file at " + location);
			}
			
		}

		return ds;
	}
	
	private ResourceLocation parseRequest(HttpServletRequest request) {
		ResourceLocation location = null;
		
		String path = request.getRequestURI();
		Matcher fo = FILE_OFFSET_PATTERN.matcher(path);
		if(fo.find()) {
			location = new ResourceLocation(fo.group(1),
					Long.parseLong(fo.group(2)));
		} else {
			fo = FILE_PATTERN.matcher(path);
			if(fo.find()) {
				String rangeHeader = request.getHeader(RANGE_HTTP_HEADER);
				if(rangeHeader != null) {
					if(rangeHeader.startsWith(HEADER_BYTES_PREFIX)) {
						rangeHeader = rangeHeader.substring(
								HEADER_BYTES_PREFIX.length());
						if(rangeHeader.endsWith(HEADER_BYTES_SUFFIX)) {
							rangeHeader = rangeHeader.substring(0,
									rangeHeader.length() - 
									HEADER_BYTES_SUFFIX.length());
						}
					}
					location = new ResourceLocation(fo.group(1),
							Long.parseLong(rangeHeader));
				} else {
					location = new ResourceLocation(fo.group(1));
				}
			}
		}
		return location;
	}
	
	/**
	 * @return the locationDB
	 */
	public ResourceFileLocationDB getLocationDB() {
		return locationDB;
	}

	/**
	 * @param locationDB the locationDB to set
	 */
	public void setLocationDB(ResourceFileLocationDB locationDB) {
		this.locationDB = locationDB;
	}
	
	/**
	 * @return the socketTimeoutMs
	 */
	public int getSocketTimeoutMs() {
		return socketTimeoutMs;
	}

	/**
	 * @param socketTimeoutMs the socketTimeoutMs to set
	 */
	public void setSocketTimeoutMs(int socketTimeoutMs) {
		this.socketTimeoutMs = socketTimeoutMs;
	}

	/**
	 * @return the connectTimeoutMs
	 */
	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	/**
	 * @param connectTimeoutMs the connectTimeoutMs to set
	 */
	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	private class ResourceLocation {
		private String name = null;
		private long offset = 0;
		public ResourceLocation(String name, long offset) {
			this.name = name;
			this.offset = offset;
		}
		public ResourceLocation(String name) {
			this(name,0);
		}
		public String getName() {
			return name;
		}
		public long getOffset() {
			return offset;
		}
	}
	
	private interface DataSource {
		public void copyTo(OutputStream os) throws IOException;
		public String getContentType();
	}
	private class FileDataSource implements DataSource {
		private RandomAccessFile raf = null;
		private String contentType = null;
		public FileDataSource(RandomAccessFile raf, String contentType) {
			this.raf = raf;
			this.contentType = contentType;
		}
		public String getContentType() {
			return contentType;
		}
		public void copyTo(OutputStream os) throws IOException {
			byte[] buffer = new byte[BUF_SIZE];
			try {
				int r = -1;
				while((r = raf.read(buffer, 0, BUF_SIZE)) != -1) {
					os.write(buffer, 0, r);
				}
			} finally {
				raf.close();
			}
		}
	}
	private class URLDataSource implements DataSource {
		private InputStream is = null;
		private String contentType = null;
		public URLDataSource(InputStream is,String contentType) {
			this.is = is;
			this.contentType = contentType;
		}
		public String getContentType() {
			return contentType;
		}
		public void copyTo(OutputStream os) throws IOException {
			byte[] buffer = new byte[BUF_SIZE];
			try {
				int r = -1;
				while((r = is.read(buffer, 0, BUF_SIZE)) != -1) {
					os.write(buffer, 0, r);
				}
			} finally {
				is.close();
			}
		}
	}
}
