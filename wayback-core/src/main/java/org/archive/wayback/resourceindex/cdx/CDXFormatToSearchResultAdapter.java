package org.archive.wayback.resourceindex.cdx;


import org.apache.log4j.Logger;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;
import org.archive.wayback.util.Adapter;

public class CDXFormatToSearchResultAdapter implements Adapter<String,CaptureSearchResult> {
	private static final Logger LOGGER = Logger.getLogger(
			CDXFormatToSearchResultAdapter.class.getName());

	private CDXFormat cdx = null;
	public CDXFormatToSearchResultAdapter(CDXFormat cdx) {
		this.cdx = cdx;
	}

	public CaptureSearchResult adapt(String line) {
		try {
			return cdx.parseResult(line);
		} catch (CDXFormatException e) {
			LOGGER.warn("CDXFormat(" + line + "):"+e.getLocalizedMessage());
		}
		return null;
	}
}
