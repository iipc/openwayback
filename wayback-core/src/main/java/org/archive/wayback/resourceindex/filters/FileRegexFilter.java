/* FileRegexFilter
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
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
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class FileRegexFilter implements ObjectFilter<CaptureSearchResult> {

	private Pattern patterns[] = null;
	
	public List<String> getPatterns() {
		ArrayList<String> s = new ArrayList<String>();
		for(Pattern p : patterns) {
			s.add(p.pattern());
		}
		return s;
	}

	public void setPatterns(List<String> patternStrings) {
		int size = patternStrings.size();
		patterns = new Pattern[size];
		for(int i = 0; i < size; i++) {
			patterns[i] = Pattern.compile(patternStrings.get(i));
		}
	}
	
	public int filterObject(CaptureSearchResult o) {
		final String file = o.getFile();
		for(Pattern pattern : patterns) {
			if(pattern.matcher(file).find()) {
				return FILTER_INCLUDE;
			}
		}
		return FILTER_EXCLUDE;
	}
}
