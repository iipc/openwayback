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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.ReplayURIConverter.URLStyle;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.replay.JSPExecutor;
import org.archive.wayback.replay.ReplayContext;
import org.archive.wayback.replay.ReplayURLTransformer;
import org.archive.wayback.util.htmllex.ParseContext;

/**
 * {@code ReplayParseContext} holds context information shared among replay
 * rewriter components.
 * <p>
 * 2014-05-02 small behavior/interface changes:
 * <ul>
 * <li>{@link #setJspExec(JSPExecutor)} no longer copies
 * {@code CaptureSearchResult} object from its {@code UIResults} object to
 * {@code result} member. Use new constructor taking {@code CaptureSearchResult}
 * object (recommended), or use
 * {@link #setCaptureSearchResult(CaptureSearchResult)} method.</li>
 * <li>
 * </ul>
 * TODO: consider replacing {@code CaptureSearchResult} reference with
 * {@code Capture}.
 */
public class ReplayParseContext extends ParseContext implements ReplayContext {

	private String datespec = null;
	private JSPExecutor jspExec = null;
	private OutputStream outputStream = null;
	private String outputCharset;
	private int phase = -1;
	private int jsBlockCount = -1;
	private CaptureSearchResult result;

	private ReplayURLTransformer replayUrlTransformer;

	private ReplayURIConverter uriConverter;

	/**
	 * {@link ReplayURIConverter} and {@link ReplayURLTransformer} implementation that uses old
	 * {@link ContextResultURIConverterFactory} and {@link ResultURIConverter}
	 * for backward compatibility, when ReplayParseContext is initialized with old
	 * constructor
	 * {@link ReplayParseContext#ReplayParseContext(ContextResultURIConverterFactory, CaptureSearchResult)}
	 */
	protected static class CompatReplayURIConverter implements ReplayURIConverter, ReplayURLTransformer {
		//private ResultURIConverter uriConverter;
		private ContextResultURIConverterFactory uriConverterFactory;
		protected Map<String, ResultURIConverter> converters;
		public CompatReplayURIConverter(
				ContextResultURIConverterFactory uriConverterFactory) {
			this.uriConverterFactory = uriConverterFactory;
			this.converters = new HashMap<String, ResultURIConverter>();
		}

		@Override
		public String makeReplayURI(String datespec, String url, String flags,
				URLStyle urlStyle) {
			ResultURIConverter converter = getConverter(flags);
			return converter.makeReplayURI(datespec, url);
		}

		@Override
		public String makeReplayURI(String datespec, String url) {
			return makeReplayURI(datespec, url, "", URLStyle.ABSOLUTE);
		}

		protected ResultURIConverter getConverter(String flags) {
			if (flags == null)
				flags = "";
			// TODO: caching should be a responsibility of ContextResultURIConverterFactory.
			// but it's a API-breaking change as converters is exposed through getter.
			ResultURIConverter converter = converters.get(flags);
			if (converter == null) {
				converter = uriConverterFactory.getContextConverter(flags);
				converters.put(flags, converter);
			}
			return converter;
		}

		@Override
		public ReplayURLTransformer getURLTransformer() {
			return this;
		}
		
		// ReplayURLTransformer implementation

		private static final String MAILTO_PREFIX = "mailto:";
		public static final String JAVASCRIPT_PREFIX = "javascript:";
		public static final String DATA_PREFIX = "data:";
		public static final String ANCHOR_PREFIX = "#";

		protected boolean rewriteHttpsOnly;

		private static boolean isProtocolRelative(String url) {
			if (url.startsWith("//"))
				return true;
			if (url.startsWith("\\/\\/"))
				return true;
			if (url.startsWith("\\\\/\\\\/"))
				return true;
			if (url.startsWith("\\\\u00252F\\\\u00252F"))
				return true;
			return false;
		}

		private static boolean isFullURL(String url) {
			if (url.startsWith("http")) {
				String s = url.substring(4);
				if (s.startsWith("s"))
					s = s.substring(1);
				if (s.startsWith(":"))
					return isProtocolRelative(s.substring(1));
				if (s.startsWith("\\\\u00253A"))
					return isProtocolRelative(s.substring(9));
			}
			return false;
		}

