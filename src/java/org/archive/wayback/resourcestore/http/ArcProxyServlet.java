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
package org.archive.wayback.resourcestore.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackServlet;
import org.archive.wayback.exception.ConfigurationException;
import org.archive.wayback.resourcestore.http.FileLocationDB;

import com.sleepycat.je.DatabaseException;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArcProxyServlet extends WaybackServlet {

	private static final String RANGE_HTTP_HEADER = "Range";
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String CONTENT_TYPE = "application/x-gzip";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private FileLocationDB getLocationDB() throws ServletException {
		try {
			return (FileLocationDB) wayback.getCachedSingleton(
					FileLocationDB.FILE_LOCATION_DB_CLASS);
		} catch (ConfigurationException e) {
			throw new ServletException(e);
		}
	}
	public void destroy() {
		try {
			getLocationDB().shutdownDB();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (ServletException e) {
			e.printStackTrace();
		}
	}
	public void doGet(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException,
			ServletException {

		FileLocationDB locationDB = getLocationDB();
		try {
			String arc = httpRequest.getPathTranslated();
			arc = arc.substring(arc.lastIndexOf('/')+1);
			if(arc.length() == 0) {
				throw new ParseException("no/invalid arc",0);
			}
			String urls[] = locationDB.arcToUrls(arc);
			if(urls == null || urls.length == 0) {
				throw new DatabaseException("Unable to locate("+arc+")");
			}
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
			for (int r = -1; (r = is.read(buffer, 0, BUF_SIZE)) != -1;) {
				os.write(buffer, 0, r);
			}
		} catch (ParseException e) {
			e.printStackTrace();
			httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST,
					e.getMessage());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND,
					e.getMessage());
		}
	}


}
