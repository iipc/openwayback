package org.archive.wayback.resourceindex.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

import junit.framework.TestCase;

public class FileRegexFilterTest extends TestCase {
	String[] patterns = {"^one-", "^two-"};

	public void testGetSetPatterns() {
		FileRegexFilter f = new FileRegexFilter();
		List<String> in = Arrays.asList(patterns);
		f.setPatterns(in);
		List<String> out = f.getPatterns();
		assertTrue(listCmp(in,out));
	}

	public void testFilterObject() {
		List<String> in = Arrays.asList(patterns);
		FileRegexFilter f = new FileRegexFilter();
		f.setPatterns(in);
		CaptureSearchResult c = new CaptureSearchResult();
		c.setFile("one-11");
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_INCLUDE);
		c.setFile("onedd-11");
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_EXCLUDE);
		c.setFile("two-11");
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_INCLUDE);
		f.setPatterns(new ArrayList<String>());
		assertEquals(f.filterObject(c), ObjectFilter.FILTER_EXCLUDE);
	}
	private boolean listCmp(List<String> one, List<String> two) {
		if(one.size() != two.size()) {
			return false;
		}
		int size = one.size();
		for(int i = 0; i < size; i++) {
			if(!one.get(i).equals(two.get(i))) {
				return false;
			}
		}
		return true;
	}
}