		@Override
		public String transform(ReplayContext replayContext, String url,
				String flags) {
			// TODO: this is identical to ArchivalUrlReplayURLTransformer.transform
			// except for "isRewriteSupported" line.

			// if we get an empty string, just return it:
			if (url.length() == 0) {
				return url;
			}

			if (url.startsWith(JAVASCRIPT_PREFIX) ||
					url.startsWith(MAILTO_PREFIX) ||
					url.startsWith(DATA_PREFIX) ||
					url.startsWith(ANCHOR_PREFIX)) {
				return url;
			}

			// don't rewrite full and path-relative URLs. For
			// https://webarchive.jira.com/browse/ARI-3985
			// TODO: URL starting with "./" or "../" are path-relative
			// and can be left as they are (?)
			String trimmedUrl = url.trim();
			if (!isFullURL(trimmedUrl) && !isProtocolRelative(trimmedUrl) &&
					!trimmedUrl.startsWith("/") && !trimmedUrl.startsWith(".")) {
				return url;
			}

			// first make url into absolute, taking BASE into account.
			// (this also removes escaping: ex. "https:\/\/" -> "https://")
			String absurl = null;
			try {
				absurl = replayContext.resolve(url);
			} catch (URISyntaxException ex) {
				return url;
			}
			if (!isRewriteSupported(absurl)) {
				return url;
			}
			return replayContext.makeReplayURI(absurl, flags, URLStyle.ABSOLUTE);
		}

		public boolean isRewriteSupported(String url) {
			if (rewriteHttpsOnly)
				return url.startsWith(WaybackConstants.HTTPS_URL_PREFIX);
			return true;
		}
	}

	/**
	 * Initialize {@code ReplayParseContext} with URL translator object and
	 * reference to target capture.
	 * @param uriConverter TODO
	 * @param replayUrlTransformer URL translator
	 * @param result capture reference (originalUrl and captureTimestamp)
	 */
	public ReplayParseContext(ReplayURIConverter uriConverter,
			CaptureSearchResult result) {
		this.result = result;
		setBaseUrl(result.getOriginalUrl());
		this.datespec = result.getCaptureTimestamp();
		this.replayUrlTransformer = uriConverter.getURLTransformer();
		if (this.replayUrlTransformer == null) {
			// TODO: default?
		}
		this.uriConverter = uriConverter;
	}

	/**
	 * Transitional constructor method, for use by wayback-core code.
	 * Calls {@link #ReplayParseContext(ReplayURIConverter, ReplayURLTransformer, CaptureSearchResult)}
	 * if {@code uriConverter} implements {@link ReplayURLTransformer}, and Calls
	 * {@link #ReplayParseContext(ContextResultURIConverterFactory, URL, String)}
	 * otherwise.
	 * @param uriConverter ResultURIConverter passed from access point.
	 * @param converterFactory contextualizing URI converter factory configured for ReplayRenderer.
	 * @param result capture being replayed
	 * @param rewriteHttpsOnly HTTPS rewrite flag
	 */
	@SuppressWarnings("deprecation")
	public static ReplayParseContext create(ResultURIConverter uriConverter,
			ContextResultURIConverterFactory converterFactory,
			CaptureSearchResult result, boolean rewriteHttpsOnly) {
		if (uriConverter instanceof ReplayURIConverter) {
			return new ReplayParseContext((ReplayURIConverter)uriConverter, result);
		}
		// backward-compatibility mode
		final ContextResultURIConverterFactory fact;
		if (converterFactory != null) {
			fact = converterFactory;
		} else if (uriConverter instanceof ContextResultURIConverterFactory) {
			fact = (ContextResultURIConverterFactory)uriConverter;
		} else {
			fact = new IdentityResultURIConverterFactory(uriConverter);
		}
		return new ReplayParseContext(fact, result);
	}

	/**
	 * Constructs {@code ReplayParseContext} for rewriting a resource
	 * represented by {@code result}.
	 * <p>
	 * Initializes {@code baseUrl} and {@code datespec} from {@code result}'s
	 * {@code originalUrl} and {@code captureTimestamp}, respectively.
	 * This used to be the primary constructor before the introduction of ReplayURIConverter
	 * and ReplayURLTransformer. Retained for 3rd party ReplayRenderer implementations using
	 * ReplayParseContext.
	 * </p>
	 * @param uriConverterFactory contextualized URI converter factory, must not be {@code null}.
	 * @param result capture being replayed
	 * @deprecated 2015-02-04 use
	 *             {@link #ReplayParseContext(ReplayURIConverter, CaptureSearchResult)}
	 *             .
	 */
	public ReplayParseContext(
			ContextResultURIConverterFactory uriConverterFactory,
			CaptureSearchResult result) {
		this(new CompatReplayURIConverter(uriConverterFactory), result);
	}

	/**
	 * constructor. {@code CaptureSearchResult} needs to be set via
	 * {@link #setCaptureSearchResult}.
	 * @param uriConverterFactory
	 * @param baseUrl
	 * @param datespec
	 * @deprecated 2014-05-02 use
	 *             {@link #ReplayParseContext(ContextResultURIConverterFactory, CaptureSearchResult)}
	 */
	public ReplayParseContext(
			ContextResultURIConverterFactory uriConverterFactory, URL baseUrl,
			String datespec) {
		setBaseUrl(baseUrl.toExternalForm());
		this.datespec = datespec;
		this.uriConverter = new CompatReplayURIConverter(uriConverterFactory);
		this.replayUrlTransformer = this.uriConverter.getURLTransformer();
	}

