package org.archive.wayback.resourceindex.cdxserver;

import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.writer.CDXWriter;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.SearchResults;

/**
 * Receive {@link CDXLine}s, produce {@link SearchResults}.
 * <p>
 * Used for implementing {@link ResourceIndex} on top of {@link CDXServer}.
 * </p>
 */
public abstract class CDXToSearchResultWriter extends CDXWriter {

	protected CDXQuery query;
	protected String msg = null;

	public CDXToSearchResultWriter() {
	}
	/**
	 * @param query
	 * @deprecated 2015-09-04 use no-arg constructor. query member will be dropped. 
	 */
	public CDXToSearchResultWriter(CDXQuery query) {
		this.query = query;
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub
	}

	@Override
	public void writeResumeKey(String resumeKey) {
		// TODO Auto-generated method stub
	}

	@Override
	public void end() {
		// TODO Auto-generated method stub
	}

	public abstract SearchResults getSearchResults();

	/**
	 * @deprecated 2015-09-04 query member will be dropped
	 * @return
	 */
	public CDXQuery getQuery() {
		return query;
	}

	@Override
	public void printError(String msg) {
		this.msg = msg;
	}

	public String getErrorMsg() {
		return msg;
	}

	@Override
	public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
		return format;
	}
}
