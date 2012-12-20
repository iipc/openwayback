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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.archive.util.zip.OpenJDK7GZIPInputStream;
import org.archive.wayback.util.ByteOp;

/**
 * @author brad
 *
 */
public class ZiplinedBlock {
	private static final Logger LOGGER = Logger.getLogger(
			ZiplinedBlock.class.getName());

	BlockLoader loader = null;
	String urlOrPaths[] = null;
	long offset = -1;
	public final static int BLOCK_SIZE = 128 * 1024;
	int count = BLOCK_SIZE;
	public final static String RANGE_HEADER = "Range";
	public final static String BYTES_HEADER = "bytes=";
	public final static String BYTES_MINUS = "-";
	/**
	 * @param urlOrPath URL where this file can be downloaded
	 * @param offset start of 128K block boundary.
	 */
	public ZiplinedBlock(String urlOrPaths[], long offset) {
		this(urlOrPaths,offset,BLOCK_SIZE);
	}
	/**
	 * @param urlOrPath URL where this file can be downloaded
	 * @param offset start of 128K block boundary.
	 * @param count number of records in this block
	 */
	public ZiplinedBlock(String urlOrPaths[], long offset, int count) {
		this.urlOrPaths = urlOrPaths;
		this.offset = offset;
		this.count = count;
	}
	/**
	 * @param loader the RemoteHttp11BlockLoader to use when fetching this block
	 */
	public void setLoader(BlockLoader loader) {
		this.loader = loader;
	}
	/**
	 * @return a BufferedReader of the underlying compressed data in this block
	 * @throws IOException for usual reasons
	 */
	public BufferedReader readBlock() throws IOException {
		if(loader != null) {
			return readBlockEfficiently(loader);
		}
		return readBlockInefficiently();
	}
	protected byte[] attemptBlockLoad(BlockLoader remote) {
		for(String urlOrPath : urlOrPaths) {
			try {
				return remote.getBlock(urlOrPath, offset, count);
			} catch (IOException e) {
				LOGGER.warning(String.format("FAILED to load(%s) (%d:%d)",
						urlOrPath,offset,count));
			}
		}
		return null;
	}
	
	protected BufferedReader readBlockEfficiently(BlockLoader remote)
	throws IOException {
		byte bytes[] = attemptBlockLoad(remote);
		if(bytes == null) {
			throw new IOException("Unable to load block!");
		}
		return new BufferedReader(new InputStreamReader(
				new OpenJDK7GZIPInputStream(new ByteArrayInputStream(bytes)),
				ByteOp.UTF8));
//		return new BufferedReader(new InputStreamReader(
//				new GZIPInputStream(new ByteArrayInputStream(bytes)),
//				ByteOp.UTF8));
	}
	protected BufferedReader readBlockInefficiently() throws IOException {
		StringBuilder sb = new StringBuilder(16);
		sb.append(BYTES_HEADER).append(offset).append(BYTES_MINUS);
		sb.append((offset + count)-1);
		LOGGER.fine("Reading block:" + urlOrPaths[0] + "("+sb.toString()+")");
		// TODO: timeouts
		URL u = new URL(urlOrPaths[0]);
		URLConnection uc = u.openConnection();
		uc.setRequestProperty(RANGE_HEADER, sb.toString());
		return new BufferedReader(new InputStreamReader(
				new GZIPInputStream(uc.getInputStream()),ByteOp.UTF8));
	}
}
