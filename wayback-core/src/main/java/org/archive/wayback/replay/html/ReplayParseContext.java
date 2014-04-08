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
package org.archive.wayback.replay.html;

import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.util.htmllex.ParseContext;

public class ReplayParseContext extends ParseContext {
	private static final String MAILTO_PREFIX = "mailto:";
	public static final String JAVASCRIPT_PREFIX = "javascript:";
	public static final String DATA_PREFIX = "data:";


	private ContextResultURIConverterFactory uriConverterFactory = null;
	private String datespec = null;
	private JSPExecutor jspExec = null;
	private OutputStream outputStream = null;
	private Map<String,ResultURIConverter> converters = null;
	private String outputCharset;
	private int phase = -1;
	private int jsBlockCount = -1;
	private CaptureSearchResult result;
	private boolean rewriteHttpsOnly;

	public ReplayParseContext(ContextResultURIConverterFactory uriConverterFactory,
			URL baseUrl, String datespec) {

		this.uriConverterFactory = uriConverterFactory;
		super.setBaseUrl(baseUrl);
		this.datespec = datespec;
		this.converters = new HashMap<String,ResultURIConverter>();
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}
	public int getPhase() {
		return phase;
	}
	
	public void setRewriteHttpsOnly(boolean rewriteHttpsOnly)
	{
		this.rewriteHttpsOnly = rewriteHttpsOnly;
	}
		
	public boolean isRewriteSupported(String url)
	{
		if (!rewriteHttpsOnly) {
			return true;
		}
		
	    return url.startsWith(WaybackConstants.HTTPS_URL_PREFIX);
	}

	/**
	 * @return the converters
	 */
	public Map<String, ResultURIConverter> getConverters() {
		return converters;
	}
	
	public CaptureSearchResult getCaptureSearchResult()
	{
		return result;
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
	

	// TODO: inline - used only in one place, no readability benefit.
	private ResultURIConverter makeConverter(String flags) {
		return uriConverterFactory.getContextConverter(flags);
	}
	/**
	 * returns {@link ResultURIConverter} for resource context <code>flags</code>.
	 * @param flags resource context indicator such as "{@code cs_}", "{@code im_}".
	 * @return ResultURIConverter for translating URL
	 * @see org.archive.wayback.archivalurl.ArchivalUrlSpecialContextReusltURIConverter
	 */
	public ResultURIConverter getConverter(String flags) {
		// TODO: caching should be a responsibility of ContextResultURIConverterFactory.
		// but it's a API-breaking change as converters is exposed through getter.
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
		// if we get an empty string, just return it:
		if(url.length() == 0) {
			return url;
		}
	    if(url.startsWith(JAVASCRIPT_PREFIX) || url.startsWith(MAILTO_PREFIX)) {
	    	return url;
	    }
	    // XXX duplicated check for MAILTO_PREFIX??
	    if(url.startsWith(DATA_PREFIX) || url.startsWith(MAILTO_PREFIX)) {
	    	return url;
	    }
	    if (!isRewriteSupported(url)) {
	    	return url;
	    }
	    // first make url into absolute, taking BASE into account.
	    url = super.contextualizeUrl(url);
	    // XXX do this in getConverter
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
		if (jspExec != null && jspExec.getUiResults() != null) {
			result = jspExec.getUiResults().getResult();
		}
	}

	/**
	 * @return the datespec
	 */
	public String getDatespec() {
		return datespec;
	}

	/**
	 * @param datespec the datespec to set
	 */
	public void setDatespec(String datespec) {
		this.datespec = datespec;
	}
	
	public void incJSBlockCount() {
		jsBlockCount++;
	}

	public int getJSBlockCount() {
		return jsBlockCount;
	}
}
