/* CDXFormatIndex
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourceindex.cdx;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.format.CDXFormat;
import org.archive.wayback.resourceindex.cdx.format.CDXFormatException;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.CloseableIterator;

public class CDXFormatIndex extends CDXIndex {
	public final static String CDX_HEADER_MAGIC = " CDX N b a m s k r M V g";

	private CDXFormat cdx = null;
	private long lastMod = -1;
	
	protected CloseableIterator<CaptureSearchResult> adaptIterator(Iterator<String> itr) 
	throws IOException {
		
		long nowMod = file.lastModified();
		if(nowMod > lastMod) {
			try {
				// BUGBUG: I don't think java will let us do much better than
				// this... No way to stat() a filehandle, right?
				BufferedReader fr = new BufferedReader(new FileReader(file));
				cdx = new CDXFormat(fr.readLine());
				lastMod = nowMod;
				fr.close();
			} catch (CDXFormatException e) {
				lastMod = -1;
				try {
					cdx = new CDXFormat(CDX_HEADER_MAGIC);
				} catch (CDXFormatException e1) {
					throw new IOException(e1);
				}
			}
		}
		return new AdaptedIterator<String,CaptureSearchResult>(itr,
				new CDXFormatToSearchResultAdapter(cdx));
	}
}
