package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.cdx.SearchResultToCDXLineAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.archive.wayback.util.url.IdentityUrlCanonicalizer;

public class WarcIndexer {

	/**
	 * CDX Header line for these fields. not very configurable..
	 */
	public final static String CDX_HEADER_MAGIC = " CDX N b h m s k r V g";

	private UrlCanonicalizer canonicalizer = null;
	public WarcIndexer() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}
	
	/**
	 * @param arc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<SearchResult> iterator(File warc)
			throws IOException {

		Adapter<ArchiveRecord, WARCRecord> adapter1 = new ArchiveRecordToWARCRecordAdapter();

		WARCRecordToSearchResultAdapter adapter2 = 
			new WARCRecordToSearchResultAdapter();
		adapter2.setCanonicalizer(canonicalizer);
		
		WARCReader reader = WARCReaderFactory.get(warc);
		
		ArchiveReaderCloseableIterator itr1 = 
			new ArchiveReaderCloseableIterator(reader,reader.iterator());

		CloseableIterator<WARCRecord> itr2 = 
			new AdaptedIterator<ArchiveRecord, WARCRecord>(itr1, adapter1);

		return new AdaptedIterator<WARCRecord, SearchResult>(itr2, adapter2);
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
		System.err.println("warc-indexer [-identity] WARCFILE");
		System.err.println("warc-indexer [-identity] WARCFILE CDXFILE");
		System.err.println("");
		System.err.println("Create a CDX format index at CDXFILE or to STDOUT");
		System.err.println("With -identity, perform no url canonicalization.");
		System.exit(1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WarcIndexer indexer = new WarcIndexer();
		int idx = 0;
		if(args[0] != null && args[0].equals("-identity")) {
			indexer.setCanonicalizer(new IdentityUrlCanonicalizer());
			idx++;
		}
		File arc = new File(args[idx]);
		idx++;
		PrintWriter pw = null;
		try {
			if (args.length == idx) {
				// dump to STDOUT:
				pw = new PrintWriter(System.out);
			} else if (args.length == (idx+1)) {
				pw = new PrintWriter(args[1]);
			} else {
				USAGE();
			}
			Iterator<SearchResult> res = indexer.iterator(arc);
			Iterator<String> lines = SearchResultToCDXLineAdapter.adapt(res);
			while (lines.hasNext()) {
				pw.println(lines.next());
			}
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class ArchiveRecordToWARCRecordAdapter implements
			Adapter<ArchiveRecord, WARCRecord> {

		/* (non-Javadoc)
		 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
		 */
		public WARCRecord adapt(ArchiveRecord o) {
			WARCRecord rec = null;
			if (o instanceof WARCRecord) {
				rec = (WARCRecord) o;
			}
			return rec;
		}
	}
}
