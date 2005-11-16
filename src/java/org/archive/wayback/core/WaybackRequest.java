/* WMRequest
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import java.util.Properties;

/**
 * Abstraction of all the data associated with a users request to the Wayback
 * Machine.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class WaybackRequest {
	
	private int resultsPerPage = 1000;
	private int pageNum = 1;
	private Properties filters = new Properties();
	
	/**
	 * Constructor
	 */
	public WaybackRequest() {
		super();
	}

	/**
	 * @return Returns the pageNum.
	 */
	public int getPageNum() {
		return pageNum;
	}

	/**
	 * @param pageNum The pageNum to set.
	 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	/**
	 * @return Returns the resultsPerPage.
	 */
	public int getResultsPerPage() {
		return resultsPerPage;
	}

	/**
	 * @param resultsPerPage The resultsPerPage to set.
	 */
	public void setResultsPerPage(int resultsPerPage) {
		this.resultsPerPage = resultsPerPage;
	}

	public boolean containsKey(String key) {
		return filters.containsKey(key);
	}

	public String get(String key) {
		return (String) filters.get(key);
	}

	public void put(String key, String value) {
		filters.put(key, value);
	}
}
