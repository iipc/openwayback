package org.archive.wayback.replay.charset;

import java.util.Map;
import java.util.Map.Entry;

import org.archive.wayback.core.Resource;

/**
 * {@link EncodingSniffer} obtaining character encoding from
 * {@code Content-Type} HTTP header.
 * <p>Step 4 of WHAT-NG <em>character encoding sniffing</em> prescription.
 */
public class ContentTypeHeaderSniffer extends BaseEncodingSniffer {
	@Override
	public String sniff(Resource resource) {
		Map<String, String> httpHeaders = resource.getHttpHeaders();
		for (Entry<String, String> e : httpHeaders.entrySet()) {
			String headerKey = e.getKey();
			if (headerKey.equalsIgnoreCase(HTTP_CONTENT_TYPE_HEADER)) {
				String ctype = e.getValue();
				String charsetName = contentTypeToCharset(ctype);
				return charsetName;
			}
		}
		return null;
	}
}