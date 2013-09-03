package org.archive.cdxserver.filter;

public interface CDXAccessFilter extends CDXFilter {
	// Filter by url, not specific to capture
	public boolean include(String urlKey, String originalUrl);
}
