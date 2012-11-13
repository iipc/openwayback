package org.archive.wayback.resourceindex.ziplines;

import java.util.Iterator;

public class StringBoundedRangeIterator extends StringPrefixIterator {
	
	protected String startMatch;
	protected String endMatch;
	protected boolean endPrefixInclusive;
	
	/**
	 * 
	 * @param inner - underlying cdx line iterator
	 * @param startMatch - string match to start with (inclusive)
	 * @param endMatch - end match to end with (exclusive or inclusive)
	 * @param endPrefixInclusive - specify if end match is exclusive or inclusive (if inclusive, use startsWith to check the end)
	 */
	public StringBoundedRangeIterator(Iterator<String> inner, String startMatch, String endMatch, boolean endPrefixInclusive) {
		super(inner, startMatch);
		this.startMatch = startMatch;
		this.endMatch = endMatch;
		this.endPrefixInclusive = endPrefixInclusive;
		
		if (startMatch.compareTo(endMatch) > 0) {
			throw new RuntimeException("StringBoundRangeIterator: start > end" + startMatch + " > " + endMatch);
		}
	}

	@Override
	public boolean hasNext() {
		if (done) {
			return false;
		}
		
		if (cachedNext != null) {
			return true;
		}
		
		while(inner.hasNext()) {
			String currLine = inner.next();
			
			// If past the end line, and doesn't start with
			// endMatch, we're done
			if ((currLine.compareTo(endMatch) > 0) &&
				!currLine.startsWith(endMatch)) {
				done = true;
				return false;
			}
			
			// If currLine before startMatch, skip over
			if (currLine.compareTo(startMatch) < 0) {
				continue;
			}
			
			// Otherwise, startMatch <= currLine < endMatch
			cachedNext = currLine;
			return true;
		}
		
		return false;
	}

	
	
}
