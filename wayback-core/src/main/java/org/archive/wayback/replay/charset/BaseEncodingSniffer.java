package org.archive.wayback.replay.charset;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import org.archive.wayback.core.Resource;

/**
 * Implements common utility methods for EncodingSniffer.
 */
public abstract class BaseEncodingSniffer implements EncodingSniffer {
	protected final static String CHARSET_TOKEN = "charset=";
	protected final static String HTTP_CONTENT_TYPE_HEADER = "Content-Type";

	public abstract String sniff(Resource resource);
	/**
	 * test if {@code charsetName} is supported by Java.
	 * @param charsetName character encoding name
	 * @return {@code true} if supported.
	 */
	protected boolean isCharsetSupported(String charsetName) {
		// Charset.isSupproted throws RuntimeException if charset
		// is not supported (wow!!)
		try {
			return charsetName != null && Charset.isSupported(charsetName);
		} catch (IllegalCharsetNameException ex) {
			return false;
		}
	}

	/**
	 * return character encoding from content-type, if specified
	 * and valid.
	 * <p>some encoding names are replaced. see {@link #mapCharset(String)}</p>
	 * @param contentType content-type text, ex. {@code "text/html; charset=shift_jis"}
	 * @return character encoding, ex. {@code "shift_jis"}, or null
	 *   character encoding is unspecified, or invalid.
	 */
	protected String contentTypeToCharset(final String contentType) {
		// FIXME: should we toLowerCase() so that we don't need to
		// upper-case CHARSET_TOKEN every time.
		int offset = contentType.toUpperCase().indexOf(
			CHARSET_TOKEN.toUpperCase());

		if (offset != -1) {
			String cs = contentType.substring(offset +
					CHARSET_TOKEN.length());
			if (cs.equalsIgnoreCase("x-user-defined")) {
				cs = "windows-1252";
			}
			if (isCharsetSupported(cs)) {
				return mapCharset(cs);
			}
			// test for extra spaces... there's at least one page out there
			// that indicates it's charset with:

			// <meta http-equiv="Content-type"
			// content="text/html; charset=i so-8859-1">

			// bad web page!
			if (isCharsetSupported(cs.replace(" ", ""))) {
				return mapCharset(cs.replace(" ", ""));
			}
		}
		return null;
	}

	protected String mapCharset(String orig) {
		String lc = orig.toLowerCase();
		if (lc.contains("iso8859-1") || lc.contains("iso-8859-1")) {
			return "windows-1252";
		}
		if (lc.contains("unicode")) {
			return CharsetDetector.DEFAULT_CHARSET;
		}
		return orig;
	}
}