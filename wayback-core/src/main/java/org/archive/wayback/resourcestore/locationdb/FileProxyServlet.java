/* ArcProxyServlet
 *
 * $Id$
 *
 * Created on 6:19:54 PM Aug 10, 2006.
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
package org.archive.wayback.resourcestore.locationdb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.webapp.ServletRequestContext;

/**
 * ServletRequestContext interface which uses a ResourceFileLocationDB to 
 * reverse proxy an incoming HTTP request for a file by name to it's actual 
 * back-end location. This will also forward HTTP byte range requests to the
 * final location.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class FileProxyServlet extends ServletRequestContext {
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
			URLConnection conn = url.openConnection();
			if(offset != 0) {
				conn.addRequestProperty(RANGE_HTTP_HEADER,
						HEADER_BYTES_PREFIX + String.valueOf(offset) + 
						HEADER_BYTES_SUFFIX);
			}

			ds = new URLDataSource(conn.getInputStream(),conn.getContentType());
			
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
