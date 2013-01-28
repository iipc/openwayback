package org.archive.wayback.resourceindex.filters;

import java.util.LinkedHashMap;
import java.util.Map;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class DuplicateHashFilter implements ObjectFilter<CaptureSearchResult> {

	private int maxDupeHashes = 10;
	protected int maxTrackedHashes = 3;
	private int numCaptures = 0;
	private int minThreshold = 100;
	
	public class LRUHashCache extends LinkedHashMap<String, Integer>
	{
		private static final long serialVersionUID = 1L;

		public boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
	    	 return (size() > maxTrackedHashes);
	    }
	}
	
	LRUHashCache cache = new LRUHashCache();
	
	@Override
	public int filterObject(CaptureSearchResult o) {
		String thisHash = o.getDigest();
		int result = FILTER_INCLUDE;
		
		// Only start filtering after minThreshold captures
		if (++numCaptures <= minThreshold) {
			return result;
		}
				
		Integer count = cache.remove(thisHash);
		
		if (count == null) {
			cache.put(thisHash, 1);
		} else {
			if (count >= maxDupeHashes) {
				result = FILTER_EXCLUDE;
				cache.put(thisHash, count);
			} else {
				cache.put(thisHash, count + 1);
			}
		}
		
		return result;
	}


}
