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
package org.archive.wayback.proxy;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.archivalurl.ArchivalUrlResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.BadContentException;
import org.archive.wayback.replay.HttpHeaderOperation;
import org.archive.wayback.replay.HttpHeaderProcessor;
import org.archive.wayback.replay.TextDocument;
import org.archive.wayback.replay.TextReplayRenderer;
import org.archive.wayback.util.Timestamp;

/**
 * rewrite https link to http in http header (redirect) or in page content (links, etc.)
 * @author lam
 */
public class HttpsRedirectAndLinksRewriteProxyHTMLMarkupReplayRenderer extends ProxyHTMLMarkupReplayRenderer {

	//to rewrite https in URL
	private String httpRegex;
	
	private ResultURIConverter uriConverter;
	
	private final static Pattern defaultHttpPattern = Pattern
		.compile("(https://[A-Za-z0-9:_@.-]+)");
	
	private Pattern pattern = defaultHttpPattern;
	
	/**
	 * @param httpHeaderProcessor
	 */
	public HttpsRedirectAndLinksRewriteProxyHTMLMarkupReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
		super(httpHeaderProcessor);
		if(httpRegex != null && httpRegex.length() > 0) {
			pattern = Pattern.compile(httpRegex);
		}
	}
	
    //handle https redirect
	@Override
	public void renderResource(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, WaybackRequest wbRequest,
			CaptureSearchResult result, Resource httpHeadersResource,
			Resource payloadResource, ResultURIConverter uriConverter,
			CaptureSearchResults results) throws ServletException,
			IOException, BadContentException {

		// Decode resource (such as if gzip encoded)
		Resource decodedResource = decodeResource(httpHeadersResource, payloadResource);
		
		HttpHeaderOperation.copyHTTPMessageHeader(httpHeadersResource, httpResponse);

		Map<String,String> headers = HttpHeaderOperation.processHeaders(
				httpHeadersResource, result, uriConverter, getHttpHeaderProcessor());
		
		//mod BnF : handle session timestamps in redirection
		if(headers.containsKey("Location") || headers.containsKey("location")) {
			
			String locationLabel = null;
			if(headers.containsKey("Location")) {
				locationLabel = "Location";
			}
			if(headers.containsKey("location")) {
				locationLabel = "location";
			}
			
			//mod BnF : handle timesteamp if is a redirect			
			
			//mod BnF : handle https redirection case
			if(headers.get(locationLabel).startsWith("https")) {
				String location = headers.get(locationLabel);
				location = "http"+location.substring(5);
				//String requestURL = httpRequest.getRequestURL().toString();
				//test BnF
				String requestURL = wbRequest.getRequestUrl();
				//only if the redirection is a https auto redicretion (the same URL but with started with https)
				if(location.equals(requestURL)) {
					
					headers.put(locationLabel, location);
					
					CaptureSearchResult prev = null;
				    CaptureSearchResult next = null;
				    String exactDateStr = wbRequest.getReplayTimestamp();
				    
				    Iterator<CaptureSearchResult> it = results.iterator();
				    while (it.hasNext()) {
				        CaptureSearchResult res = it.next();
				        String resDateStr = res.getCaptureTimestamp();
				        int compared = resDateStr.compareTo(exactDateStr.substring(0,
				                resDateStr.length()));
				        if (compared < 0) {
				            prev = res;
				        } else if (compared > 0) {
				            if (next == null) {
				                next = res;
				            }
				        }
				    }
				    //we have now the previous and the next capture
				    if(next != null) {
				    	wbRequest.setReplayTimestamp(next.getCaptureTimestamp());
				    } else if(prev != null) {
				    	wbRequest.setReplayTimestamp(prev.getCaptureTimestamp());
				    } else {
				    	// else, eg only 1 https redirect result : we keep https header
				    	headers.put(locationLabel, "https"+location.substring(4));
				    }
				} else {
					//rewriting redirected url from https to http
					headers.put(locationLabel, location);
				}
			}
		}
		

		String charSet = getCharsetDetector().getCharset(httpHeadersResource,
				decodedResource, wbRequest);

		ResultURIConverter pageConverter = uriConverter;
		if (getPageConverterFactory() != null) {
			// XXX: ad-hoc code - ContextResultURIConverterFactory should take ResultURIConverter
			// as argument, so that it can simply wrap the original.
			String replayURIPrefix = (uriConverter instanceof ArchivalUrlResultURIConverter ?
					((ArchivalUrlResultURIConverter)uriConverter).getReplayURIPrefix() : "");
			ResultURIConverter ruc = getPageConverterFactory().getContextConverter(replayURIPrefix);
			if (ruc != null)
				pageConverter = ruc;
		}
		
		

		// Load content into an HTML page, and resolve load-time URLs:
		TextDocument page = new TextDocument(decodedResource, result,
				uriConverter);
		page.readFully(charSet);
		
		//KLM : replace https url in page
		replaceHttpsInPage(page,result);

		updatePage(page, httpRequest, httpResponse, wbRequest, result,
				decodedResource, pageConverter, results);

		// set the corrected length:
		int bytes = page.getBytes().length;
		headers.put(HttpHeaderOperation.HTTP_LENGTH_HEADER, String.valueOf(bytes));
		if (getGuessedCharsetHeader() != null) {
			headers.put(getGuessedCharsetHeader(), page.getCharSet());
		}

		// send back the headers:
		HttpHeaderOperation.sendHeaders(headers, httpResponse);

		// Tomcat will always send a charset... It's trying to be smarter than
		// we are. If the original page didn't include a "charset" as part of
		// the "Content-Type" HTTP header, then Tomcat will use the default..
		// who knows what that is, or what that will do to the page..
		// let's try explicitly setting it to what we used:
		httpResponse.setCharacterEncoding(page.getCharSet());

		page.writeToOutputStream(httpResponse.getOutputStream());
	}
	
	/**
	 * look for https links in page and replace them with http
	 */
	protected void replaceHttpsInPage(TextDocument page, 
			CaptureSearchResult result) throws ServletException, IOException {
		String resourceTS = result.getCaptureTimestamp();
		String captureTS = Timestamp.parseBefore(resourceTS).getDateStr();

		StringBuilder sb = page.sb;
		StringBuffer replaced = new StringBuffer(sb.length());
		Matcher m = pattern.matcher(sb);
		
		// If at least 2 groups, prepend before 2nd group and include 1st group. Allows for more sophisticated matching.
		// Otherwise, insert before 1st group
		if (m.groupCount() > 1) {
			while (m.find()) {
				String beforeHost = m.group(1);
				String host = m.group(2);
				String replacement = uriConverter.makeReplayURI(captureTS, host);
				m.appendReplacement(replaced, beforeHost + replacement);
			}	
		} else {
			while (m.find()) {
				String host = m.group(1);
				String replacement = uriConverter.makeReplayURI(captureTS, host);
				m.appendReplacement(replaced, replacement);
			}			
		}
		
		m.appendTail(replaced);
		// blasted StringBuilder/StringBuffer... gotta convert again...
		page.sb.setLength(0);
		page.sb.ensureCapacity(replaced.length());
		page.sb.append(replaced);

	}
	
	public String getHttpRegex() {
		return pattern.pattern();
	}

	public void setHttpRegex(String httpRegex) {
		pattern = Pattern.compile(httpRegex);
	}

	public ResultURIConverter getUriConverter() {
		return uriConverter;
	}

	public void setUriConverter(ResultURIConverter uriConverter) {
		this.uriConverter = uriConverter;
	}

}
