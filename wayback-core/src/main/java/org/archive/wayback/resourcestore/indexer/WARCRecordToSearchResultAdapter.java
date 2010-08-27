/* WARCRecordToSearchResultAdapter
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.RecoverableIOException;
import org.archive.io.arc.ARCConstants;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.IdentityUrlCanonicalizer;

/**
 * Adapts certain WARCRecords into SearchResults. DNS and response records are
 * mostly straightforward, but SearchResult objects generated from revisit 
 * records contain lots of "placeholder" fields, which are expected to be
 * understood by later processes traversing a stream of SearchResult objects.
 * 
 * See org.archive.wayback.resourceindex.DeduplicateSearchResultAnnotationAdapter.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class WARCRecordToSearchResultAdapter
implements Adapter<WARCRecord,CaptureSearchResult>{
	
	private static final Logger LOGGER =
        Logger.getLogger(WARCRecordToSearchResultAdapter.class.getName());
	
	private final static String DEFAULT_VALUE = "-"; 
	private UrlCanonicalizer canonicalizer = null;
	private HTTPRecordAnnotater annotater = null;
	
	private boolean processAll = false;

	public WARCRecordToSearchResultAdapter() {
		canonicalizer = new IdentityUrlCanonicalizer();
		annotater = new HTTPRecordAnnotater();
	}

	/* 
	 * This just calls adaptInner, returning null if an Exception is thrown:
	 */
	public CaptureSearchResult adapt(WARCRecord rec) {
		try {
			return adaptInner(rec);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private CaptureSearchResult adaptInner(WARCRecord rec) throws IOException {
		
		ArchiveRecordHeader header = rec.getHeader();

		String type = header.getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
		if(type.equals(WARCConstants.WARCINFO)) {
			LOGGER.info("Skipping record type : " + type);
			return null;
		}

		CaptureSearchResult result = genericResult(rec);

		if(type.equals(WARCConstants.RESPONSE)) {
			String mime = annotater.transformHTTPMime(header.getMimetype());
			if(mime.equals("text/dns")) {
				// close to complete reading, then the digest is legit
				// TODO: DO we want to use the WARC header digest for this?
				rec.close();
				result.setDigest(transformWARCDigest(rec.getDigestStr()));
				result.setMimeType(mime);
			} else {
				result = adaptWARCHTTPResponse(result,rec);
			}
		} else if(type.equals(WARCConstants.REVISIT)) {
			// also set the mime type:
			result.setMimeType("warc/revisit");

		} else if(type.equals(WARCConstants.REQUEST)) {
			
			if(processAll) {
				// also set the mime type:
				result.setMimeType("warc/request");
			} else {
				result = null;
			}
		} else if(type.equals(WARCConstants.METADATA)) {

			if(processAll) {
				// also set the mime type:
				result.setMimeType("warc/metadata");
			} else {
				result = null;
			}
		} else {
			LOGGER.info("Skipping record type : " + type);
		}

		return result;
	}

	// ALL HELPER METHODS BELOW:

	/*
	 * Extract all common WARC fields into a CaptureSearchResult. This is the
	 * same for all WARC record types:
	 *  
	 *    file, offset, timestamp, digest, urlKey, originalUrl 
	 */
	private CaptureSearchResult genericResult(WARCRecord rec) {

		CaptureSearchResult result = new CaptureSearchResult();

		result.setMimeType(DEFAULT_VALUE);
		result.setHttpCode(DEFAULT_VALUE);
		result.setRedirectUrl(DEFAULT_VALUE);

		ArchiveRecordHeader header = rec.getHeader();

		String file = transformWARCFilename(header.getReaderIdentifier());
		long offset = header.getOffset();
		
		result.setCaptureTimestamp(transformWARCDate(header.getDate()));
		result.setFile(file);
		result.setOffset(offset);
		result.setDigest(transformWARCDigest(header.getHeaderValue(
				WARCRecord.HEADER_KEY_PAYLOAD_DIGEST)));
		
		String origUrl = header.getUrl();
		result.setOriginalUrl(origUrl);
		try {
			String urlKey = canonicalizer.urlStringToKey(origUrl);
			result.setUrlKey(urlKey);
		} catch (URIException e) {
			LOGGER.warning("FAILED canonicalize(" + origUrl + "):" + 
					file + " " + offset);
			result.setUrlKey(origUrl);
		}
		return result;
	}

    /**
     * borrowed(copied) from org.archive.io.arc.ARCRecord...
     * 
     * @param bytes Array of bytes to examine for an EOL.
     * @return Count of end-of-line characters or zero if none.
     */
    private int getEolCharsCount(byte [] bytes) {
        int count = 0;
        if (bytes != null && bytes.length >=1 &&
                bytes[bytes.length - 1] == '\n') {
            count++;
            if (bytes.length >=2 && bytes[bytes.length -2] == '\r') {
                count++;
            }
        }
        return count;
    }

    private String transformWARCFilename(String readerIdentifier) {
		String warcName = readerIdentifier;
		int index = warcName.lastIndexOf(File.separator);
		if (index > 0 && (index + 1) < warcName.length()) {
		    warcName = warcName.substring(index + 1);
		}
		return warcName;
	}

	private String transformWARCDigest(final Object o) {
		if(o == null) {
			return DEFAULT_VALUE;
		}
		String orig = o.toString();
		if(orig.startsWith("sha1:")) {
			return orig.substring(5);
		}
		return orig;
	}

	/*
	 * Transform input date to 14-digit timestamp:
	 * 2007-08-29T18:00:26Z => 20070829180026
	 */
	private static String transformWARCDate(final String input) {
		
		StringBuilder output = new StringBuilder(14);
		
		output.append(input.substring(0,4));
		output.append(input.substring(5,7));
		output.append(input.substring(8,10));
		output.append(input.substring(11,13));
		output.append(input.substring(14,16));
		output.append(input.substring(17,19));
		
		return output.toString();
	}

    /*
     * Currently the WARCReader doesn't parse HTTP headers. This method parses
     * them then calls the common ARC/WARC shared record parsing code, which
     * addresses HTTP headers, and possibly even parses HTML content to look
     * for Robot Meta tags.
     */
	private CaptureSearchResult adaptWARCHTTPResponse(CaptureSearchResult result,
			WARCRecord rec) throws IOException {

		ArchiveRecordHeader header = rec.getHeader();
		// need to parse the documents HTTP message and headers here: WARCReader
		// does not implement this... yet..
		
        byte [] statusBytes = HttpParser.readRawLine(rec);
        int eolCharCount = getEolCharsCount(statusBytes);
        if (eolCharCount <= 0) {
            throw new RecoverableIOException("Failed to read http status where one " +
                " was expected: " + new String(statusBytes));
        }
        String statusLine = EncodingUtil.getString(statusBytes, 0,
            statusBytes.length - eolCharCount, ARCConstants.DEFAULT_ENCODING);
        if ((statusLine == null) ||
                !StatusLine.startsWithHTTP(statusLine)) {
           throw new RecoverableIOException("Failed parse of http status line.");
        }
        StatusLine status = new StatusLine(statusLine);
		result.setHttpCode(String.valueOf(status.getStatusCode()));
        
		Header[] headers = HttpParser.parseHeaders(rec,
                ARCConstants.DEFAULT_ENCODING);

		
		annotater.annotateHTTPContent(result,rec,headers,header.getMimetype());

		return result;
	}


	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	public boolean isProcessAll() {
		return processAll;
	}

	public void setProcessAll(boolean processAll) {
		this.processAll = processAll;
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
