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
package org.archive.wayback.resourceindex.updater;

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

import org.archive.wayback.util.ByteOp;

/**
 * Filter that accepts PUT HTTP requests to insert CDX files into the incoming
 * directory for a local BDBIndex.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class RemoteSubmitFilter implements Filter  {

	private final static String INCOMING_PATH = "config-tmp.incoming";
	private final static String HTTP_PUT_METHOD = "PUT";
	private File incoming = null;
	private File tmpIncoming = null;
	
	// TODO: get rid of this
	@SuppressWarnings("unchecked")
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
		
		String cfgName = INCOMING_PATH;
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
		BufferedReader reader = new BufferedReader(new InputStreamReader(in,ByteOp.UTF8));
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
