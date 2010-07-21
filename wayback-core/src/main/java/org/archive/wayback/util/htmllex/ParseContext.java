/* ParseContext
 *
 * $Id$
 *
 * Created on 2:06:46 PM Feb 19, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
 *
 * This file is part of test.
 *
 * test is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * test is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with test; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util.htmllex;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.URIException;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.htmlparser.util.Translate;

/**
 * Class which tracks the context and state involved with parsing an HTML
 * document via SAX events.
 * 
 * Also holds some page URL information, and provides some URL resolving 
 * functionality.
 * 
 * Lastly, this class exposes a general purpose HashMap<String,String> for use
 * by specific applications.
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class ParseContext {
	protected UURI baseUrl = null;

	private boolean inCSS = false;
	private boolean inJS = false;
	private boolean inScriptText = false;
	private HashMap<String,String> data = null;

	/**
	 * constructor
	 */
	public ParseContext() {
		data = new HashMap<String, String>();
	}
	/**
	 * Stores arbitrary key value pairs in this ParseContext
	 * @param key for storage
	 * @param value for storage
	 */
	public void putData(String key, String value) {
		data.put(key, value);
	}
	/**
	 * Retrieves previously stored data for key key from this ParseContext
	 * @param key under which value was stored
	 * @return previously stored value for key or null, if nothing was stored
	 */
	public String getData(String key) {
		return data.get(key);
	}
	public Map<String,String> getMap() {
		return data;
	}
	/**
	 * @param url against which relative URLs should be resolved for this parse
	 */
	public void setBaseUrl(URL url) {
		try {
			baseUrl = UURIFactory.getInstance(url.toExternalForm());
		} catch (URIException e) {
			e.printStackTrace();
		}
	}
	/**
	 * @param url which should be resolved against the baseUrl for this 
	 * ParseContext.
	 * @return absolute form of url, resolved against baseUrl if relative.
	 * @throws URISyntaxException if the input URL is malformed
	 */
	public String resolve(String url) throws URISyntaxException {
		url = Translate.decode(url);
		int hashIdx = url.indexOf('#');
		String frag = "";
		if(hashIdx != -1) {
			frag = url.substring(hashIdx);
			url = url.substring(0,hashIdx);
		}
		try {
			if(baseUrl == null) {
				// TODO: log
				System.err.println("No url to resolve!");
				return url;
			}
			return baseUrl.resolve(url,false).toString() + frag;
//			return UURIFactory.getInstance(baseUrl, url).toString() + frag;
		} catch (URIException e) {
			e.printStackTrace();
		}
		return url;
	}	
	/**
	 * @param url which should be resolved.
	 * @return absolute form of input url, or url itself if javascript:
	 */
	public String contextualizeUrl(String url) {
	    if(url.startsWith("javascript:")) {
	    	return url;
	    }
		try {
			return resolve(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return url;
		}
	}

	/**
	 * @return the inCSS
	 */
	public boolean isInCSS() {
		return inCSS;
	}
	/**
	 * @param inCSS the inCSS to set
	 */
	public void setInCSS(boolean inCSS) {
		this.inCSS = inCSS;
	}
	/**
	 * @return the inJS
	 */
	public boolean isInJS() {
		return inJS;
	}
	/**
	 * @param inJS the inJS to set
	 */
	public void setInJS(boolean inJS) {
		this.inJS = inJS;
	}

	/**
	 * @return the inScriptText
	 */
	public boolean isInScriptText() {
		return inScriptText;
	}
	/**
	 * @param inScriptText the inScriptText to set
	 */
	public void setInScriptText(boolean inScriptText) {
		this.inScriptText = inScriptText;
	}
}
