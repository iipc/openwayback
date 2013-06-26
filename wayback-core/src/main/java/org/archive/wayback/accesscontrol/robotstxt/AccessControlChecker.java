package org.archive.wayback.accesscontrol.robotstxt;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringEscapeUtils;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.webapp.AbstractRequestHandler;

public class AccessControlChecker extends AbstractRequestHandler {
	
	protected final static String HTTP_PREFIX = "http://";
	protected final static String HTTPS_PREFIX = "https://";
	
	protected ExclusionFilterFactory exclusionFactory;
	protected UrlCanonicalizer canonicalizer = null;

	@Override
	public boolean handleRequest(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {
		
		String url = this.translateRequestPath(httpRequest);
		
		if (!url.startsWith(HTTP_PREFIX) && !url.startsWith(HTTPS_PREFIX)) {
			url = HTTP_PREFIX + url;
		}
		
		PrintWriter writer = httpResponse.getWriter();
		httpResponse.setContentType("application/json");
		return checkAccess(url, writer);
	}
	
	protected boolean checkAccess(String url, PrintWriter writer) {
		if (exclusionFactory == null) {
			return false;
		}
		
		ExclusionFilter filter = exclusionFactory.get();
		FastCaptureSearchResult result = new FastCaptureSearchResult();
		result.setOriginalUrl(url);
		
		int status = ExclusionFilter.FILTER_EXCLUDE;
		
		String urlKey = url;
		
		try {
			// Must set canonicalizer
			urlKey = canonicalizer.urlStringToKey(url);			
			result.setUrlKey(urlKey);
			
			status = filter.filterObject(result);
		} catch (URIException e) {
			//Exclude invalid
		}
		
		if (status == ExclusionFilter.FILTER_INCLUDE) {
			writer.println("[\"allow\"]");
		} else {
			writer.println("[\"block\"]");
		}
		
		return true;
	}

	public ExclusionFilterFactory getExclusionFactory() {
		return exclusionFactory;
	}

	public void setExclusionFactory(ExclusionFilterFactory exclusionFactory) {
		this.exclusionFactory = exclusionFactory;
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
}
