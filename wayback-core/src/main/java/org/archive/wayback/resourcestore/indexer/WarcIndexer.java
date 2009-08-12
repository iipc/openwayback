package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
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
	private boolean processAll = false;
	public WarcIndexer() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	public boolean isProcessAll() {
		return processAll;
	}

	public void setProcessAll(boolean processAll) {
		this.processAll = processAll;
	}

	
	/**
	 * @param warc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(File warc)
			throws IOException {
		return iterator(WARCReaderFactory.get(warc));
	}
	/**
	 * @param pathOrUrl
	 * @return Iterator of SearchResults for input pathOrUrl
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(String pathOrUrl)
			throws IOException {
		return iterator(WARCReaderFactory.get(pathOrUrl));
	}
	/**
	 * @param arc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(WARCReader reader)
			throws IOException {

		Adapter<ArchiveRecord, WARCRecord> adapter1 = new ArchiveRecordToWARCRecordAdapter();

		WARCRecordToSearchResultAdapter adapter2 = 
			new WARCRecordToSearchResultAdapter();
		adapter2.setCanonicalizer(canonicalizer);
		adapter2.setProcessAll(processAll);

		ArchiveReaderCloseableIterator itr1 = 
			new ArchiveReaderCloseableIterator(reader,reader.iterator());

		CloseableIterator<WARCRecord> itr2 = 
			new AdaptedIterator<ArchiveRecord, WARCRecord>(itr1, adapter1);

		return new AdaptedIterator<WARCRecord, CaptureSearchResult>(itr2, adapter2);
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
		System.err.println("warc-indexer [-identity] [-all] WARCFILE");
		System.err.println("warc-indexer [-identity] [-all] WARCFILE CDXFILE");
		System.err.println("");
		System.err.println("Create a CDX format index at CDXFILE or to STDOUT");
		System.err.println("With -identity, perform no url canonicalization.");
		System.err.println("With -all, output request and metadata records.");
		System.exit(1);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		WarcIndexer indexer = new WarcIndexer();
		int idx = 0;
		while(args[idx] != null) {
			if(args[idx].equals("-identity")) {
				indexer.setCanonicalizer(new IdentityUrlCanonicalizer());
			} else if(args[idx].equals("-all")) {
				indexer.setProcessAll(true);
			} else {
				break;
			}
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
			Iterator<CaptureSearchResult> res = indexer.iterator(arc);
			Iterator<String> lines = SearchResultToCDXLineAdapter.adapt(res);
			while (lines.hasNext()) {
				pw.println(lines.next());
			}
			pw.close();
			System.exit(1);

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
