package org.archive.wayback.replay.charset;

import java.io.IOException;

import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.TagMagix;

/**
 * {@link EncodingSniffer} that pre-scan byte stream for
 * {@code <meta http-equiv="content-type" ... >} tag.
 * <p>This is step 6 of WHAT-NG prescription, but decodes pre-scanned
 * content as {@code UTF-8} to simplify the code. That should okay
 * for the purpose...</p>
 * <p>CHANGE: 1.8.1 2014-07-07 override {@code UTF-16} encodings to
 * {@code UTF-8}, and x-user-defined encoding to {@code Windows-1252},
 * as prescribed by WHAT-NG.</p>
 */
public class PrescanMetadataSniffer extends BaseEncodingSniffer {
	// hand off this many bytes to the chardet library
	protected final static int MAX_CHARSET_READAHEAD = 65536;

	@Override
	public String sniff(Resource resource) {
		String charsetName = null;
		try {
			byte[] bbuffer = new byte[MAX_CHARSET_READAHEAD];
			resource.mark(MAX_CHARSET_READAHEAD);
			resource.read(bbuffer, 0, MAX_CHARSET_READAHEAD);
			resource.reset();
			// convert to UTF-8 String -- which hopefully will not mess up the
			// characters we're interested in...
			StringBuilder sb = new StringBuilder(new String(bbuffer,
					"UTF-8"));
			// HTML5 charset declaration
			String metaCharset = TagMagix.getTagAttr(sb, "META", "charset");
			if (metaCharset != null) {
				charsetName = checkCharset(metaCharset);
			}
			if (charsetName == null) {
				String metaContentType = TagMagix.getTagAttrWhere(sb, "META",
					"content", "http-equiv", "Content-Type");
				if (metaContentType != null) {
					charsetName = contentTypeToCharset(metaContentType);
				}
			}
			// override - if META says UTF-16, it's definitely wrong.
			if (charsetName != null) {
				String upped = charsetName.toUpperCase();
				if (upped.startsWith("UTF-16"))
					charsetName = "UTF-8";
			}
			return charsetName;
		} catch (IOException ex) {
			// TODO: log at FINE.
			return null;
		}
	}
}