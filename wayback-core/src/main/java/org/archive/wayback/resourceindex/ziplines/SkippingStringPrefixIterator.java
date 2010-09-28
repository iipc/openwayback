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
