/* ArcIndexer
 *
 * $Id$
 *
 * Created on 2:33:29 PM Oct 11, 2006.
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
package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.cdx.SearchResultToCDXLineAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.CloseableIterator;

/**
 * Transforms an ARC file into Iterator<SearchResult>.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArcIndexer {

	/**
	 * CDX Header line for these fields. not very configurable..
	 */
	public final static String CDX_HEADER_MAGIC = " CDX N b h m s k r V g";

	/**
	 * @param arc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<SearchResult> iterator(File arc)
	throws IOException {
		ARCReader arcReader = ARCReaderFactory.get(arc);
		arcReader.setParseHttpHeaders(true);

		Adapter<ArchiveRecord,ARCRecord> adapter1 =
			new ArchiveRecordToARCRecordAdapter();

		Adapter<ARCRecord,SearchResult> adapter2 =
			new ARCRecordToSearchResultAdapter();
		
		Iterator<ArchiveRecord> itr1 = arcReader.iterator();

		CloseableIterator<ARCRecord> itr2 = 
			new AdaptedIterator<ArchiveRecord,ARCRecord>(itr1,adapter1);
		
		return new AdaptedIterator<ARCRecord,SearchResult>(itr2,adapter2);
	}
	
	
	private class ArchiveRecordToARCRecordAdapter 
	implements Adapter<ArchiveRecord,ARCRecord> {

		/* (non-Javadoc)
		 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
		 */
		public ARCRecord adapt(ArchiveRecord o) {
			ARCRecord rec = null;
			if(o instanceof ARCRecord) {
				rec = (ARCRecord) o;
			}
			return rec;
		}
	}

	private static void USAGE() {
		System.err.println("USAGE:");
		System.err.println("");
		System.err.println("arc-indexer ARCFILE");
		System.err.println("arc-indexer ARCFILE CDXFILE");
		System.err.println("");
		System.err.println("Create a CDX format index at CDXFILE or to STDOUT");
		System.exit(1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArcIndexer indexer = new ArcIndexer();
		File arc = new File(args[0]);
		PrintWriter pw = null;
		try {
			if(args.length == 1) {
				// dump to STDOUT:
				pw = new PrintWriter(System.out);
			} else if(args.length == 2) {
				pw = new PrintWriter(args[1]);
			} else {
				USAGE();
			}
			Iterator<SearchResult> res = indexer.iterator(arc);
			Iterator<String> lines = SearchResultToCDXLineAdapter.adapt(res);
			while(lines.hasNext()) {
				pw.println(lines.next());
			}
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
