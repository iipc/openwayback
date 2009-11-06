package org.archive.wayback.resourceindex.cdx;

import java.util.Iterator;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;

public class SearchResultToCDXFormatAdapter implements
		Adapter<CaptureSearchResult, String> {

	private CDXFormat cdx = null;

	public SearchResultToCDXFormatAdapter(CDXFormat cdx) {
		this.cdx = cdx;
	}

	public String adapt(CaptureSearchResult o) {
		return cdx.serializeResult(o);
	}
	public static Iterator<String> adapt(Iterator<CaptureSearchResult> input,
			CDXFormat cdx) {
		SearchResultToCDXFormatAdapter adapter =
			new SearchResultToCDXFormatAdapter(cdx);
		return new AdaptedIterator<CaptureSearchResult,String>(input,adapter);
	}
}
