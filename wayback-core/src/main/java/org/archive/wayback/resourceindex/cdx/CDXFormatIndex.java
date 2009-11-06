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
