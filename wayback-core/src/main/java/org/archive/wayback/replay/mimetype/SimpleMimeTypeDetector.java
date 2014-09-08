package org.archive.wayback.replay.mimetype;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.charset.CharsetDetector;
import org.archive.wayback.replay.charset.StandardCharsetDetector;

/**
 * Simple {@link MimeTypeDetector} implementation.
 * It's ad-hoc and not customizable, just tested against many samples.
 */
public class SimpleMimeTypeDetector implements MimeTypeDetector {
	public static final int SNIFF_LENGTH = 1024;
	private CharsetDetector charsetDetector = new StandardCharsetDetector();

	/**
	 * Return {@code true} if {@code bytes} looks like a beginning of a
	 * binary file.
	 * <p>Looks for well-known binary format MAGICs.</p>
	 * @param bytes array of bytes.
	 * @return detected mimetype, or {@code null} if no binary
	 * format is detected.
	 */
	private String detectBinaryTypes(byte[] bytes) {
		// NB: there are a lot of specific mimetypes we can detect by file
		// magic, but we don't want to make too much effort here.
		// "application/octet-stream" is fine if file looks very much like
		// binary but don't know what exactly. Returned value is not really
		// used beyond being non-null.
		if (bytes[0] == (byte)0xFE) {
			if (bytes[1] == (byte)0xFF) {
				// UTF-16LE BOM
				return null;
			}
		} else if (bytes[0] == (byte)0xFF) {
			if (bytes[1] == (byte)0xFE) {
				// UTF-16BE BOM
				return null;
			} else if ((bytes[1] & 0xFE) == (byte)0xFA) {
				// audio/mp3
				return "audio/mp3";
			} else if (bytes[1] == (byte)0xD8) {
				// image/jpeg - commonly <FF><D8><FF><E0><00><10>JFIF
				return "image/jpeg";
			} else {
				// WordPerfect (<FF>WPC) falls in this category.
				// (WordPerfect also has <D8>WPC
				return "application/octet-stream";
			}
		} else if (bytes[0] == (byte)0xEF && bytes[1] == (byte)0xBB && bytes[2] == (byte)0xBF) {
			// UTF-8 BOM
			return null;
		} else if (bytes[0] == (byte)0xF7 && bytes[1] == 0x02 && bytes[2] == 0x01) {
			// unconfirmed.
			return "application/x-dvi";
		} else if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == '8') {
			return "image/gif";
		} else if (bytes[0] == 'm' && bytes[1] == 'o' && bytes[2] == 'o' && bytes[3] == 'v') {
			return "video/quicktime";
		} else if (bytes[0] == 'm' && bytes[1] == 'd' && bytes[2] == 'a' && bytes[3] == 't') {
			return "video/quicktime";
		} else if (bytes[0] == 'P' && bytes[1] == 'K' && bytes[2] == 0x03 && bytes[3] == 0x04) {
			return "application/zip";
		} else if (bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F' && bytes[4] == '-') {
			return "application/pdf";
		} else if (bytes[0] == 'P' && bytes[1] == 'E' && bytes[2] == 0x00 && bytes[3] == 0x00 && bytes[4] == 'M' && bytes[5] == 'S') {
			// Windows PE
			return "application/octet-stream";
		} else if (bytes[0] == 'M' && bytes[1] == 'Z' && bytes[2] == (byte)0x90 && bytes[3] == 0x00) {
			// Windows Executable
			return "application/octet-stream";
		} else if (bytes[0] == 'M' && bytes[1] == 'T' && bytes[3] == 'h' && bytes[3] == 'd') {
			// MIDI
			return "audio/midi"; // or "application/x-midi"
		} else if (bytes[0] == 0x1F && bytes[1] == (byte)0x8B) {
			return "application/x-gzip";
		} else if (bytes[0] == 'B' && bytes[1] == 'Z' && bytes[2] == 'h') {
			return "application/x-bzip2";
		} else if (bytes[0] == 'F' && bytes[1] == 'W' && bytes[2] == 'S') {
			return "application/x-shockwave-flash";
		} else if (bytes[0] == '%' && bytes[1] == '!' && bytes[2] == 'P' && bytes[3] == 'S' && bytes[4] == '-') {
			// application/postscript, Type 1 font.
			return "application/postscript";
		} else if (bytes[0] == 0x01) {
			if (bytes[1] == (byte)0xB3 || bytes[1] == (byte)0xBA) {
				return "video/mpeg";
			}
		} else if (bytes[0] == (byte)0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
			return "image/png";
		} else if (bytes[0] == (byte)0xCA && bytes[1] == (byte)0xFE && bytes[2] == (byte)0xBA && bytes[3] == (byte)0xBE) {
			// application/java (class files)
			return "application/java";
		}
		return null;
	}

	@Override
	public String sniff(Resource resource) {
		// This sniffer only works with HTTP response record.
		// TODO: check record type.

		byte[] bbuffer = new byte[SNIFF_LENGTH];
		resource.mark(SNIFF_LENGTH);
		try {
			resource.read(bbuffer, 0, SNIFF_LENGTH);
			resource.reset();
		} catch (IOException ex) {
			// TODO: log
			return null;
		}

		// Spare decoding and regexp-matching for clearly-binary files.
		// Most mimetype detector libraries are overkill since we don't
		// need to know the details (ex. bitrate of MP3, PDF version).
		// So we use our own detector.
		String ctype = detectBinaryTypes(bbuffer);
		if (ctype != null) {
			// Very like be a binary file.
			return ctype;
		}

		// Try decoding as text and look for signature patterns.
		// It doesn't need to be too complex, since want to differentiate
		// only a handful of text types: HTML, JAVASCRIPT, JSON and CSS.
		String encoding;
		try {
			encoding = charsetDetector.getCharset(resource, null);
		} catch (IOException ex1) {
			// IO error at this stage means we won't be able to sniff
			// content type either.
			// TODO: log
			return null;
		}
		System.err.println("detected encoding: " + encoding);
		String text;
		try {
			text = new String(bbuffer, encoding);
		} catch (UnsupportedEncodingException ex) {
			// likely to happen, already checked by CharsetDetector.
			return null;
		}
		{
			Matcher m = Pattern.compile("\\s*<\\?xml\\s+version=\"[.\\d]+\"\\s+.*\\?>").matcher(text);
			if (m.lookingAt()) {
				text = text.substring(m.end());
			}
		}
		{
			Matcher m = Pattern.compile("(?s)\\s*<!--.*?-->").matcher(text);
			if (m.lookingAt()) {
				text = text.substring(m.end());
			}
		}
		{
			Matcher m = Pattern.compile("\\s*<!DOCTYPE\\s+(html|HTML)").matcher(text);
			if (m.lookingAt())
				return "text/html";
		}
		{
			Matcher m = Pattern.compile("(?i)\\s*<(HTML|HEAD|STYLE|SCRIPT|META)(\\s|>)").matcher(text);
			if (m.lookingAt())
				return "text/html";
		}
		{
			Matcher m = Pattern.compile("(?m)^var\\s+[_a-zA-Z$][_a-zA-Z$0-9]+").matcher(text);
			if (m.find())
				return "text/javascript";
		}
		{
			Matcher m = Pattern.compile("\\s*\\{\\s*\"").matcher(text);
			if (m.lookingAt()) {
				// TODO: if resource has content-type "text/javascript", just use it.
				return "application/json";
			}
		}
		// CSS (they are rarely returned with mimetype "unk" or "text/html")
		{
			Matcher m = Pattern.compile("(?i)(font-family|font-size|margin|padding|text-align|text-decoration):[^}]+;").matcher(text);
			if (m.find()) {
				return "text/css";
			}
		}
		{
			Matcher m = Pattern.compile("(?i)\\s*[-a-z]*([a-z]|[.#][-a-z0-9]+)\\s*\\{").matcher(text);
			if (m.lookingAt()) {
				return "text/css";
			}
		}
		{
			Matcher m = Pattern.compile("\\s*@import").matcher(text);
			if (m.lookingAt()) {
				return "text/css";
			}
		}
		return null;
	}
}