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
