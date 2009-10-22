package org.archive.wayback.replay.charset;

import java.io.IOException;

import org.archive.wayback.core.Resource;
import org.archive.wayback.core.WaybackRequest;

public class StandardCharsetDetector extends CharsetDetector {

	@Override
	public String getCharset(Resource resource, WaybackRequest request)
	throws IOException {
		String charSet = getCharsetFromHeaders(resource);
		if(charSet == null) {
			charSet = getCharsetFromMeta(resource);
			if(charSet == null) {
				charSet = getCharsetFromBytes(resource);
				if(charSet == null) {
					charSet = DEFAULT_CHARSET;
				}
			}
		}
		return charSet;
	}
}
