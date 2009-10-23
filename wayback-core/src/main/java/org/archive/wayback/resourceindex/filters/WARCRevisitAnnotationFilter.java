package org.archive.wayback.resourceindex.filters;

import java.util.HashMap;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;

/**
 * Filter class that observes a stream of SearchResults tracking for each
 * complete record, a mapping of that records Digest to:
 *   Arc/Warc Filename
 * 	 Arc/Warc offset
 *   HTTP Response
 *   MIME-Type
 *   Redirect URL
 *   
 * If subsequent SearchResults are missing these fields ("-") and the Digest 
 * field is in the map, then the SearchResults missing fields are replaced with 
 * the values from the previously seen record with the same digest, and an 
 * additional annotation field is added.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class WARCRevisitAnnotationFilter 
implements ObjectFilter<CaptureSearchResult> {
	
	private final static String EMPTY_VALUE = "-";
	private final static String REVISIT_VALUE = "warc/revisit";

	private HashMap<String,CaptureSearchResult> memory = null;

	public WARCRevisitAnnotationFilter() {
		memory = new HashMap<String,CaptureSearchResult>();
	}

	private int annotate(CaptureSearchResult o) {
		String thisDigest = o.getDigest();
		CaptureSearchResult last = memory.get(thisDigest);
		if(last == null) {
			// TODO: log missing record digest reference?
			return FILTER_EXCLUDE;
		}
		o.setFile(last.getFile());
		o.setOffset(last.getOffset());
		o.setHttpCode(last.getHttpCode());
		o.setMimeType(last.getMimeType());
		o.setRedirectUrl(last.getRedirectUrl());
		o.flagDuplicateDigest(last.getCaptureTimestamp());
		return FILTER_INCLUDE;
	}

	private int remember(CaptureSearchResult o) {
		memory.put(o.getDigest(),o);
		return FILTER_INCLUDE;
	}

//	public CaptureSearchResult adapt(CaptureSearchResult o) {
//		if(o.getFile().equals(EMPTY_VALUE)
//				|| o.getMimeType().equals(REVISIT_VALUE)) {
//			return annotate(o);
//		}
//		return remember(o);
//	}

	public int filterObject(CaptureSearchResult o) {
		if(o.getFile().equals(EMPTY_VALUE)
				|| o.getMimeType().equals(REVISIT_VALUE)) {
			return annotate(o);
		}
		return remember(o);
	}
}
