/* UIResults
 *
 * $Id$
 *
 * Created on 4:05:51 PM Feb 1, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-svn.
 *
 * wayback-svn is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-svn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.core;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.util.StringFormatter;
import org.archive.wayback.webapp.AccessPoint;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UIResults {
	private final static String FERRET_NAME = "ui-results";
	protected WaybackRequest wbRequest;
	private String contentJsp = null;
	
	
	/**
	 * @param wbRequest Wayback Request argument
	 */
	public UIResults(WaybackRequest wbRequest) {
		super();
		this.wbRequest = wbRequest;
	}
	/**
	 * @return Returns the wbRequest.
	 */
	public WaybackRequest getWbRequest() {
		if(wbRequest == null) {
			wbRequest = new WaybackRequest();
		}
		return wbRequest;
	}

	/**
	 * @return StringFormatter localized to user request
	 */
	public StringFormatter getFormatter() {
		return getWbRequest().getFormatter();
	}
	/**
	 * Store this UIResults in the HttpServletRequest argument.
	 * @param httpRequest
	 * @param contentJsp 
	 */
	public void storeInRequest(HttpServletRequest httpRequest, 
			String contentJsp) {
		this.contentJsp = contentJsp;
		httpRequest.setAttribute(FERRET_NAME, this);		
	}

	/**
	 * @param httpRequest
	 * @return UIResults from httpRequest, or a generic one if not present 
	 */
	public static UIResults getFromRequest(HttpServletRequest httpRequest) {
		UIResults results = (UIResults) httpRequest.getAttribute(FERRET_NAME);
		if(results == null) {
			results = getGeneric(httpRequest);
			// why not store it in case someone else needs it...
//			results.storeInRequest(httpRequest,"");
		}
		return results;
	}
	
	/**
	 * @param httpRequest
	 * @return generic UIResult with info from httpRequest applied.
	 */
	public static UIResults getGeneric(HttpServletRequest httpRequest) {
		WaybackRequest wbRequest = new WaybackRequest();
		wbRequest.fixup(httpRequest);
		return new UIResults(wbRequest);
	}
	private static void replaceAll(StringBuffer s,
			final String o, final String n) {
		int olen = o.length();
		int nlen = n.length();
		int found = s.indexOf(o);
		while(found >= 0) {
			s.replace(found,found + olen,n);
			found = s.indexOf(o,found + nlen);
		}
	}
	
	/**
	 * return a string appropriate for inclusion as an XML tag
	 * @param tagName
	 * @return encoded tagName
	 */
	public static String encodeXMLEntity(final String tagName) {
		StringBuffer encoded = new StringBuffer(tagName);
		//replaceAll(encoded,";","&semi;");
		replaceAll(encoded,"&","&amp;");
		replaceAll(encoded,"\"","&quot;");
		replaceAll(encoded,"'","&apos;");
		replaceAll(encoded,"<","&lt;");
		replaceAll(encoded,">","&gt;");
		return encoded.toString();
	}
	
	/**
	 * return a string appropriate for inclusion as an XML tag
	 * @param content
	 * @return encoded content
	 */
	public static String encodeXMLContent(final String content) {
		StringBuffer encoded = new StringBuffer(content);
		
		replaceAll(encoded,"&","&amp;");
		replaceAll(encoded,"\"","&quot;");
		replaceAll(encoded,"'","&apos;");
		replaceAll(encoded,"<","&lt;");
		replaceAll(encoded,">","&gt;");
		
		return encoded.toString();
	}

	/**
	 * return a string appropriate for inclusion as an XML tag
	 * @param content
	 * @return encoded content
	 */
	public static String encodeXMLEntityQuote(final String content) {
		StringBuffer encoded = new StringBuffer(content);
		replaceAll(encoded,"amp","&#38;#38;");
		replaceAll(encoded,"apos","&#39;");
		replaceAll(encoded,"<","&#38;#60;");
		replaceAll(encoded,"gt","&#62;");
		replaceAll(encoded,"quot","&#34;");
		return encoded.toString();
	}
	/**
	 * @return URL that points to the root of the current WaybackContext
	 */
	public String getContextPrefix() {
		return getWbRequest().getContextPrefix();
	}

	/**
	 * @return URL that points to the root of the Server
	 */
	public String getServerPrefix() {
		return getWbRequest().getServerPrefix();
	}
	/**
	 * @return the contentJsp
	 */
	public String getContentJsp() {
		return contentJsp;
	}
	/**
	 * @param contentJsp the contentJsp to set
	 */
	public void setContentJsp(String contentJsp) {
		this.contentJsp = contentJsp;
	}
	/**
	 * @param configName
	 * @return String configuration for the context, if present, otherwise null
	 */
	public String getContextConfig(final String configName) {
		String configValue = null;
		AccessPoint context = getWbRequest().getContext();
		if(context != null) {
			Properties configs = context.getConfigs();
			if(configs != null) {
				configValue = configs.getProperty(configName);
			}
		}
		return configValue;
	}
	
}
