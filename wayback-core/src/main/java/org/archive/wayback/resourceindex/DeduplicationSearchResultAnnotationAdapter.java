package org.archive.wayback.resourceindex;

import java.util.HashMap;

import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.Adapter;

/**
 * Adapter class that observes a stream of SearchResults tracking the last seen:
 *   Arc/Warc Filename
 * 	 Arc/Warc offset
 *   HTTP Response
 *   MIME-Type
 *   Redirect URL
 *   
 * for complete SearchResults. If subsequent SearchResults are missing these
 * fields ("-") and the Digest field is the same, then the subsequent 
 * SearchResults are updated with the values from the kept copy, and an 
 * additional annotation field is added.
 * 
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class DeduplicationSearchResultAnnotationAdapter 
implements Adapter<SearchResult,SearchResult> {
	private final static String EMPTY_VALUE = "-";
	private final static String FIELDS[] = {
		WaybackConstants.RESULT_ARC_FILE,
		WaybackConstants.RESULT_OFFSET,
		WaybackConstants.RESULT_HTTP_CODE,
		WaybackConstants.RESULT_MIME_TYPE,
		WaybackConstants.RESULT_REDIRECT_URL
	};
	private String lastDigest = null;
	private HashMap<String,String> lastValues = new HashMap<String,String>();
	private SearchResult annotate(SearchResult o) {
		String thisDigest = o.get(WaybackConstants.RESULT_MD5_DIGEST);
		if(!thisDigest.equals(lastDigest)) {
			return null;
		}
		for(String field : FIELDS) {
			o.put(field, lastValues.get(field));
		}
		o.put(WaybackConstants.RESULT_DUPLICATE_ANNOTATION, 
				WaybackConstants.RESULT_DUPLICATE_DIGEST);
		return o;
	}
	private SearchResult remember(SearchResult o) {
		lastDigest = o.get(WaybackConstants.RESULT_MD5_DIGEST);
		for(String field : FIELDS) {
			lastValues.put(field, o.get(field));
		}
		return o;
	}
	public SearchResult adapt(SearchResult o) {
		if(o.get(FIELDS[0]).equals(EMPTY_VALUE)) {
			return annotate(o);
		}
		return remember(o);
	}

}
