package org.archive.wayback.replay.charset;

import java.io.IOException;

import org.archive.wayback.core.Resource;

/**
 * {@link EncodingSniffer} that peek the content for
 * Byte Order Mark bytes.
 * <p>This is the step 3 of character encoding sniffing
 * prescribed by WHAT-NG.</p>
 */
public class ByteOrderMarkSniffer extends BaseEncodingSniffer {
	public static final int MAX_BOM_LEN = 3;
	@Override
	public String sniff(Resource resource) {
		byte[] bbuffer = new byte[MAX_BOM_LEN];
		resource.mark(MAX_BOM_LEN);
		try {
			resource.read(bbuffer, 0, MAX_BOM_LEN);
			resource.reset();
		} catch (IOException ex) {
			return null;
		}
		if (bbuffer[0] == (byte)0xFE && bbuffer[1] == (byte)0xFF)
			return "UTF-16BE";
		if (bbuffer[0] == (byte)0xFF && bbuffer[1] == (byte)0xFE)
			return "UTF-16LE";
		if (bbuffer[0] == (byte)0xEF && bbuffer[1] == (byte)0xBB && bbuffer[2] == (byte)0xBF)
			return "UTF-8";
		return null;
	}
}