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

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.util.htmllex.ParseContext;

/**
 * {@code ReplayParseContext} holds context information shared among
 * replay rewriter components.
 * <p>2014-05-02 small behavior/interface changes:
 * <ul>
 * <li>{@link #setJspExec(JSPExecutor)} no longer copies {@code CaptureSearchResult}
 * object from its {@code UIResults} object to {@code result} member. Use new constructor
 * taking {@code CaptureSearchResult} object (recommended), or use
 * {@link #setCaptureSearchResult(CaptureSearchResult)} method.</li>
 * <li>
 * </ul>
 * TODO: consider replacing {@code CaptureSearchResult} reference with {@code Capture}.
 */
public class ReplayParseContext extends ParseContext {
	private static final String MAILTO_PREFIX = "mailto:";
	public static final String JAVASCRIPT_PREFIX = "javascript:";
	public static final String DATA_PREFIX = "data:";
	public static final String ANCHOR_PREFIX = "#";

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

	/**
	 * Constructs {@code ReplayParseContext} for rewriting a resource
	 * represented by {@code result}.
	 * <p>Initializes {@code baseUrl} and {@code datespec} from {@code result}'s
	 * {@code originalUrl} and {@code captureTimestamp}, respectively.</p>
	 * @param uriConverterFactory
	 * @param result
	 * @throws IOException
	 */
	public ReplayParseContext(ContextResultURIConverterFactory uriConverterFactory,
			CaptureSearchResult result) throws IOException {
		this.uriConverterFactory = uriConverterFactory;
		this.result = result;
		setBaseUrl(result.getOriginalUrl());
		this.datespec = result.getCaptureTimestamp();

		this.converters = new HashMap<String, ResultURIConverter>();
	}

	/**
	 * constructor. {@code CaptureSearchResult} needs to be set via
	 * {@link #setCaptureSearchResult}.
	 * @param uriConverterFactory
	 * @param baseUrl
	 * @param datespec
	 * @deprecated 2014-05-02 use {@link #ReplayParseContext(ContextResultURIConverterFactory, CaptureSearchResult)}
	 */
	public ReplayParseContext(ContextResultURIConverterFactory uriConverterFactory,
			URL baseUrl, String datespec) {

		this.uriConverterFactory = uriConverterFactory;
		setBaseUrl(baseUrl.toExternalForm());
		this.datespec = datespec;
		this.converters = new HashMap<String,ResultURIConverter>();
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}
	public int getPhase() {
		return phase;
	}
	
	public void setRewriteHttpsOnly(boolean rewriteHttpsOnly) {
		this.rewriteHttpsOnly = rewriteHttpsOnly;
	}
		
	/**
	 * return {@code true} if {@code url} needs rewrite in this
	 * replay.
	 * <p>As {@link #contextualizeUrl(String, String)} runs this test,
	 * there's no real point doing this check outside of ReplayParseContext.
	 * this method may be changed to {@code protected} in the future.</p>
	 * @param url URL to test. it must be free of escaping (i.e. no {@code "https:\/\/"}.)
	 * @return {@code true} if {@code url} needs rewrite.
	 * @see #setRewriteHttpsOnly(boolean)
	 */
	public boolean isRewriteSupported(String url) {
		if (rewriteHttpsOnly)
			return url.startsWith(WaybackConstants.HTTPS_URL_PREFIX);
		return true;
	}

	/**
	 * @return the converters
	 */
	public Map<String, ResultURIConverter> getConverters() {
		return converters;
	}
	
	/**
	 * return {@code CaptureSearchResult} being rendered.
	 * <p>intended for selecting site-specific rewrite rules.</p>
	 * <p>TODO: what's really needed is its {@code urlKey}. add
	 * a method for it for better encapsulation.</p>
	 * @return {@code CaptureSearchResult} in replay mode,
	 *   or {@code null} otherwise.
	 */
	public CaptureSearchResult getCaptureSearchResult() {
		return result;
	}
	/**
	 * Set capture being rendered.
	 * @param result
	 * @deprecated 2014-11-05 Pass it to constructor
	 */
	public void setCaptureSearchResult(CaptureSearchResult result) {
		this.result = result;
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
			converter = uriConverterFactory.getContextConverter(flags);
			converters.put(flags,converter);
		}
		return converter;
	}
	
	public String contextualizeUrl(String url) {
		return contextualizeUrl(url,"");
	}
	/**
	 * Rewrite URL {@code url} in accordance with current replay mode, taking
	 * replay context {@code flags} into account.
	 * <p>It is important to return the same String object {@code url} if no rewrite
	 * is necessary, so that caller can short-circuit to avoid expensive String operations.</p>
	 * @param url URL, candidate for rewrite. may contain escaping. must not be {@code null}.
	 * @param flags <em>context</em> designator, such as {@code "cs_"}. can be {@code null}.
	 * @return rewrittenURL, or {@code url} if no rewrite is necessary. never {@code null}.
	 */
	public String contextualizeUrl(final String url, String flags) {
		// if we get an empty string, just return it:
		if (url.length() == 0) {
			return url;
		}
		
		if (url.startsWith(JAVASCRIPT_PREFIX) || url.startsWith(MAILTO_PREFIX) || url.startsWith(ANCHOR_PREFIX)) {
	    	return url;
	    }
	    // XXX duplicated check for MAILTO_PREFIX??
		if (url.startsWith(DATA_PREFIX) || url.startsWith(MAILTO_PREFIX)) {
	    	return url;
	    }

		// don't rewrite path-relative urls. For
		// https://webarchive.jira.com/browse/ARI-3985
		String trimmedUrl = url.trim();

		if (!trimmedUrl.startsWith("http://") &&
				!trimmedUrl.startsWith("https://") &&
				!trimmedUrl.startsWith("//") &&
				!trimmedUrl.startsWith("http:\\\\/\\\\/") &&
				!trimmedUrl.startsWith("http\\\\u00253A\\\\u00252F\\\\u00252F") &&
				!trimmedUrl.startsWith("https:\\\\/\\\\/") &&
				!trimmedUrl
					.startsWith("https\\\\u00253A\\\\u00252F\\\\u00252F") &&
				!trimmedUrl.startsWith("http:\\/\\/") &&
				!trimmedUrl.startsWith("https:\\/\\/") &&
				!trimmedUrl.startsWith("/") &&
				!trimmedUrl.startsWith(".")) {
			return url;
		}

	    // first make url into absolute, taking BASE into account.
		// (this also removes escaping: ex. "https:\/\/" -> "https://")
	    String absurl = super.contextualizeUrl(url);
	    if (!isRewriteSupported(absurl)) {
	    	return url;
	    }
	    // XXX do this in getConverter
		if (flags == null) {
	    	flags = "";
	    }
	    ResultURIConverter converter = getConverter(flags);
		return converter.makeReplayURI(datespec, absurl);
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
