/* MimeTypeFilter
 *
 * $Id$
 *
 * Created on 3:41:41 PM Aug 17, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.filters;

import java.util.HashMap;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * SearchResultFilter which includes only records matching one or more supplied
 * Mime-Types. All comparision is case-insensitive.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class MimeTypeFilter implements ObjectFilter<CaptureSearchResult> {
	private HashMap<String,Integer> validMimes = null;
	private boolean includeIfContains = true; 
	
	/**
	 * @param mime String which is valid match for mime-type field
	 */
	public void addMime(final String mime) {
		if(validMimes == null) {
			validMimes = new HashMap<String, Integer>();
		}
		validMimes.put(mime.toLowerCase(),null);
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult r) {
		String mime = r.getMimeType().toLowerCase();
		return validMimes.containsKey(mime) == includeIfContains ? 
				FILTER_INCLUDE : FILTER_EXCLUDE;
	}

	/**
	 * @return the includeIfContains
	 */
	public boolean isIncludeIfContains() {
		return includeIfContains;
	}

	/**
	 * @param includeIfContains the includeIfContains to set
	 */
	public void setIncludeIfContains(boolean includeIfContains) {
		this.includeIfContains = includeIfContains;
	}
}
