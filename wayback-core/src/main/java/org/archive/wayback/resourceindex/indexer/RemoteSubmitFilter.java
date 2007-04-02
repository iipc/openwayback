/* RemoteSubmitFilter
 *
 * $Id$
 *
 * Created on 3:57:00 PM Oct 12, 2006.
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
package org.archive.wayback.resourceindex.indexer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.resourceindex.SearchResultSourceFactory;

/**
 * Filter that accepts PUT HTTP requests to insert CDX files into the incoming
 * directory for a local BDBIndex.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteSubmitFilter implements Filter  {

	private final static String HTTP_PUT_METHOD = "PUT";
	private File incoming = null;
	private File tmpIncoming = null;
	
	public void init(FilterConfig c) throws ServletException {

		Properties p = new Properties();
		ServletContext sc = c.getServletContext();
		for (Enumeration e = sc.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, sc.getInitParameter(key));
		}
		for (Enumeration e = c.getInitParameterNames(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			p.put(key, c.getInitParameter(key));
		}
		
		String cfgName = SearchResultSourceFactory.INCOMING_PATH;
		String incomingPath = p.getProperty(cfgName);
		if((incomingPath == null) || incomingPath.length() == 0) {
			throw new ServletException("Invalid or missing " + cfgName +
					" configuration");
		}
		incoming = new File(incomingPath);
		tmpIncoming = new File(incoming,"tmp");
		try {
			ensureDir(incoming);
			ensureDir(tmpIncoming);
		} catch (IOException e) {
			throw new ServletException(e);
		}
	}
	private void ensureDir(File dir) throws IOException {
		if(dir.exists()) {
			if(!dir.isDirectory()) {
				throw new IOException("Path " + dir.getAbsolutePath() +
						"exists but is not a directory.");
			}
		} else {
			if(!dir.mkdirs()) {
				throw new IOException("FAILED mkdir " + dir.getAbsolutePath());
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!handle(request, response)) {
			chain.doFilter(request, response);
		}
	}
	/**
	 * @param request
	 * @param response
	 * @return boolean, true unless something went wrong..
	 * @throws IOException
	 * @throws ServletException
	 */
	protected boolean handle(final ServletRequest request,
			final ServletResponse response) throws IOException,
			ServletException {
		if (!(request instanceof HttpServletRequest)) {
			return false;
		}
		if (!(response instanceof HttpServletResponse)) {
			return false;
		}
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		if(httpRequest.getMethod().equals(HTTP_PUT_METHOD)) {
		
			return handlePut(httpRequest,response);
			
		}
		return false;
	}

	protected boolean handlePut(final HttpServletRequest request,
			final ServletResponse response) throws IOException,
			ServletException {

		String reqURI = request.getRequestURI();
		int lastSlashIdx = reqURI.lastIndexOf("/");
		if (lastSlashIdx == -1) {
			return false;
		}
		String targetFileName = reqURI.substring(lastSlashIdx + 1);
		String tmpFileName = targetFileName + ".tmp";
		File tmpFile = new File(tmpIncoming,tmpFileName);
		File targetFile = new File(incoming, targetFileName);

		int i;
		InputStream input;
		input = request.getInputStream();
		BufferedInputStream in = new BufferedInputStream(input);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		FileWriter out = new FileWriter(tmpFile);

		while ((i = reader.read()) != -1) {
			out.write(i);
		}

		out.close();
		in.close();
		if (!tmpFile.renameTo(targetFile)) {
			throw new IOException("Unable to rename "
					+ tmpFile.getAbsolutePath() + " to "
					+ targetFile.getAbsolutePath());
		}

		PrintWriter outHTML = response.getWriter();
		outHTML.println("done");
		return true;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}
