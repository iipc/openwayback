/* ZiplinedBlock
 *
 * $Id$:
 *
 * Created on Nov 23, 2009.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.resourceindex.ziplines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

/**
 * @author brad
 *
 */
public class ZiplinedBlock {
	String urlOrPath = null;
	long offset = -1;
	public final static int BLOCK_SIZE = 128 * 1024;
	private final static String RANGE_HEADER = "Range";
	private final static String BYTES_HEADER = "bytes=";
	private final static String BYTES_MINUS = "-";
	/**
	 * @param urlOrPath URL where this file can be downloaded
	 * @param offset start of 128K block boundary.
	 */
	public ZiplinedBlock(String urlOrPath, long offset) {
		this.urlOrPath = urlOrPath;
		this.offset = offset;
	}
	/**
	 * @return a BufferedReader of the underlying compressed data in this block
	 * @throws IOException for usual reasons
	 */
	public BufferedReader readBlock() throws IOException {
		URL u = new URL(urlOrPath);
		URLConnection uc = u.openConnection();
		StringBuilder sb = new StringBuilder(16);
		sb.append(BYTES_HEADER).append(offset).append(BYTES_MINUS);
		sb.append((offset + BLOCK_SIZE)-1);
		uc.setRequestProperty(RANGE_HEADER, sb.toString());
		return new BufferedReader(new InputStreamReader(
				new GZIPInputStream(uc.getInputStream())));
	}
}
