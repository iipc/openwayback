/* PipeLineServletFilter
 *
 * Created on Oct 20, 2005
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the wayback (crawler.archive.org).
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.cdx.indexer;

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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.cdx.BDBResourceIndex;
import org.archive.wayback.exception.ConfigurationException;

import com.sleepycat.je.DatabaseException;

/**
 * @author brad
 * 
 */
public class PipelineFilter implements Filter {

	/**
	 * configuration name for directory containing index
	 */
	private final static String INDEX_PATH = "resourceindex.indexpath";

	/**
	 * configuration name for BDBJE database name within the db directory
	 */
	private final static String DB_NAME = "resourceindex.dbname";

	
	private final static String HTTP_PUT_METHOD = "PUT";
	
	/**
	 * name of configuration for the JSP that renders the status of the pipeline
	 */
	private final String PIPELINE_STATUS_JSP = "pipeline.statusjsp";

	/**
	 * Name of configuration for flag to activate pipeline thread
	 */
	private final static String RUN_PIPELINE = "indexpipeline.runpipeline";

	/**
	 * IndexPipeline object
	 */
	private IndexPipeline pipeline = null;

	/**
	 * path to the JSP that renders the pipeline status, context relative
	 */
	private String pipelineStatusJsp = null;

	/**
	 * Constructor
	 */
	public PipelineFilter() {
		super();
	}

	public void init(FilterConfig c) throws ServletException {

		boolean pipelineReadonly = true;
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

		pipelineStatusJsp = c.getInitParameter(PIPELINE_STATUS_JSP);
		if ((pipelineStatusJsp == null) || (pipelineStatusJsp.length() <= 0)) {
			throw new ServletException("No config (" + PIPELINE_STATUS_JSP
					+ ")");
		}
		String dbPath = (String) p.get(INDEX_PATH);
		if (dbPath == null || (dbPath.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + INDEX_PATH);
		}
		String dbName = (String) p.get(DB_NAME);
		if (dbName == null || (dbName.length() <= 0)) {
			throw new IllegalArgumentException("Failed to find " + DB_NAME);
		}
		String runPipeline = (String) p.get(RUN_PIPELINE);
		if ((runPipeline != null) && (runPipeline.equals("1"))) {
			pipelineReadonly = false;
		}
		BDBResourceIndex db;
		try {
			db = new BDBResourceIndex(dbPath,dbName,pipelineReadonly);
		} catch (DatabaseException e1) {
			throw new ServletException(e1);
		}
		pipeline = new IndexPipeline(db,!pipelineReadonly);
		try {
			pipeline.init(p);
		} catch (ConfigurationException e) {
			throw new ServletException(e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
		pipeline.shutdown();
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
			
		} else {
			PipelineStatus status = pipeline.getStatus();

			request.setAttribute("pipelinestatus", status);
			RequestDispatcher dispatcher = httpRequest
					.getRequestDispatcher(pipelineStatusJsp);

			dispatcher.forward(request, response);
		}
		return true;
	}

	protected boolean handlePut(final HttpServletRequest request,
			final ServletResponse response) throws IOException,
			ServletException {
		
	     PrintWriter outHTML = response.getWriter();
	     outHTML.println("done");
	     String reqURI = request.getRequestURI();
	     int lastSlashIdx = reqURI.lastIndexOf("/");
	     if(lastSlashIdx == -1) {
	    	 return false;
	     }
	     String targetFileName = reqURI.substring(lastSlashIdx+1);
	     String tmpFileName = targetFileName + ".tmp";
	     File tmpFile = new File(pipeline.getIndexingDir(),tmpFileName);
	     File targetFile = new File(pipeline.getToBeMergedDir(),targetFileName);
	     
	     
        int i;
        InputStream input;
        input = request.getInputStream();
        BufferedInputStream in = 
           new BufferedInputStream(input);
        BufferedReader reader = 
           new BufferedReader(
             new InputStreamReader(in));
        FileWriter out = 
           new FileWriter(tmpFile);
    
        while ((i = reader.read()) != -1) 
        {
          out.write(i);
        }
    
        out.close();
        in.close();
        if(!tmpFile.renameTo(targetFile)) {
        	throw new IOException("Unable to rename " + 
        			tmpFile.getAbsolutePath() + " to " +
        			targetFile.getAbsolutePath());
        }
		
		return true;
	}
}
