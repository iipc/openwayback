package org.archive.wayback.resourceindex;

import java.util.HashMap;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
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
implements Adapter<SearchResult,SearchResult> {
	private final static String EMPTY_VALUE = "-";

	// these fields are all copied to deduped records as-is:
	private final static String FIELDS[] = {
		WaybackConstants.RESULT_ARC_FILE,
		WaybackConstants.RESULT_OFFSET,
		WaybackConstants.RESULT_HTTP_CODE,
		WaybackConstants.RESULT_MIME_TYPE,
		WaybackConstants.RESULT_REDIRECT_URL,
	};
	private HashMap<String,SearchResult> memory = null;

	public DeduplicationSearchResultAnnotationAdapter() {
		memory = new HashMap<String,SearchResult>();
	}

	private SearchResult annotate(SearchResult o) {
		String thisDigest = o.get(WaybackConstants.RESULT_MD5_DIGEST);
		SearchResult last = memory.get(thisDigest);
		if(last == null) {
			return null;
		}
		for(String field : FIELDS) {
			o.put(field, last.get(field));
		}
		o.put(WaybackConstants.RESULT_DUPLICATE_ANNOTATION, 
				WaybackConstants.RESULT_DUPLICATE_DIGEST);
		o.put(WaybackConstants.RESULT_DUPLICATE_STORED_DATE, 
				last.get(WaybackConstants.RESULT_CAPTURE_DATE));
		return o;
	}

	private SearchResult remember(SearchResult o) {
		memory.put(o.get(WaybackConstants.RESULT_MD5_DIGEST),o);
		return o;
	}

	public SearchResult adapt(SearchResult o) {
		if(o.get(FIELDS[0]).equals(EMPTY_VALUE)) {
			return annotate(o);
		}
		return remember(o);
	}
}