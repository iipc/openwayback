/* SearchResultComparator
 *
 * $Id$
 *
 * Created on 4:21:10 PM Aug 17, 2006.
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
package org.archive.wayback.resourceindex;

import java.util.Comparator;

import org.archive.wayback.core.CaptureSearchResult;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class SearchResultComparator implements Comparator<CaptureSearchResult> {

	private boolean backwards;
	/**
	 * Constructor backwards value of true creates a reverse comparator
	 * @param backwards
	 */
	public SearchResultComparator(boolean backwards) {
		this.backwards = backwards;
	}
	/**
	 * Constructor: compare in normal forwards sort order
	 */
	public SearchResultComparator() {
		backwards = false;
	}
	
	private String objectToKey(CaptureSearchResult r) {
		String urlKey = r.getUrlKey();
		String captureDate = r.getCaptureTimestamp();
		return urlKey + " " + captureDate;
	}
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(CaptureSearchResult o1, CaptureSearchResult o2) {
		String k1 = objectToKey(o1);
		String k2 = objectToKey(o2);
		if(backwards) {
			return k2.compareTo(k1);
		}
		return k1.compareTo(k2);
	}
}
