/* ReplayParseContext
 *
 * $Id$
 *
 * Created on 12:36:59 PM Nov 5, 2009.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of wayback.
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
package org.archive.wayback.replay.html;

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.util.htmllex.ParseContext;

public class ReplayParseContext extends ParseContext {
	private ContextResultURIConverterFactory uriConverterFactory = null;
	private String datespec = null;
	private JSPExecutor jspExec = null;
	private OutputStream outputStream = null;
	private Map<String,ResultURIConverter> converters = null;
	private String outputCharset;
	private int phase = -1;

	public ReplayParseContext(ContextResultURIConverterFactory uriConverterFactory,
			URL baseUrl, String datespec) {

		this.uriConverterFactory = uriConverterFactory;
		this.baseUrl = baseUrl;
		this.datespec = datespec;
		converters = new HashMap<String,ResultURIConverter>();
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}
	public int getPhase() {
		return phase;
	}

	/**
	 * @return the converters
	 */
	public Map<String, ResultURIConverter> getConverters() {
		return converters;
	}

	/**
	 * @param converters the converters to set
	 */
	public void setConverters(Map<String, ResultURIConverter> converters) {
		this.converters = converters;
	}
	public void addConverter(String flag, ResultURIConverter converter) {
		converters.put(flag, converter);
	}
	

	private ResultURIConverter makeConverter(String flags) {
		return uriConverterFactory.getContextConverter(flags);
	}
	public ResultURIConverter getConverter(String flags) {
		ResultURIConverter converter = converters.get(flags);
		if(converter == null) {
			converter = makeConverter(flags);
			converters.put(flags,converter);
		}
		return converter;
	}
	
	public String contextualizeUrl(String url) {
		return contextualizeUrl(url,"");
	}
	public String contextualizeUrl(String url, String flags) {
	    if(url.startsWith("javascript:")) {
	    	return url;
	    }
	    url = super.contextualizeUrl(url);
	    if(flags == null) {
	    	flags = "";
	    }
	    ResultURIConverter converter = getConverter(flags);
		return converter.makeReplayURI(datespec, url); 
	}
	
	
	/**
	 * @return the charset
	 */
	public String getOutputCharset() {
		return outputCharset;
	}

	/**
	 * @param outputCharset the outputCharset to set
	 */
	public void setOutputCharset(String outputCharset) {
		this.outputCharset = outputCharset;
	}
	
	/**
	 * @return the outputStream
	 */
	public OutputStream getOutputStream() {
		return outputStream;
	}

	/**
	 * @param outputStream the outputStream to set
	 */
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	/**
	 * @return the jspExec
	 */
	public JSPExecutor getJspExec() {
		return jspExec;
	}
	/**
	 * @param jspExec the jspExec to set
	 */
	public void setJspExec(JSPExecutor jspExec) {
		this.jspExec = jspExec;
	}
}
