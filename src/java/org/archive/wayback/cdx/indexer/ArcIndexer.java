/* ArcIndexer
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.cdx.indexer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.cdx.CDXRecord;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URIException;

/**
 * Transforms an ARC file into ResourceResults, or a serialized ResourceResults
 * file(CDX).
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class ArcIndexer {
	   private static final Logger LOGGER =
	        Logger.getLogger(ArcIndexer.class.getName());

	private final static String LOCATION_HTTP_HEADER = "Location";

	/**
	 * Constructor
	 */
	public ArcIndexer() {
		super();
	}

	/**
	 * Create a ResourceResults representing the records in ARC file at arcPath.
	 * 
	 * @param arc
	 * @return ResourceResults in arcPath.
	 * @throws IOException
	 */
	public SearchResults indexArc(File arc) throws IOException {
		SearchResults results = new SearchResults();
		ARCReader arcReader = ARCReaderFactory.get(arc);
		try {
			arcReader.setParseHttpHeaders(true);
			// doh. this does not generate quite the columns we need:
			// arcReader.createCDXIndexFile(arcPath);
			Iterator itr = arcReader.iterator();
			while (itr.hasNext()) {
				ARCRecord rec = (ARCRecord) itr.next();
				SearchResult result;
				try {
					result = arcRecordToSearchResult(rec, arc);
				} catch (NullPointerException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				if(result != null) {
					results.addSearchResult(result);
				}
			}
		} finally {
			arcReader.close();
		}
		return results;
	}

	private SearchResult arcRecordToSearchResult(final ARCRecord rec,
			File arc) throws NullPointerException, IOException, ParseException {
		rec.close();
		ARCRecordMetaData meta = rec.getMetaData();

		SearchResult result = new SearchResult();
		result.put(WaybackConstants.RESULT_ARC_FILE,arc.getName());
		result.put(WaybackConstants.RESULT_OFFSET,""+meta.getOffset());

		String statusCode = (meta.getStatusCode() == null) ? "-" : meta
				.getStatusCode();
		result.put(WaybackConstants.RESULT_HTTP_CODE,statusCode);

		result.put(WaybackConstants.RESULT_MD5_DIGEST,meta.getDigest());
		result.put(WaybackConstants.RESULT_MIME_TYPE,meta.getMimetype());
		
		String uriStr = meta.getUrl();
		if(uriStr.startsWith(ARCRecord.ARC_MAGIC_NUMBER)) {
			// skip filedesc record...
			return null;
		}
		if(uriStr.startsWith(WaybackConstants.DNS_URL_PREFIX)) {
			// skip dns records...
			return null;
		}
		
		UURI uri = UURIFactory.getInstance(uriStr);
		String uriHost = uri.getHost();
		if(uriHost == null) {
			LOGGER.info("No host in " + uriStr + " in " + 
					arc.getAbsolutePath());
			return null;
		}
		result.put(WaybackConstants.RESULT_ORIG_HOST,uriHost);

		String redirectUrl = "-";
		Header[] headers = rec.getHttpHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				if (headers[i].getName().equals(LOCATION_HTTP_HEADER)) {
					String locationStr = headers[i].getValue();
					// TODO: "Location" is supposed to be absolute:
					// (http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html)
					// (section 14.30) but Content-Location can be relative.
					// is it correct to resolve a relative Location, as we are?
					// it's also possible to have both in the HTTP headers...
					// should we prefer one over the other?
					// right now, we're ignoring "Content-Location"
					try {
						UURI uriRedirect = UURIFactory.getInstance(uri,
								locationStr);
						redirectUrl = uriRedirect.getEscapedURI();
						
					} catch (URIException e) {
						LOGGER.info("Bad Location: " + locationStr +
								" for " + uriStr + " in " + 
								arc.getAbsolutePath() + " Skipped");
					}
					break;
				}
			}
		}
		result.put(WaybackConstants.RESULT_REDIRECT_URL,redirectUrl);
		result.put(WaybackConstants.RESULT_CAPTURE_DATE,meta.getDate());
		UURI uriCap = new UURI(meta.getUrl(), false);
		String searchHost = uriCap.getHostBasename();
		String searchPath = uriCap.getEscapedPathQuery();

		String indexUrl = searchHost + searchPath;
		result.put(WaybackConstants.RESULT_URL,indexUrl);

		return result;
	}

	/**
	 * Write out ResourceResults into CDX file at cdxPath
	 * 
	 * @param results
	 * @param target
	 * @throws IOException
	 */
	public void serializeResults(final SearchResults results,
			File target) throws IOException {
		
		FileOutputStream os = new FileOutputStream(target);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		PrintWriter pw = new PrintWriter(bos);
		try {
			pw.println(CDXRecord.CDX_HEADER_MAGIC);
			CDXRecord cdxRecord = new CDXRecord();
			Iterator itr = results.iterator();
			while (itr.hasNext()) {
				SearchResult result = (SearchResult) itr.next();
				cdxRecord.fromSearchResult(result);
				pw.println(cdxRecord.toValue());
			}
		} finally {
			pw.close();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArcIndexer indexer = new ArcIndexer();
		File arc = new File(args[0]);
		File cdx = new File(args[1]);
		try {
			SearchResults results = indexer.indexArc(arc);
			indexer.serializeResults(results, cdx);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
