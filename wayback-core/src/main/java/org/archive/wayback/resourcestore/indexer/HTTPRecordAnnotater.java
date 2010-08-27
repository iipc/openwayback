/* HTTPRecordAnnotater
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
				if (httpHeader.getName().equals(
						WaybackConstants.LOCATION_HTTP_HEADER)) {
	
					String locationStr = httpHeader.getValue();
					// TODO: "Location" is supposed to be absolute:
					// (http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html)
					// (section 14.30) but Content-Location can be
					// relative.
					// is it correct to resolve a relative Location, as
					// we are?
					// it's also possible to have both in the HTTP
					// headers...
					// should we prefer one over the other?
					// right now, we're ignoring "Content-Location"
					result.setRedirectUrl(
							UrlOperations.resolveUrl(result.getOriginalUrl(),
									locationStr));

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
