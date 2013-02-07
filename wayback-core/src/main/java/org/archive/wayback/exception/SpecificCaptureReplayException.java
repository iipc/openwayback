package org.archive.wayback.exception;

import java.util.Iterator;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;

public abstract class SpecificCaptureReplayException extends WaybackException {
	protected CaptureSearchResults results = null;
	protected CaptureSearchResult result = null;
	protected CaptureSearchResult previous = null;
	protected CaptureSearchResult next = null;
	
	protected Exception origException;
	
	public SpecificCaptureReplayException(String message) {
		super(message);
	}

	public SpecificCaptureReplayException(String message, String title,
			String details) {
		super(message, title, details);
	}

	public SpecificCaptureReplayException(String message, String title) {
		super(message, title);
	}
	public void setCaptureContext(CaptureSearchResults results, CaptureSearchResult result) {

        Iterator<CaptureSearchResult> itr = results.iterator();
        previous = null;
        next = null;
        this.result = result;
        while(itr.hasNext()) {
                CaptureSearchResult cur = itr.next();
                if(cur.isClosest()) {
                        break;
                }
                previous = cur;
        }
        if(itr.hasNext()) {
                next = itr.next();
        }

	}
	public CaptureSearchResults getCaptureSearchResults() {
		return results;
	}
	public CaptureSearchResult getCaptureSearchResult() {
		return result;
	}
	public CaptureSearchResult getNextResult() {
		return next;
	}
	public CaptureSearchResult getPreviousResult() {
		return previous;
	}
	public Exception getOrigException()
	{
		return origException;
	}
}
