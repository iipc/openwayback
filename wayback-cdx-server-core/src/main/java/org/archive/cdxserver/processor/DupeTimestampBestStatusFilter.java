package org.archive.cdxserver.processor;

import org.apache.commons.lang.math.NumberUtils;
import org.archive.cdxserver.CDXServer;
import org.archive.format.cdx.CDXLine;

/**
 * Performs <i>timestamp-based collapsing</i>, that is to group
 * CDX lines by {@code timestamp} prefix and filter out all but just
 * one CDX line per group.
 * <p>Timestamp prefix is specified in terms of the number of digits
 * (from left). If two CDX lines have {@code timestamp}s whose prefixes are
 * identical, they are considered to be in the same group.</p>
 * <p>It picks the first CDX line with the best (i.e. smallest)
 * {@code statuscode} field within each group.</p>
 * <p>CDX lines with {@code filename} that starts with any of prefixes
 * specified in {@code noCollapsePrefix} are written out regardless of its
 * {@code timestamp} or {@code statuscode}, in addition to the one picked for
 * the group.</p>
 * <p>Instantiated by {@link CDXServer} as part of CDX line processing pipeline.</p>
 */
public class DupeTimestampBestStatusFilter extends WrappedProcessor {
	final static int WORST_HTTP_CODE = 9999;

	protected String lastTimestamp;

	protected int bestHttpCode = WORST_HTTP_CODE;
	protected int timestampDedupLength;

	protected String[] noCollapsePrefix;

	public DupeTimestampBestStatusFilter(BaseProcessor output,
			int timestampDedupLength, String[] noCollapsePrefix) {
		super(output);
		this.timestampDedupLength = timestampDedupLength;
		this.noCollapsePrefix = noCollapsePrefix;
	}

	/**
	 * Return {@code true} if {@code line} is to be passed through,
	 * as specified by {@code noCollapsePrefix}.
	 * <p>Soft-blocked captures are also passed-through.</p>
	 * @param line CDX line
	 * @return boolean
	 */
	protected final boolean passThrough(CDXLine line) {
		return isBlocked(line) || noCollapse(line);
	}

	protected final boolean isBlocked(CDXLine line) {
		String robotflags = line.getRobotFlags();
		// TODO: give 'X' a constant symbol - CaptureSearchResult.CAPTURE_ROBOT_BLOCKED
		// is exactly that, but wayback-cdx-server cannot use it.
		return robotflags != null && robotflags.indexOf('X') >= 0;
	}

	protected final boolean noCollapse(CDXLine line) {
		if (noCollapsePrefix != null) {
			for (String prefix : noCollapsePrefix) {
				if (line.getFilename().startsWith(prefix)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int writeLine(CDXLine line) {
		if (include(line)) {
			return super.writeLine(line);
		} else {
			return 0;
		}
	}

	protected boolean include(CDXLine line) {

		if (timestampDedupLength <= 0) {
			return true;
		}

		// If starts with special no collapse prefix, then always include
		if (passThrough(line)) return true;

		String timestamp = line.getTimestamp();
		timestamp = timestamp.substring(0, Math.min(timestampDedupLength, timestamp.length()));
		int httpCode = NumberUtils.toInt(line.getStatusCode(), WORST_HTTP_CODE);

		boolean isDupe = false;

		if ((lastTimestamp != null) && timestamp.equals(lastTimestamp)) {
			if (httpCode < bestHttpCode) {
				bestHttpCode = httpCode;
			} else {
				isDupe = true;
			}
		} else {
			bestHttpCode = httpCode;
		}

		lastTimestamp = timestamp;

		return !isDupe;
	}
}
