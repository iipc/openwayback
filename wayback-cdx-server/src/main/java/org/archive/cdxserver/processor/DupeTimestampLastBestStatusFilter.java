package org.archive.cdxserver.processor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.math.NumberUtils;
import org.archive.format.cdx.CDXLine;

/**
 * A variant of {@link DupeTimestampBestStatusFilter} that returns
 * the last best capture instead of the first one.
 * Support of {@code noCollapsePrefix} complicates processing, so this
 * may be slightly slower than {@link DupeTimestampBestStatusFilter}.
 * <p>Note that the semantics of {@link #writeLine(CDXLine)} is slightly
 * different from other processors. It returns 1 if some (non-pass-through)
 * CDX line, which is not necessarily the same as the CDX line passed as argument,
 * is written out. Count of ones would be one less than others.</p>
 * <p>Tests in {@link DupeTimestampBestStatusFilterTest}.</p>
 */
public class DupeTimestampLastBestStatusFilter extends DupeTimestampBestStatusFilter {

	/**
	 * Keeps the best CDX line so far within a group.
	 */
	protected CDXLine bestLine;
	/**
	 * Keeps a list of CDXLines that matches {@code noCollapsePreifx}, but
	 * cannot be written yet because their {@code timestamp}s are larger than
	 * {@code bestLine.timestamp}.
	 */
	protected List<CDXLine> pendingPassThroughs;

	public DupeTimestampLastBestStatusFilter(BaseProcessor output,
			int timestampDedupLength, String[] noCollapsePrefix) {
		super(output, timestampDedupLength, noCollapsePrefix);
		this.pendingPassThroughs = new ArrayList<CDXLine>();
	}

	/**
	 * Write out all pending pass-throughs, and
	 * clear pass-through buffer.
	 */
	protected final void flushPassThrough() {
		for (CDXLine line : pendingPassThroughs) {
			// NB: don't call super.writeLine()
			inner.writeLine(line);
		}
		pendingPassThroughs.clear();
	}

	/**
	 * return group key of {@code line}.
	 * @param line CDX line
	 * @return first {@code timestampDedupLength} digits
	 * of {@code timestamp}
	 */
	protected final String groupKey(CDXLine line) {
		String timestamp = line.getTimestamp();
		return timestamp.substring(0,
			Math.min(timestampDedupLength, timestamp.length()));
	}

	@Override
	public int writeLine(CDXLine line) {
		if (timestampDedupLength <= 0) {
			// NB: do not call super.writeLine()
			// same for all writeLine() calls below.
			return inner.writeLine(line);
		}
		if (passThrough(line)) {
			if (bestLine != null) {
				pendingPassThroughs.add(line);
				return 1;
			} else {
				return inner.writeLine(line);
			}
		}
		String key = groupKey(line);
		int httpCode = NumberUtils.toInt(line.getStatusCode(), WORST_HTTP_CODE);
		if (lastTimestamp != null && key.equals(lastTimestamp)) {
			// within a collapse group
			if (httpCode <= bestHttpCode) {
				flushPassThrough();
				bestLine = line;
				bestHttpCode = httpCode;
			}
			return 0;
		} else {
			// new collapse group
			int r = 0;
			if (bestLine != null) {
				// for the first line
				r = inner.writeLine(bestLine);
				flushPassThrough();
			}
			bestLine = line;
			bestHttpCode = httpCode;
			lastTimestamp = key;
			return r;
		}
	}

	@Override
	public void end() {
		// last collapse group. bestLine == null happens
		// only when writeLine() was never called, or
		// timestampDedupLength <= 0
		if (bestLine != null) {
			inner.writeLine(bestLine);
			flushPassThrough();
		}
		super.end();
	}
}
