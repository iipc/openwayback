package org.archive.wayback.resourcestore;

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
import org.archive.net.UURI;
import org.archive.net.UURIFactory;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.WaybackConstants;
import org.archive.wayback.core.SearchResult;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

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
implements Adapter<WARCRecord,SearchResult>{
	
	private final static String DEFAULT_VALUE = "-"; 
	private final static String SEARCH_FIELDS[] = {
			WaybackConstants.RESULT_URL,
			WaybackConstants.RESULT_URL_KEY,
			WaybackConstants.RESULT_ORIG_HOST,
			WaybackConstants.RESULT_CAPTURE_DATE,
			WaybackConstants.RESULT_MD5_DIGEST,
			WaybackConstants.RESULT_MIME_TYPE,
			WaybackConstants.RESULT_HTTP_CODE,
			WaybackConstants.RESULT_REDIRECT_URL,
			WaybackConstants.RESULT_ARC_FILE,
			WaybackConstants.RESULT_OFFSET,
	};

	private static final Logger LOGGER = Logger.getLogger(
			WARCRecordToSearchResultAdapter.class.getName());

	private UrlCanonicalizer canonicalizer = null;

	public WARCRecordToSearchResultAdapter() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
	 */
	public SearchResult adapt(WARCRecord rec) {
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

	private SearchResult getBlankSearchResult() {
		SearchResult result = new SearchResult();
		for(String field : SEARCH_FIELDS) {
			result.put(field, DEFAULT_VALUE);
		}
		return result;
	}
	
	private UURI addUrlDataToSearchResult(SearchResult result, String urlStr)
	throws IOException {

		result.put(WaybackConstants.RESULT_URL, urlStr);
		result.put(WaybackConstants.RESULT_URL_KEY, urlStr);

	
		UURI uri = UURIFactory.getInstance(urlStr);
		String uriHost = uri.getHost();
		if (uriHost == null) {

			LOGGER.info("No host in " + urlStr);

		} else {

			result.put(WaybackConstants.RESULT_ORIG_HOST, uriHost);
		}

		String urlKey = canonicalizer.urlStringToKey(urlStr);
		result.put(WaybackConstants.RESULT_URL_KEY, urlKey);

		return uri;
	}

	private SearchResult adaptDNS(ArchiveRecordHeader header, WARCRecord rec) 
	throws IOException {

		SearchResult result = getBlankSearchResult();

		result.put(WaybackConstants.RESULT_CAPTURE_DATE, 
				transformDate(header.getDate()));
		result.put(WaybackConstants.RESULT_ARC_FILE,
				transformWarcFilename(header.getReaderIdentifier()));
		result.put(WaybackConstants.RESULT_OFFSET, 
				String.valueOf(header.getOffset()));
		
		String uriStr = header.getUrl();
		
		String origHost = uriStr.substring(WaybackConstants.DNS_URL_PREFIX
				.length());
		result.put(WaybackConstants.RESULT_MIME_TYPE, header.getMimetype());

		result.put(WaybackConstants.RESULT_ORIG_HOST, origHost);
		result.put(WaybackConstants.RESULT_URL, uriStr);
		result.put(WaybackConstants.RESULT_URL_KEY, uriStr);

		rec.close();
		result.put(WaybackConstants.RESULT_MD5_DIGEST, rec.getDigestStr());

		return result;
	}

	private SearchResult adaptRevisit(ArchiveRecordHeader header, WARCRecord rec) 
	throws IOException {

		SearchResult result = getBlankSearchResult();

		result.put(WaybackConstants.RESULT_CAPTURE_DATE, 
				transformDate(header.getDate()));
		result.put(WaybackConstants.RESULT_MD5_DIGEST, 
				transformDigest(header.getHeaderValue(
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
	
	private SearchResult adaptResponse(ArchiveRecordHeader header, WARCRecord rec) 
	throws IOException {

		SearchResult result = getBlankSearchResult();

		result.put(WaybackConstants.RESULT_CAPTURE_DATE, 
				transformDate(header.getDate()));
		result.put(WaybackConstants.RESULT_ARC_FILE,
				transformWarcFilename(header.getReaderIdentifier()));
		result.put(WaybackConstants.RESULT_OFFSET, 
				String.valueOf(header.getOffset()));
		
		String origUrl = header.getUrl();
		UURI uri = addUrlDataToSearchResult(result,origUrl);

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
		result.put(WaybackConstants.RESULT_HTTP_CODE, 
				String.valueOf(status.getStatusCode()));
        
		Header[] headers = HttpParser.parseHeaders(rec,
                ARCConstants.DEFAULT_ENCODING);

		rec.close();
		result.put(WaybackConstants.RESULT_MD5_DIGEST, 
				transformDigest(header.getHeaderValue(
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
					try {
						UURI uriRedirect = UURIFactory.getInstance(uri,
								locationStr);
						result.put(WaybackConstants.RESULT_REDIRECT_URL,
								uriRedirect.getEscapedURI());
					} catch (URIException e) {
						LOGGER.info("Bad Location: " + locationStr
								+ " for " + origUrl + " in "
								+ header.getReaderIdentifier() + " Skipped");
					}
				} else if(httpHeader.getName().toLowerCase().equals("content-type")) {
					result.put(WaybackConstants.RESULT_MIME_TYPE, 
							transformHTTPMime(httpHeader.getValue()));
				}
			}
		}
		return result;
	}
	
	private SearchResult adaptInner(WARCRecord rec) throws IOException {
		
		SearchResult result = null;
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
