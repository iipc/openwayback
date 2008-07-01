package org.archive.wayback.resourceindex;

import java.util.HashMap;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;

/**
 * Adapter class that observes a stream of SearchResults tracking for each
 * complete record, a mapping of that records digest to:
 *   Arc/Warc Filename
 * 	 Arc/Warc offset
 *   HTTP Response
 *   MIME-Type
 *   Redirect URL
 *   
 * If subsequent SearchResults are missing these fields ("-") and the Digest 
 * field has been seen, then the subsequent SearchResults are updated with the 
 * values from the kept copy matching that digest, and an additional annotation
 * field is added.
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class DeduplicationSearchResultAnnotationAdapter 
implements Adapter<CaptureSearchResult,CaptureSearchResult> {
	private final static String EMPTY_VALUE = "-";

	private HashMap<String,CaptureSearchResult> memory = null;

	public DeduplicationSearchResultAnnotationAdapter() {
		memory = new HashMap<String,CaptureSearchResult>();
	}

	private CaptureSearchResult annotate(CaptureSearchResult o) {
		String thisDigest = o.getDigest();
		CaptureSearchResult last = memory.get(thisDigest);
		if(last == null) {
			// TODO: log missing record digest reference
			return null;
		}
		o.setFile(last.getFile());
		o.setOffset(last.getOffset());
		o.setHttpCode(last.getHttpCode());
		o.setMimeType(last.getMimeType());
		o.setRedirectUrl(last.getRedirectUrl());
		o.flagDuplicateDigest(last.getCaptureTimestamp());
		return o;
	}

	private CaptureSearchResult remember(CaptureSearchResult o) {
		memory.put(o.getDigest(),o);
		return o;
	}

	public CaptureSearchResult adapt(CaptureSearchResult o) {
		if(o.getFile().equals(EMPTY_VALUE)) {
			return annotate(o);
		}
		return remember(o);
	}
}