	public void setPhase(int phase) {
		this.phase = phase;
	}

	public int getPhase() {
		return phase;
	}

	/**
	 * @param rewriteHttpsOnly
	 * @deprecated See ProxyHttpsResultURIConverter
	 */
	public void setRewriteHttpsOnly(boolean rewriteHttpsOnly) {
		if (replayUrlTransformer instanceof CompatReplayURIConverter) {
			((CompatReplayURIConverter)replayUrlTransformer).rewriteHttpsOnly = rewriteHttpsOnly;
		}
	}

	/**
	 * return {@code true} if {@code url} needs rewrite in this replay.
	 * <p>
	 * As {@link #contextualizeUrl(String, String)} runs this test, there's no
	 * real point doing this check outside of ReplayParseContext. this method
	 * may be changed to {@code protected} in the future.
	 * </p>
	 * @param url URL to test. it must be free of escaping (i.e. no
	 *        {@code "https:\/\/"}.)
	 * @return {@code true} if {@code url} needs rewrite.
	 * @deprecated See ProxyHttpsResultURIConverter
	 */
	public boolean isRewriteSupported(String url) {
		if (replayUrlTransformer instanceof CompatReplayURIConverter) {
			return ((CompatReplayURIConverter)replayUrlTransformer).isRewriteSupported(url);
		}
		// Return value does not matter because this method is not supposed to be used in
		// new ReplayURLTransformer implementations. If it ever is, returning true does less
		// harm than returning false.
		return true;
	}

	/**
	 * @return the converters
	 * @deprecated 2015-01-14 no replacement.
	 */
	public Map<String, ResultURIConverter> getConverters() {
		if (uriConverter instanceof CompatReplayURIConverter) {
			return ((CompatReplayURIConverter)uriConverter).converters;
		} else {
			return null;
		}
	}

	/**
	 * return {@code CaptureSearchResult} being rendered.
	 * <p>
	 * intended for selecting site-specific rewrite rules.
	 * </p>
	 * <p>
	 * TODO: what's really needed is its {@code urlKey}. add a method for it for
	 * better encapsulation.
	 * </p>
	 * @return {@code CaptureSearchResult} in replay mode, or {@code null}
	 *         otherwise.
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
	 * @deprecated 2015-01-14 no replacement
	 */
	public void setConverters(Map<String, ResultURIConverter> converters) {
		if (uriConverter instanceof CompatReplayURIConverter) {
			((CompatReplayURIConverter)uriConverter).converters = converters;
		}
	}

	/**
	 * @param flag resource context indicator
	 * @param converter ResultURIConverter for translating URL
	 * @deprecated 2015-01-14 no replacement
	 */
	public void addConverter(String flag, ResultURIConverter converter) {
		if (uriConverter instanceof CompatReplayURIConverter) {
			((CompatReplayURIConverter)uriConverter).converters.put(
				flag, converter);
		}
	}

	/**
	 * returns {@link ResultURIConverter} for resource context
	 * <code>flags</code>.
	 * @param flags resource context indicator such as "{@code cs_}", "
	 *        {@code im_}".
	 * @return ResultURIConverter for translating URL
	 * @see org.archive.wayback.archivalurl.ArchivalUrlSpecialContextReusltURIConverter
	 * @deprecated 2015-02-10 no direct replacement. See {@link ReplayURLTransformer}
	 */
	public ResultURIConverter getConverter(String flags) {
		if (uriConverter instanceof CompatReplayURIConverter) {
			return ((CompatReplayURIConverter)uriConverter).getConverter(flags);
		} else {
			return null;
		}
	}

	public String contextualizeUrl(String url) {
		return contextualizeUrl(url, "");
	}

	/**
	 * Rewrite URL {@code url} in accordance with current replay mode, taking
	 * replay context {@code flags} into account.
	 * <p>
	 * It is important to return the same String object {@code url} if no
	 * rewrite is necessary, so that caller can short-circuit to avoid expensive
	 * String operations.
	 * </p>
	 * @param url URL, candidate for rewrite. may contain escaping. must not be
	 *        {@code null}.
	 * @param flags <em>context</em> designator, such as {@code "cs_"}. can be
	 *        {@code null}.
	 * @return rewrittenURL, or {@code url} if no rewrite is necessary. never
	 *         {@code null}.
	 */
	public String contextualizeUrl(final String url, String flags) {
		if (replayUrlTransformer == null)
			return url;
		return replayUrlTransformer.transform(this, url, flags);
	}

	@Override
	public String makeReplayURI(String url, String flags, URLStyle urlStyle) {
		return uriConverter.makeReplayURI(getDatespec(), url, flags, urlStyle);
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

	public String getDatespec() {
		return datespec;
	}

	/**
	 * @param datespec the datespec to set
	 * @deprecated 2015-02-04 no replacement.
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
