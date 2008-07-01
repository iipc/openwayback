package org.archive.wayback.resourcestore.indexer;

import java.io.File;
import java.io.IOException;
//import java.util.logging.Logger;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.RecoverableIOException;
import org.archive.io.arc.ARCConstants;
import org.archive.io.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;
import org.archive.wayback.util.url.UrlOperations;

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
	
	private final static String DEFAULT_VALUE = "-"; 

//	private static final Logger LOGGER = Logger.getLogger(
//			WARCRecordToSearchResultAdapter.class.getName());

	private UrlCanonicalizer canonicalizer = null;

	public WARCRecordToSearchResultAdapter() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public CaptureSearchResult adapt(WARCRecord rec) {
		try {
			return adaptInner(rec);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * Transform input date to 14-digit timestamp:
	 * 2007-08-29T18:00:26Z => 20070829180026
	 */
	private static String transformDate(final String input) {
		
		StringBuilder output = new StringBuilder(14);
		
		output.append(input.substring(0,4));
		output.append(input.substring(5,7));
		output.append(input.substring(8,10));
		output.append(input.substring(11,13));
		output.append(input.substring(14,16));
		output.append(input.substring(17,19));
		
		return output.toString();
	}
	
	private static String transformHTTPMime(final String input) {
		int semiIdx = input.indexOf(";");
		if(semiIdx > 0) {
			return input.substring(0,semiIdx).trim();
		}
		return input.trim();
	}

	private String transformWarcFilename(String readerIdentifier) {
		String warcName = readerIdentifier;
		int index = warcName.lastIndexOf(File.separator);
		if (index > 0 && (index + 1) < warcName.length()) {
		    warcName = warcName.substring(index + 1);
		}
		return warcName;
	}

	private String transformDigest(final Object o) {
		if(o == null) {
			return DEFAULT_VALUE;
		}
		String orig = o.toString();
		if(orig.startsWith("sha1:")) {
			return orig.substring(5);
		}
		return orig;
	}

	private CaptureSearchResult getBlankSearchResult() {
		CaptureSearchResult result = new CaptureSearchResult();

		result.setUrlKey(DEFAULT_VALUE);
		result.setOriginalUrl(DEFAULT_VALUE);
		result.setCaptureTimestamp(DEFAULT_VALUE);
		result.setDigest(DEFAULT_VALUE);
		result.setMimeType(DEFAULT_VALUE);
		result.setHttpCode(DEFAULT_VALUE);
		result.setRedirectUrl(DEFAULT_VALUE);
		result.setFile(DEFAULT_VALUE);
		result.setOffset(0);
		return result;
	}
	
	private void addUrlDataToSearchResult(CaptureSearchResult result, String urlStr)
	throws IOException {

		result.setOriginalUrl(urlStr);
		String urlKey = canonicalizer.urlStringToKey(urlStr);
		result.setUrlKey(urlKey);
	}

	private CaptureSearchResult adaptDNS(ArchiveRecordHeader header, WARCRecord rec) 
	throws IOException {

		CaptureSearchResult result = getBlankSearchResult();

		result.setCaptureTimestamp(transformDate(header.getDate()));
		result.setFile(transformWarcFilename(header.getReaderIdentifier()));
		result.setOffset(header.getOffset());
		
		String uriStr = header.getUrl();
		
		result.setMimeType(header.getMimetype());

		result.setOriginalUrl(uriStr);
		result.setUrlKey(uriStr);

		rec.close();
		result.setDigest(rec.getDigestStr());

		return result;
	}

	private CaptureSearchResult adaptRevisit(ArchiveRecordHeader header, WARCRecord rec) 
	throws IOException {

		CaptureSearchResult result = getBlankSearchResult();

		result.setCaptureTimestamp(transformDate(header.getDate()));
		result.setDigest(transformDigest(header.getHeaderValue(
						WARCRecord.HEADER_KEY_PAYLOAD_DIGEST)));
		
		addUrlDataToSearchResult(result,header.getUrl());

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
	
	private CaptureSearchResult adaptResponse(ArchiveRecordHeader header, WARCRecord rec) 
	throws IOException {

		CaptureSearchResult result = getBlankSearchResult();

		result.setCaptureTimestamp(transformDate(header.getDate()));
		result.setFile(transformWarcFilename(header.getReaderIdentifier()));
		result.setOffset(header.getOffset());
		
		String origUrl = header.getUrl();
		addUrlDataToSearchResult(result,origUrl);

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

		rec.close();
		result.setDigest(transformDigest(header.getHeaderValue(
						WARCRecord.HEADER_KEY_PAYLOAD_DIGEST)));

		if (headers != null) {
	
			for (Header httpHeader : headers) {
				if (httpHeader.getName().equals(
						WaybackConstants.LOCATION_HTTP_HEADER)) {
	
					String locationStr = httpHeader.getValue();
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
					result.setRedirectUrl(
							UrlOperations.resolveUrl(origUrl, locationStr));
				} else if(httpHeader.getName().toLowerCase().equals("content-type")) {
					result.setMimeType(transformHTTPMime(httpHeader.getValue()));
				}
			}
		}
		return result;
	}
	
	private CaptureSearchResult adaptInner(WARCRecord rec) throws IOException {
		
		CaptureSearchResult result = null;
		ArchiveRecordHeader header = rec.getHeader();
		String type = header.getHeaderValue(WARCConstants.HEADER_KEY_TYPE).toString();
		if(type.equals(WARCConstants.RESPONSE)) {
			String mime = header.getMimetype();
			if(mime.equals("text/dns")) {
				result = adaptDNS(header,rec);
			} else {
				result = adaptResponse(header,rec);
			}
		} else if(type.equals(WARCConstants.REVISIT)) {
			result = adaptRevisit(header,rec);
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
