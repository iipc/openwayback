package org.archive.wayback.exception;

import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.Resource;

/**
 * RangeNotSatisfiableException is thrown when selected Resource does not have content data
 that can satisfy requested range.
 * This happens when the Resource itself is a capture of range request that does not cover
 * requested range.
 * @see org.archive.wayback.replay.TransparentReplayRenderer
 */
public class RangeNotSatisfiableException extends SpecificCaptureReplayException {
	private static final long serialVersionUID = 1L;

	private Resource origResource;
	private long[][] requestedRanges;

	public RangeNotSatisfiableException(Resource origResource, long[][] requestedRanges, String message) {
		super(message, "RangeNotSatisfiable");
		this.origResource = origResource;
		this.requestedRanges = requestedRanges;
	}

	@Override
	public int getStatus() {
		return HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE;
	}

	public Resource getOrigResource() {
		return origResource;
	}

	public long[][] getRequestedRanges() {
		return requestedRanges;
	}
}
