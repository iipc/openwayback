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
package org.archive.wayback.resourcestore.indexer;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.htmllex.ContextAwareLexer;
import org.archive.wayback.util.htmllex.ParseContext;
import org.archive.wayback.util.htmllex.ParseEventDelegator;
import org.archive.wayback.util.url.UrlOperations;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.util.ParserException;

public class HTTPRecordAnnotater {
	private RobotMetaRule rule = null;
	private ParseEventDelegator rules = null;
	private RobotMetaFlags robotFlags;
	private static final Logger LOGGER =
        Logger.getLogger(HTTPRecordAnnotater.class.getName());
	private static final String UPPER_LOCATION = 
			WaybackConstants.LOCATION_HTTP_HEADER.toUpperCase();
	private final static String[] mimes = {
		"html"
	};
	public HTTPRecordAnnotater() {
		rules = new ParseEventDelegator();
		rules.init();
		rule = new RobotMetaRule();
		robotFlags = new RobotMetaFlags();
		rule.setRobotFlags(robotFlags);
		rule.visit(rules);
	}
	public boolean isHTML(String mimeType) {
		String mimeLower = mimeType.toLowerCase();
		for(String mime : mimes) {
			if(mimeLower.contains(mime)) {
				return true;
			}
		}
		return false;
	}

	private String escapeSpaces(final String input) {
		if(input.contains(" ")) {
			return input.replace(" ", "%20");
		}
		return input;
	}
	
	public String transformHTTPMime(String input) {
		if(input == null) {
			return null;
		}
		int semiIdx = input.indexOf(";");
		if(semiIdx > 0) {
			return escapeSpaces(input.substring(0,semiIdx).trim());
		}
		return escapeSpaces(input.trim());
	}
	
	public void annotateHTTPContent(CaptureSearchResult result, 
    		InputStream is, Header[] headers, String mimeGuess) {
		robotFlags.reset();
		String mimeType = null;
		if (headers != null) {
	
			for (Header httpHeader : headers) {
				if (httpHeader.getName().toUpperCase().equals(
						UPPER_LOCATION)) {
					
					// Old Comment: "Location" is supposed to be absolute:
					// (http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html)
					// (section 14.30) but Content-Location can be
					// relative.
					// is it correct to resolve a relative Location, as
					// we are?
					// it's also possible to have both in the HTTP
					// headers...
					// should we prefer one over the other?
					// right now, we're ignoring "Content-Location"
					//
					
					// NOTE: FILLING THE REDIRECT FIELD IN CDX IS DISABLED!
					// If we want to support redirect in cdx as long as the url is valid
					// comment out the following lines:
					
					// String locationStr = httpHeader.getValue();
					// result.setRedirectUrl(
					//		UrlOperations.resolveUrl(result.getOriginalUrl(),
					//				locationStr, "-"));

				} else if(httpHeader.getName().toLowerCase().equals("content-type")) {
					mimeType = transformHTTPMime(httpHeader.getValue());
				} else if(httpHeader.getName().toLowerCase().equals(
						WaybackConstants.X_ROBOTS_HTTP_HEADER)) {

					robotFlags.parse(httpHeader.getValue());
				}
			}
		}
		
		// TODO: get the encoding:
		String encoding = "utf-8";
		if(mimeType == null) {
			// nothing present in the HTTP headers.. Use the WARC field:
			mimeType = transformHTTPMime(mimeGuess);
		}
		if(mimeType == null) {
			mimeType = "unknown";
		}
		result.setMimeType(mimeType);
		// Now the sticky part: If it looks like an HTML document, look for
		// robot meta tags:
		if(isHTML(mimeType)) {
			String fileContext = result.getFile() + ":" + result.getOffset();
			annotateHTMLContent(is, encoding, fileContext, result);
		}
		robotFlags.apply(result);
		
	}
	
	public void annotateHTMLContent(InputStream is, String charSet, String fileContext, 
			CaptureSearchResult result) {

		ParseContext context = new ParseContext();
   	
    	Node node;
    	try {
        	ContextAwareLexer lex = new ContextAwareLexer(
        			new Lexer(new Page(is,charSet)),context);
			while((node = lex.nextNode()) != null) {
//				System.err.println("\nDEBUG-Node:js("+context.isInJS()+")css("+context.isInCSS()+"):");
//				System.err.println("-------------------/START");
//				System.err.println(node.toHtml(true));
//				System.err.println("-------------------/END");
				rules.handleNode(context, node);
			}
			rules.handleParseComplete(context);
		} catch (ParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.warning(fileContext + " " + e.getLocalizedMessage());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			LOGGER.warning(fileContext + " " + e.getLocalizedMessage());
		} catch (IOException e) {
			LOGGER.warning(fileContext + " " + e.getLocalizedMessage());
		}
	}
}
