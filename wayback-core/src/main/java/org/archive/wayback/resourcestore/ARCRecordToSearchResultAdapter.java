/* ArcRecordToSearchResultAdapter
 *
 * $Id$
 *
 * Created on 3:27:03 PM Jul 26, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.URIException;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ARCRecordToSearchResultAdapter 
implements Adapter<ARCRecord,SearchResult>{

	private static final Logger LOGGER = Logger.getLogger(
			ARCRecordToSearchResultAdapter.class.getName());

	private UrlCanonicalizer canonicalizer = null;
	
	public ARCRecordToSearchResultAdapter() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}
//	public static SearchResult arcRecordToSearchResult(final ARCRecord rec)
//	throws IOException, ParseException {
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public SearchResult adapt(ARCRecord rec) {
		try {
			return adaptInner(rec);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private SearchResult adaptInner(ARCRecord rec) throws IOException {
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
						if (headers[i].getName().equals(
								WaybackConstants.LOCATION_HTTP_HEADER)) {

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
	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}
	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}
}
