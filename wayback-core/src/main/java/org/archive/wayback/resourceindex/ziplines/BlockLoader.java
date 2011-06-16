package org.archive.wayback.resourceindex.ziplines;

import java.io.IOException;

public interface BlockLoader {
	/**
	 * Fetch a range of bytes from a particular URL. Note that the bytes are
	 * read into memory all at once, so care should be taken with the length
	 * argument.
	 * 
	 * @param url String URL to fetch
	 * @param offset byte start offset of the desired range
	 * @param length number of octets to fetch
	 * @return a new byte[] containing the octets fetched
	 * @throws IOException on Network and protocol failures, as well as Timeouts
	 */
	public byte[] getBlock(String url, long offset, int length) 
	throws IOException;
}
