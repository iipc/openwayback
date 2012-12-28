package org.archive.wayback.resourceindex.filters;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

public class DuplicateHashFilter implements ObjectFilter<CaptureSearchResult> {

	private String lastHash = null;
	private int maxDupeHashes = 10;
	private int count = 0;
	
	@Override
	public int filterObject(CaptureSearchResult o) {
		String thisHash = o.getDigest();
		int result = FILTER_INCLUDE;
		if (lastHash != null) {
			if (lastHash.equals(thisHash) && !o.isDuplicateDigest()) {
				count++;
				if (count >= maxDupeHashes) {
					result = FILTER_EXCLUDE;
				}
			} else {
				count = 0;
			}
		}
		lastHash = thisHash;
		return result;
	}


}
