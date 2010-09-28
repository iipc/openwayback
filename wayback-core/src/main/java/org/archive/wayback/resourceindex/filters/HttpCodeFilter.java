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
