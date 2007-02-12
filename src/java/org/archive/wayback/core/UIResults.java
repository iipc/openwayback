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

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.util.StringFormatter;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class UIResults {
	private final static String FERRET_NAME = "ui-results";
	protected WaybackRequest wbRequest;
	
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
		return wbRequest;
	}

	/**
	 * @return StringFormatter localized to user request
	 */
	public StringFormatter getFormatter() {
		return wbRequest.getFormatter();
	}
	/**
	 * Store this UIResults in the HttpServletRequest argument.
	 * @param httpRequest
	 */
	public void storeInRequest(HttpServletRequest httpRequest) {
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
			results.storeInRequest(httpRequest);
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
}
