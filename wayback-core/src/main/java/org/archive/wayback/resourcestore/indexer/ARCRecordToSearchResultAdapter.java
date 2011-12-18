/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.Header;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.IdentityUrlCanonicalizer;

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
	private static final String VERSION = "0.1.0";
	private static final String ARC_FILEDESC_VERSION = "arc/filedesc" + VERSION;
	private HTTPRecordAnnotater annotater = null;
	private UrlCanonicalizer canonicalizer = null;
	
	public ARCRecordToSearchResultAdapter() {
		canonicalizer = new IdentityUrlCanonicalizer();
		annotater = new HTTPRecordAnnotater();
	}

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
		result.setRedirectUrl("-");
		
//		result.setDigest("sha1:"+rec.getDigestStr());
		result.setDigest(rec.getDigestStr());
		result.setCaptureTimestamp(meta.getDate());
		String uriStr = meta.getUrl();
		result.setOriginalUrl(uriStr);
		
		
		if (uriStr.startsWith(ARCRecord.ARC_MAGIC_NUMBER)) {
			result.setMimeType(ARC_FILEDESC_VERSION);
		} else if (uriStr.startsWith(WaybackConstants.DNS_URL_PREFIX)) {
			// skip URL + HTTP header processing for dns records...
		
			result.setUrlKey(uriStr);
			result.setMimeType("text/dns");
			result.setCompressedLength(rec.compressedBytes);

		} else {
		
			result.setUrlKey(canonicalizer.urlStringToKey(uriStr));
		
			String statusCode = (meta.getStatusCode() == null) ? "-" : meta
					.getStatusCode();
			result.setHttpCode(statusCode);
	
			Header[] headers = rec.getHttpHeaders();
			annotater.annotateHTTPContent(result, rec, headers, meta.getMimetype());
		}
		return result;
	}
	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}
	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	/**
	 * @return the annotater
	 */
	public HTTPRecordAnnotater getAnnotater() {
		return annotater;
	}

	/**
	 * @param annotater the annotater to set
	 */
	public void setAnnotater(HTTPRecordAnnotater annotater) {
		this.annotater = annotater;
	}
}
