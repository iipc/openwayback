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
package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.cdx.SearchResultToCDXLineAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.archive.wayback.util.url.IdentityUrlCanonicalizer;

/**
 * Transforms an ARC file into Iterator<CaptureSearchResult>.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArcIndexer {

	/**
	 * CDX Header line for these fields. not very configurable..
	 */
	public final static String CDX_HEADER_MAGIC = " CDX N b h m s k r V g";
	private UrlCanonicalizer canonicalizer = null;
	
	public ArcIndexer() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	/**
	 * @param arc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(File arc)
	throws IOException {
		return iterator(ARCReaderFactory.get(arc));
	}

	/**
	 * @param pathOrUrl
	 * @return Iterator of SearchResults for input pathOrUrl
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(String pathOrUrl)
	throws IOException {
		return iterator(ARCReaderFactory.get(pathOrUrl));
	}
	
	/**
	 * @param arcReader
	 * @return Iterator of SearchResults for input ARCReader
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(ARCReader arcReader)
	throws IOException {
		arcReader.setParseHttpHeaders(true);

		Adapter<ArchiveRecord,ARCRecord> adapter1 =
			new ArchiveRecordToARCRecordAdapter();

		ARCRecordToSearchResultAdapter adapter2 =
			new ARCRecordToSearchResultAdapter();
		adapter2.setCanonicalizer(canonicalizer);
		
		ArchiveReaderCloseableIterator itr1 = 
			new ArchiveReaderCloseableIterator(arcReader,arcReader.iterator());

		CloseableIterator<ARCRecord> itr2 = 
			new AdaptedIterator<ArchiveRecord,ARCRecord>(itr1,adapter1);
		
		return new AdaptedIterator<ARCRecord,CaptureSearchResult>(itr2,adapter2);
	}
	
	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	private static void USAGE() {
		System.err.println("USAGE:");
		System.err.println("");
		System.err.println("arc-indexer [-identity] ARCFILE");
		System.err.println("arc-indexer [-identity] ARCFILE CDXFILE");
		System.err.println("");
		System.err.println("Create a CDX format index at CDXFILE or to STDOUT.");
		System.err.println("With -identity, perform no url canonicalization.");
		System.exit(1);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArcIndexer indexer = new ArcIndexer();
		int idx = 0;
		if(args[0] != null && args[0].equals("-identity")) {
			indexer.setCanonicalizer(new IdentityUrlCanonicalizer());
			idx++;
		}
		File arc = new File(args[idx]);
		idx++;
		PrintWriter pw = null;
		try {
			if(args.length == idx) {
				// dump to STDOUT:
				pw = new PrintWriter(System.out);
			} else if(args.length == (idx + 1)) {
				pw = new PrintWriter(args[idx]);
			} else {
				USAGE();
			}
			Iterator<CaptureSearchResult> res = indexer.iterator(arc);
			Iterator<String> lines = SearchResultToCDXLineAdapter.adapt(res);
			while(lines.hasNext()) {
				pw.println(lines.next());
			}
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
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
}
