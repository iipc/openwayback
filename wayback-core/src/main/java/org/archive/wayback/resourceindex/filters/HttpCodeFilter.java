/* HttpCodeFilter
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * ObjectFilter which allows including or excluding results based on the
 * Http response code. 
 * 
 * @author brad
 * @version $Date$, $Rev$
 */
public class HttpCodeFilter implements ObjectFilter<CaptureSearchResult> {

	private Map<String,Object> includes = null;
	private Map<String,Object> excludes = null;

	private static Map<String,Object> listToMap(List<String> list) {
		if(list == null) {
			return null;
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		for(String s : list) {
			map.put(s, null);
		}
		return map;
	}
	private static List<String> mapToList(Map<String,Object> map) {
		if(map == null) {
			return null;
		}
		List<String> list = new ArrayList<String>();
		list.addAll(map.keySet());
		return list;
	}
	
	public List<String> getIncludes() {
		return mapToList(includes);
	}

	public void setIncludes(List<String> includes) {
		this.includes = listToMap(includes);
	}


	public List<String> getExcludes() {
		return mapToList(excludes);
	}


	public void setExcludes(List<String> excludes) {
		this.excludes = listToMap(excludes);
	}
	
	public int filterObject(CaptureSearchResult o) {
		String code = o.getHttpCode();
		if(excludes != null) {
			if(excludes.containsKey(code)) {
				return FILTER_EXCLUDE;
			}
		}
		if(includes != null) {
			if(!includes.containsKey(code)) {
				return FILTER_EXCLUDE;				
			}
		}
		return FILTER_INCLUDE;
	}
}
