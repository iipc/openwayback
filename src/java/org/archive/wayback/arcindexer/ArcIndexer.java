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

package org.archive.wayback.arcindexer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;

import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.net.UURI;
import org.archive.wayback.core.ResourceResult;
import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.Timestamp;
import org.apache.commons.httpclient.Header;

/**
 * Transforms an ARC file into ResourceResults, or a serialized ResourceResults
 * file(CDX).
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class ArcIndexer {
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
	 * @param arcPath
	 * @return ResourceResults in arcPath.
	 * @throws IOException
	 */
	public ResourceResults indexArc(final String arcPath) throws IOException {
		ResourceResults results = new ResourceResults();
		File arc = new File(arcPath);
		ARCReader arcReader = ARCReaderFactory.get(arc);
		arcReader.setParseHttpHeaders(true);
		// doh. this does not generate quite the columns we need:
		// arcReader.createCDXIndexFile(arcPath);
		Iterator itr = arcReader.iterator();
		while (itr.hasNext()) {
			ARCRecord rec = (ARCRecord) itr.next();
			ResourceResult result;
			try {
				result = arcRecordToResourceResult(rec, arc);
			} catch (NullPointerException e) {
				e.printStackTrace();
				continue;
			} catch (ParseException e) {
				e.printStackTrace();
				continue;
			}
			results.addResourceResult(result);
		}
		return results;
	}

	private ResourceResult arcRecordToResourceResult(final ARCRecord rec,
			File arc) throws NullPointerException, IOException, ParseException {
		rec.close();
		ARCRecordMetaData meta = rec.getMetaData();

		ResourceResult result = new ResourceResult();
		result.setArcFileName(arc.getName());
		result.setCompressedOffset(meta.getOffset());

		String statusCode = (meta.getStatusCode() == null) ? "-" : meta
				.getStatusCode();
		result.setHttpResponseCode(statusCode);

		result.setMd5Fragment(meta.getDigest());
		result.setMimeType(meta.getMimetype());
		UURI uri = new UURI(meta.getUrl(), false);
		result.setOrigHost(uri.getHost());

		String redirectUrl = "-";
		Header[] headers = rec.getHttpHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				if (headers[i].getName().equals(LOCATION_HTTP_HEADER)) {
					redirectUrl = headers[i].getValue();
					break;
				}
			}
		}
		result.setRedirectUrl(redirectUrl);
		result.setTimestamp(Timestamp.parseBefore(meta.getDate()));
		UURI uriCap = new UURI(meta.getUrl(), false);
		String searchHost = uriCap.getHostBasename();
		String searchPath = uriCap.getEscapedPathQuery();

		String indexUrl = searchHost + searchPath;
		result.setUrl(indexUrl);

		return result;
	}

	/**
	 * Write out ResourceResults into CDX file at cdxPath
	 * 
	 * @param results
	 * @param cdxPath
	 * @throws IOException
	 */
	public void serializeResults(final ResourceResults results,
			final String cdxPath) throws IOException {
		Iterator itr = results.iterator();
		File cdx = new File(cdxPath);
		FileOutputStream output = new FileOutputStream(cdx);
		output.write((ResourceResult.getCDXHeaderString() + "\n").getBytes());
		while (itr.hasNext()) {
			ResourceResult result = (ResourceResult) itr.next();
			output.write((result.toString() + "\n").getBytes());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ArcIndexer indexer = new ArcIndexer();
		String arc = args[0];
		String cdx = args[1];
		try {
			ResourceResults results = indexer.indexArc(arc);
			indexer.serializeResults(results, cdx);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
