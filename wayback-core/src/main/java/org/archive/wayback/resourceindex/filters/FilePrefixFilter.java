package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class FilePrefixFilter implements ObjectFilter<CaptureSearchResult> {

	private String prefixes[] = null;
	
	public String[] getPrefixes() {
		return prefixes;
	}
	public void setPrefixes(String[] prefixes) {
		this.prefixes = prefixes;
	}
	
	public int filterObject(CaptureSearchResult o) {
		final String file = o.getFile();
		for(String prefix : prefixes) {
			if(file.startsWith(prefix)) {
				return FILTER_INCLUDE;
			}
		}
		return FILTER_EXCLUDE;
	}
}
