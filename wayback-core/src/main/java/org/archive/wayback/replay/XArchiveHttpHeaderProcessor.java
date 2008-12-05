package org.archive.wayback.replay;

import java.util.Map;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.core.CaptureSearchResult;

public class XArchiveHttpHeaderProcessor implements HttpHeaderProcessor {

	private static String DEFAULT_PREFIX = "X-Wayback-Orig-";
	private String prefix = DEFAULT_PREFIX; 
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void filter(Map<String, String> output, String key, String value,
			ResultURIConverter uriConverter, CaptureSearchResult result) {
		String keyUp = key.toUpperCase();

		output.put(prefix + key,value);
		if (keyUp.startsWith(HTTP_CONTENT_TYPE_HEADER_UP)) {
			// add this one as-is, too.
			output.put(key, value);
		}
	}
}
