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
package org.archive.wayback.util.htmllex;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringEscapeUtils;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
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
	private static final Logger LOGGER = Logger.getLogger(
			ParseContext.class.getName());

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

	/**
	 * @return the full Map of String to String for this parsing context.
	 */
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
		// BUG in Translate.decode(): "foo?a=b&lang=en" acts as if it 
		// was "&lang;"
//		url = Translate.decode(url);
		url = StringEscapeUtils.unescapeHtml(url);
		int hashIdx = url.indexOf('#');
		String frag = "";
		if(hashIdx != -1) {
			frag = url.substring(hashIdx);
			url = url.substring(0,hashIdx);
		}
		
		if(baseUrl == null) {
			// TODO: log ?
			return url + frag;
		}
		
		try {
			return UURIFactory.getInstance(baseUrl, url).toString() + frag;
		} catch (URIException e) {
			LOGGER.warning("FAILED RESOLVE: base(" + baseUrl + ") frag(" + url +
					") error(" + e.getMessage() + ")");
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
