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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

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

	private static final String RANGE_HTTP_HEADER = "Range";
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String CONTENT_TYPE = "application/x-gzip";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ResourceFileLocationDB locationDB = null;
	
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		String name = httpRequest.getRequestURI();
		name = name.substring(name.lastIndexOf('/')+1);
		if(name.length() == 0) {
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"no/invalid name");
		} else {
			
			String urls[] = locationDB.nameToUrls(name);

			if(urls == null || urls.length == 0) {
				
				httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND,
						"Unable to locate("+name+")");
			} else {

				String urlString = urls[0];
				String rangeHeader = httpRequest.getHeader(RANGE_HTTP_HEADER);
				URL url = new URL(urlString);
				URLConnection conn = url.openConnection();
				if(rangeHeader != null) {
					conn.addRequestProperty(RANGE_HTTP_HEADER,rangeHeader);
				}
				InputStream is = conn.getInputStream();
				httpResponse.setStatus(HttpServletResponse.SC_OK);
				String typeHeader = conn.getHeaderField(CONTENT_TYPE_HEADER);
				if(typeHeader == null) {
					typeHeader = CONTENT_TYPE;
				}
				httpResponse.setContentType(typeHeader);
				OutputStream os = httpResponse.getOutputStream();
				int BUF_SIZE = 4096;
				byte[] buffer = new byte[BUF_SIZE];
				try {
					for(int r = -1; (r = is.read(buffer, 0, BUF_SIZE)) != -1;) {
						os.write(buffer, 0, r);
					}
				} finally {
					is.close();
				}
			}
		}
		return true;
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
}
