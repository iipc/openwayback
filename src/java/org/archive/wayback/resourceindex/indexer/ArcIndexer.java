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
package org.archive.wayback.resourceindex.indexer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URIException;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.bdb.BDBRecord;
import org.archive.wayback.bdb.BDBRecordSet;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.resourceindex.cdx.CDXLineToSearchResultAdapter;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.UrlCanonicalizer;
import org.archive.wayback.util.flatfile.FlatFile;

import com.sleepycat.je.DatabaseEntry;

/**
 * Transforms an ARC file into SearchResults, or a serialized SearchResults
 * file(CDX).
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
	 * Logger for this class
	 */
	private static final Logger LOGGER = Logger.getLogger(ArcIndexer.class
			.getName());

	/**
	 * HTTP Header for redirection URL
	 */
	private final static String LOCATION_HTTP_HEADER = "Location";

	/**
	 * Constant indicating entire CDX line
	 */
	protected final static int TYPE_CDX_LINE = 0;

	/**
	 * Constant indicating entire url + timestamp only
	 */
	protected final static int TYPE_CDX_KEY = 1;

	/**
	 * Constant indicating trailing data fields from CDX line following url +
	 * timestamp
	 */
	protected final static int TYPE_CDX_VALUE = 2;

	static UrlCanonicalizer canonicalizer = new UrlCanonicalizer();

	private final static int DEFAULT_CAPACITY = 120;
	
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
					result = arcRecordToSearchResult(rec);
				} catch (NullPointerException e) {
					e.printStackTrace();
					continue;
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				if (result != null) {
					results.addSearchResult(result);
				}
			}
		} finally {
			arcReader.close();
		}
		return results;
	}

	/**
	 * transform an ARCRecord into a SearchResult
	 * 
	 * @param rec
	 * @param arc
	 * @return SearchResult for this document
	 * @throws NullPointerException
	 * @throws IOException
	 * @throws ParseException
	 */
	private static SearchResult arcRecordToSearchResult(final ARCRecord rec)
			throws NullPointerException, IOException, ParseException {
		rec.close();
		ARCRecordMetaData meta = rec.getMetaData();

		SearchResult result = new SearchResult();
        String arcName = meta.getArc(); 
        int index = arcName.lastIndexOf(File.separator);
        if (index > 0 && (index + 1) < arcName.length()) {
            arcName = arcName.substring(index + 1);
        }
		result.put(WaybackConstants.RESULT_ARC_FILE, arcName);
		result.put(WaybackConstants.RESULT_OFFSET, String.valueOf(meta
				.getOffset()));

		// initialize with default HTTP code...
		result.put(WaybackConstants.RESULT_HTTP_CODE, "-");

		result.put(WaybackConstants.RESULT_MD5_DIGEST, rec.getDigestStr());
		result.put(WaybackConstants.RESULT_MIME_TYPE, meta.getMimetype());
		result.put(WaybackConstants.RESULT_CAPTURE_DATE, meta.getDate());

		String uriStr = meta.getUrl();
		if (uriStr.startsWith(ARCRecord.ARC_MAGIC_NUMBER)) {
			// skip filedesc record altogether...
			return null;
		}
		if (uriStr.startsWith(WaybackConstants.DNS_URL_PREFIX)) {
			// skip URL + HTTP header processing for dns records...

			String origHost = uriStr.substring(WaybackConstants.DNS_URL_PREFIX
					.length());
			result.put(WaybackConstants.RESULT_ORIG_HOST, origHost);
			result.put(WaybackConstants.RESULT_REDIRECT_URL, "-");
			result.put(WaybackConstants.RESULT_URL, uriStr);
			result.put(WaybackConstants.RESULT_URL_KEY, uriStr);

		} else {

			UURI uri = UURIFactory.getInstance(uriStr);
			result.put(WaybackConstants.RESULT_URL, uriStr);

			String uriHost = uri.getHost();
			if (uriHost == null) {
				LOGGER.info("No host in " + uriStr + " in " + meta.getArc());
			} else {
				result.put(WaybackConstants.RESULT_ORIG_HOST, uriHost);

				String statusCode = (meta.getStatusCode() == null) ? "-" : meta
						.getStatusCode();
				result.put(WaybackConstants.RESULT_HTTP_CODE, statusCode);

				String redirectUrl = "-";
				Header[] headers = rec.getHttpHeaders();
				if (headers != null) {

					for (int i = 0; i < headers.length; i++) {
						if (headers[i].getName().equals(LOCATION_HTTP_HEADER)) {
							String locationStr = headers[i].getValue();
							// TODO: "Location" is supposed to be absolute:
							// (http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html)
							// (section 14.30) but Content-Location can be
							// relative.
							// is it correct to resolve a relative Location, as
							// we are?
							// it's also possible to have both in the HTTP
							// headers...
							// should we prefer one over the other?
							// right now, we're ignoring "Content-Location"
							try {
								UURI uriRedirect = UURIFactory.getInstance(uri,
										locationStr);
								redirectUrl = uriRedirect.getEscapedURI();

							} catch (URIException e) {
								LOGGER.info("Bad Location: " + locationStr
										+ " for " + uriStr + " in "
										+ meta.getArc() + " Skipped");
							}
							break;
						}
					}
				}
				result.put(WaybackConstants.RESULT_REDIRECT_URL, redirectUrl);

				String indexUrl = canonicalizer.urlStringToKey(meta.getUrl());
				result.put(WaybackConstants.RESULT_URL_KEY, indexUrl);
			}

		}
		return result;
	}

	/**
	 * Write out ResourceResults into CDX file at cdxPath
	 * 
	 * @param results
	 * @param target
	 * @throws IOException
	 */
	public void serializeResults(final SearchResults results, File target)
			throws IOException {

		FileOutputStream os = new FileOutputStream(target);
		BufferedOutputStream bos = new BufferedOutputStream(os);
		PrintWriter pw = new PrintWriter(bos);
		try {
			serializeResults(results, pw);
		} finally {
			pw.close();
		}
	}

	/**
	 * @param results
	 * @param pw
	 * @param addHeader 
	 * @throws IOException
	 */
	public void serializeResults(final SearchResults results, PrintWriter pw,
			final boolean addHeader)
			throws IOException {
		if(addHeader) {
			pw.println(CDX_HEADER_MAGIC);
		}
		Iterator itr = results.iterator();
		while (itr.hasNext()) {
			SearchResult result = (SearchResult) itr.next();
			pw.println(searchResultToString(result, TYPE_CDX_LINE));
		}
		pw.flush();
	}

	
	/**
	 * @param results
	 * @param pw
	 * @throws IOException
	 */
	public void serializeResults(final SearchResults results, PrintWriter pw)
			throws IOException {
		serializeResults(results,pw,true);
	}

	/**
	 * @param rec
	 * @return String in "CDX format" for rec argument
	 * @throws IOException
	 * @throws ParseException
	 */
	public static String arcRecordToCDXLine(ARCRecord rec) 
	throws IOException, ParseException {
		return searchResultToString(arcRecordToSearchResult(rec),TYPE_CDX_LINE);
	}
	
	/**
	 * Transform a SearchResult into a String representation.
	 * 
	 * @param result
	 * @param type
	 * @return String value of either line, key or value for the SearchResult
	 */
	protected static String searchResultToString(final SearchResult result,
			int type) {

		StringBuilder sb = new StringBuilder(DEFAULT_CAPACITY);

		if (type == TYPE_CDX_LINE) {

			sb.append(result.get(WaybackConstants.RESULT_URL_KEY));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_CAPTURE_DATE));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_ORIG_HOST));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_MIME_TYPE));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_HTTP_CODE));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_MD5_DIGEST));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_REDIRECT_URL));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_OFFSET));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_ARC_FILE));
			
		} else if (type == TYPE_CDX_KEY) {
			
			sb.append(result.get(WaybackConstants.RESULT_URL_KEY));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_CAPTURE_DATE));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_OFFSET));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_ARC_FILE));
			
		} else if (type == TYPE_CDX_VALUE) {

			sb.append(result.get(WaybackConstants.RESULT_ORIG_HOST));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_MIME_TYPE));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_HTTP_CODE));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_MD5_DIGEST));
			sb.append(" ");
			sb.append(result.get(WaybackConstants.RESULT_REDIRECT_URL));

		} else {
			throw new IllegalArgumentException("Unknown transformation type");
		}
		return sb.toString();
	}

	/**
	 * @param cdxFile
	 * @return Iterator that will return BDBRecords, one for each line in 
	 * cdxFile argument 
	 * @throws IOException
	 */
	public Iterator getCDXFileBDBRecordIterator(File cdxFile) throws IOException {
		FlatFile ffile = new FlatFile(cdxFile.getAbsolutePath());
		AdaptedIterator searchResultItr = new AdaptedIterator(
				ffile.getSequentialIterator(),
				new CDXLineToSearchResultAdapter());
		return new AdaptedIterator(searchResultItr,
				new SearchResultToBDBRecordAdapter());
	}

	private class SearchResultToBDBRecordAdapter implements Adapter {

		ArcIndexer indexer = new ArcIndexer();

		DatabaseEntry key = new DatabaseEntry();

		DatabaseEntry value = new DatabaseEntry();

		BDBRecord record = new BDBRecord(key, value);

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
		 */
		public Object adapt(Object o) {
			if (!(o instanceof SearchResult)) {
				throw new IllegalArgumentException(
						"Argument is not a SearchResult");
			}
			SearchResult result = (SearchResult) o;
			key.setData(BDBRecordSet.stringToBytes(ArcIndexer
					.searchResultToString(result, ArcIndexer.TYPE_CDX_KEY)));
			value.setData(BDBRecordSet.stringToBytes(ArcIndexer
					.searchResultToString(result, ArcIndexer.TYPE_CDX_VALUE)));

			return record;
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
