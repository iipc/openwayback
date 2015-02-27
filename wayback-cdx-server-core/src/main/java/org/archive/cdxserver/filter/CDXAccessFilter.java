package org.archive.cdxserver.filter;

import org.archive.format.cdx.CDXLine;

public interface CDXAccessFilter {
		
	// Filter by url, not specific to capture
	public boolean includeUrl(String urlKey, String originalUrl);
	
	// Filter by specific capture, implying that the includeUrl() was called before and has passed the filter
	public boolean includeCapture(CDXLine line);
}
