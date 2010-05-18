/* SkippingStringPrefixIterator
 *
 * $Id$:
 *
 * Created on May 14, 2010.
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

package org.archive.wayback.resourceindex.ziplines;

import java.util.Iterator;

/**
 * @author brad
 *
 */
public class SkippingStringPrefixIterator extends StringPrefixIterator {
	private long skipCount = 0;
	private long totalMatches = -1;
	
	public SkippingStringPrefixIterator(Iterator<String> inner, String prefix, 
			long skipCount) {
		super(inner,prefix);
		this.skipCount = skipCount;
	}
	public SkippingStringPrefixIterator(Iterator<String> inner, String prefix) {
		super(inner,prefix);
	}
	public long getTotalMatches() {
		return totalMatches;
	}
	public void setTotalMatches(long totalMatches) {
		this.totalMatches = totalMatches;
	}
	public boolean hasNext() {
		while(skipCount > 0) {
			if(super.hasNext()) {
				next();
				skipCount--;
			} else {
				return false;
			}
		}
		return super.hasNext();
	}
}
