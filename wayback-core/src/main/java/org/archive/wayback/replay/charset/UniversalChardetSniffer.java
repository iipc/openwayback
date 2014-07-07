package org.archive.wayback.replay.charset;

import java.io.IOException;

import org.archive.wayback.core.Resource;
import org.mozilla.universalchardet.UniversalDetector;

/**
 * {@link EncodingSniffer} that runs {@link UniversalDetector} on
 * the content.
 * <p>Note: as of version 1.0.3, UniversalDetector returns {@code null}
 * for ASCII-only text encoded as either {@code UTF-8} or {@code UTF-16}.</p>
 */
public class UniversalChardetSniffer extends BaseEncodingSniffer {
	// hand off this many bytes to the chardet library
	protected final static int MAX_CHARSET_READAHEAD = 65536;

	@Override
	public String sniff(Resource resource) {
		String charsetName = null;

		byte[] bbuffer = new byte[MAX_CHARSET_READAHEAD];
		// (1)
		UniversalDetector detector = new UniversalDetector(null);

		// (2)
		resource.mark(MAX_CHARSET_READAHEAD);
		try {
			int len = resource.read(bbuffer, 0, MAX_CHARSET_READAHEAD);
			resource.reset();
			detector.handleData(bbuffer, 0, len);
		} catch (IOException ex) {
			//
		}
		// (3)
		detector.dataEnd();
		// (4)
		charsetName = detector.getDetectedCharset();

		// (5)
		detector.reset();
		if (isCharsetSupported(charsetName)) {
			return charsetName;
		}
		return null;
	}
}