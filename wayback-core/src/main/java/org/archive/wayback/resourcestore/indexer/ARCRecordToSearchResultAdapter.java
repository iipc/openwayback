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
package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;
//import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.IdentityUrlCanonicalizer;
import org.archive.wayback.util.url.UrlOperations;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ARCRecordToSearchResultAdapter 
implements Adapter<ARCRecord,CaptureSearchResult>{

//	private static final Logger LOGGER = Logger.getLogger(
//			ARCRecordToSearchResultAdapter.class.getName());

	private UrlCanonicalizer canonicalizer = null;
	
	public ARCRecordToSearchResultAdapter() {
		canonicalizer = new IdentityUrlCanonicalizer();
	}
//	public static SearchResult arcRecordToSearchResult(final ARCRecord rec)
//	throws IOException, ParseException {
	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public CaptureSearchResult adapt(ARCRecord rec) {
		try {
			return adaptInner(rec);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private CaptureSearchResult adaptInner(ARCRecord rec) throws IOException {
		rec.close();
		ARCRecordMetaData meta = rec.getMetaData();
		
		CaptureSearchResult result = new CaptureSearchResult();
		String arcName = meta.getArc(); 
		int index = arcName.lastIndexOf(File.separator);
		if (index > 0 && (index + 1) < arcName.length()) {
		    arcName = arcName.substring(index + 1);
		}
		result.setFile(arcName);
		result.setOffset(meta.getOffset());
		
		// initialize with default HTTP code...
		result.setHttpCode("-");
		
		result.setDigest(rec.getDigestStr());
		result.setMimeType(meta.getMimetype());
		result.setCaptureTimestamp(meta.getDate());
		
		String uriStr = meta.getUrl();
		if (uriStr.startsWith(ARCRecord.ARC_MAGIC_NUMBER)) {
			// skip filedesc record altogether...
			return null;
		}
		if (uriStr.startsWith(WaybackConstants.DNS_URL_PREFIX)) {
			// skip URL + HTTP header processing for dns records...
		
			result.setOriginalUrl(uriStr);
			result.setRedirectUrl("-");
			result.setUrlKey(uriStr);
		
		} else {
		
			result.setOriginalUrl(uriStr);
		
		
			String statusCode = (meta.getStatusCode() == null) ? "-" : meta
					.getStatusCode();
			result.setHttpCode(statusCode);
	
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
						redirectUrl = UrlOperations.resolveUrl(uriStr, 
								locationStr);
	
						break;
					}
				}
				result.setRedirectUrl(redirectUrl);
		
				String urlKey = canonicalizer.urlStringToKey(meta.getUrl());
				result.setUrlKey(urlKey);
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
