package org.archive.wayback.resourceindex.cdxserver;

import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.processor.GroupCountProcessor;
import org.archive.format.cdx.CDXLine;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.UrlSearchResult;
import org.archive.wayback.core.UrlSearchResults;

public class CDXToUrlSearchResultWriter extends CDXToSearchResultWriter {

	/**
	 * Initialize with query parameters.
	 * @param query CDXQuery
	 */
	public CDXToUrlSearchResultWriter(CDXQuery query) {
		super(query);
	}

	UrlSearchResults results;
	int count;

	@Override
	public void begin() {
		results = new UrlSearchResults();
		count = 0;
	}

	@Override
	public int writeLine(CDXLine line) {
		UrlSearchResult result = new UrlSearchResult();

		result.setUrlKey(line.getUrlKey());
		result.setOriginalUrl(line.getOriginalUrl());
		result.setFirstCapture(line.getTimestamp());

		result.setLastCapture(line.getField(GroupCountProcessor.endtimestamp));
		result.setNumCaptures(line.getField(GroupCountProcessor.groupcount));
		result.setNumVersions(line.getField(GroupCountProcessor.uniqcount));

		results.addSearchResult(result);
		++count;

		return 1;
	}

	@Override
	public void end() {
		results.setReturnedCount(count);
		results.setMatchingCount(count);
	}

	@Override
	public SearchResults getSearchResults() {
		return results;
	}

}
