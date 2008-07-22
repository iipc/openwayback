/* LegacyToIdentityFilter
 *
 * $Id$
 *
 * Created on 11:48:56 AM Jul 10, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
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
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.UrlOperations;

/**
 * CaptureSearchResult ObjectFilter which passes through all inputs, modifying
 * each to construct a corrected original URL to comply with new Identity 
 * format.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class LegacyToIdentityFilter implements ObjectFilter<CaptureSearchResult> {
	private final static String DEFAULT_SCHEME = "http://";
	
	private int getEndOfHostIndex(String url) {
		int portIdx = url.indexOf(UrlOperations.PORT_SEPARATOR);
		int pathIdx = url.indexOf(UrlOperations.PATH_START);
		if(portIdx == -1 && pathIdx == -1) {
			return url.length();
		}
		if(portIdx == -1) {
			return pathIdx;
		}
		if(pathIdx == -1) {
			return portIdx;
		}
		if(pathIdx > portIdx) {
			return portIdx;
		} else {
			return pathIdx;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.ObjectFilter#filterObject(java.lang.Object)
	 */
	public int filterObject(CaptureSearchResult o) {
		String urlKey = o.getUrlKey();
		StringBuilder sb = new StringBuilder(urlKey.length());
		sb.append(DEFAULT_SCHEME);
		sb.append(o.getOriginalUrl());
		sb.append(urlKey.substring(getEndOfHostIndex(urlKey)));
		o.setOriginalUrl(sb.toString());
		return FILTER_INCLUDE;
	}

}
