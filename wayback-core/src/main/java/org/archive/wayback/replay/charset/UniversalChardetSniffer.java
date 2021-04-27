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
			if (matches != null) {

			    String charsetNextBest = null;
			    
			    for (int i = 0; i < matches.length; i++) {
					charsetName = matches[i].getName();
					if (!isDubious(charsetName) && isCharsetSupported(charsetName)) {
					    
					    if (charsetNextBest == null) { charsetNextBest = charsetName; }

					    // prefer UTF character sets
					    if (charsetName.startsWith("UTF-8")) { return charsetName; }
					}
				}
			    
			    return charsetNextBest;
			}
		} catch (IOException ex) {
			//
		}
		return null;
	}

	/*
	 * Pretty much nothing in the wild is really UTF-32,
	 * yet icu4j returns that as the likeliest possibility
	 * for several captures...
	 */
	protected boolean isDubious(String charsetName) {
		return charsetName.startsWith("UTF-32");
	}
}
