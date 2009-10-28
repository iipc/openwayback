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
