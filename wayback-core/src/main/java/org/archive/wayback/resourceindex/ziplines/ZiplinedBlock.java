/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.resourceindex.ziplines;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.archive.wayback.util.ByteOp;

/**
 * @author brad
 *
 */
public class ZiplinedBlock {
	private static final Logger LOGGER = Logger.getLogger(
			ZiplinedBlock.class.getName());

	String urlOrPath = null;
	long offset = -1;
	int count = 0;
	public final static int BLOCK_SIZE = 128 * 1024;
	private final static String RANGE_HEADER = "Range";
	private final static String BYTES_HEADER = "bytes=";
	private final static String BYTES_MINUS = "-";
	/**
	 * @param urlOrPath URL where this file can be downloaded
	 * @param offset start of 128K block boundary.
	 */
	public ZiplinedBlock(String urlOrPath, long offset) {
		this(urlOrPath,offset,0);
	}
	/**
	 * @param urlOrPath URL where this file can be downloaded
	 * @param offset start of 128K block boundary.
	 * @param count number of records in this block
	 */
	public ZiplinedBlock(String urlOrPath, long offset, int count) {
		this.urlOrPath = urlOrPath;
		this.offset = offset;
		this.count = count;
	}
	/**
	 * @return a BufferedReader of the underlying compressed data in this block
	 * @throws IOException for usual reasons
	 */
	public BufferedReader readBlock() throws IOException {
		StringBuilder sb = new StringBuilder(16);
		sb.append(BYTES_HEADER).append(offset).append(BYTES_MINUS);
		sb.append((offset + BLOCK_SIZE)-1);
		LOGGER.fine("Reading block:" + urlOrPath + "("+sb.toString()+")");
		// TODO: timeouts
		URL u = new URL(urlOrPath);
		URLConnection uc = u.openConnection();
		uc.setRequestProperty(RANGE_HEADER, sb.toString());
		return new BufferedReader(new InputStreamReader(
				new GZIPInputStream(uc.getInputStream()),ByteOp.UTF8));
	}
}
