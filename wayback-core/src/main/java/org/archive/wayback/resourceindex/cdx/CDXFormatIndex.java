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
package org.archive.wayback.resourceindex.cdx;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.ByteOp;

public class CDXFormatIndex extends CDXIndex {
	public final static String CDX_HEADER_MAGIC = " CDX N b a m s k r M V g";

	private CDXFormat format = null;
	private long lastMod = -1;
	
	protected CloseableIterator<CaptureSearchResult> adaptIterator(Iterator<String> itr) 
	throws IOException {
		
		long nowMod = file.lastModified();
		CDXFormat cdx = format;
		if(cdx == null) {
			if(nowMod > lastMod) {
				try {
					// BUGBUG: I don't think java will let us do much better than
					// this... No way to stat() a filehandle, right?
					FileInputStream fis = new FileInputStream(file);
					InputStreamReader isr = new InputStreamReader(fis,ByteOp.UTF8);
					BufferedReader fr = new BufferedReader(isr);
					cdx = new CDXFormat(fr.readLine());
					lastMod = nowMod;
					fr.close();
				} catch (CDXFormatException e) {
					lastMod = -1;
					try {
						cdx = new CDXFormat(CDX_HEADER_MAGIC);
					} catch (CDXFormatException e1) {
						throw new IOException(e1.getMessage());
					}
				}
			}
		}
		return new AdaptedIterator<String,CaptureSearchResult>(itr,
				new CDXFormatToSearchResultAdapter(cdx));
	}

	/**
	 * @return the format
	 */
	public CDXFormat getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(CDXFormat format) {
		this.format = format;
	}
}
