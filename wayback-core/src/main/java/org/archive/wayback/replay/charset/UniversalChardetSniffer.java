package org.archive.wayback.replay.charset;

import java.io.IOException;

import org.archive.wayback.core.Resource;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * {@link EncodingSniffer} that runs {@link icu4j CharacterDetector} on
 * the content.
 */
public class UniversalChardetSniffer extends BaseEncodingSniffer {
	// hand off this many bytes to the chardet library
	protected final static int MAX_CHARSET_READAHEAD = 204800;

	@Override
	public String sniff(Resource resource) {
		String charsetName = null;

		byte[] bbuffer = new byte[MAX_CHARSET_READAHEAD];

		CharsetDetector detector = new CharsetDetector();

		resource.mark(MAX_CHARSET_READAHEAD);
		try {
			resource.read(bbuffer, 0, MAX_CHARSET_READAHEAD);
			resource.reset();

			detector.setText(bbuffer);
			CharsetMatch[] matches = detector.detectAll();
			if (matches != null && matches.length > 0) {
				charsetName = matches[0].getName();
			}

		} catch (IOException ex) {
			//
		}
		if (isCharsetSupported(charsetName)) {
			return charsetName;
		}
		return null;
	}
}
