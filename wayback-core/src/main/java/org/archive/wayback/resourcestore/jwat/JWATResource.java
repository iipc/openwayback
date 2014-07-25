package org.archive.wayback.resourcestore.jwat;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.archive.util.ArchiveUtils;
import org.apache.commons.lang.time.DateUtils;
import org.archive.format.warc.WARCConstants;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
import org.archive.wayback.replay.HttpHeaderOperation;
import org.jwat.arc.ArcReader;
import org.jwat.arc.ArcReaderFactory;
import org.jwat.arc.ArcRecordBase;
import org.jwat.common.ByteCountingPushBackInputStream;
import org.jwat.common.HeaderLine;
import org.jwat.common.HttpHeader;
import org.jwat.common.Payload;
import org.jwat.common.UriProfile;
import org.jwat.gzip.GzipEntry;
import org.jwat.gzip.GzipReader;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;

/**
 * JWATResource -- created by Nick Clarke for interfacing with JWAT ARC/WARC Readers
 * Originally forked from https://bitbucket.org/nclarkekb/jwat-wayback-resourcestore
 *
 * @see JWATFlexResourceStore
 */
public class JWATResource extends Resource implements WARCConstants {

	protected ByteCountingPushBackInputStream pbin;

	protected GzipReader gzipReader;
	protected GzipEntry gzipEntry;

	protected ArcReader arcReader;
	protected ArcRecordBase arcRecord;

	protected WarcReader warcReader;
	protected WarcRecord warcRecord;

	protected InputStream payloadStream;

	protected Map<String, String> headers = null;
	protected long length = 0;
	protected int status = 0;

	private static WARCRecordType getWARCRecordType(WarcRecord rec)
			throws ResourceNotAvailableException {
		HeaderLine rectypeHeader = rec.getHeader(HEADER_KEY_TYPE);
		if (rectypeHeader == null) {
			throw new ResourceNotAvailableException("WARC-Type header is missing");
		}
		try {
			return WARCRecordType.valueOf(rectypeHeader.value);
		} catch (IllegalArgumentException ex) {
			throw new ResourceNotAvailableException(
				"unrecognized WARC-Type \"" + rectypeHeader.value + "\"");
		}
	}

	public static Resource getResource(InputStream rin, long offset)
			throws IOException, ResourceNotAvailableException {
		JWATResource r = new JWATResource();

		r.pbin = new ByteCountingPushBackInputStream(rin, 32);
		ByteCountingPushBackInputStream in = null;

		if (GzipReader.isGzipped(r.pbin)) {
			r.gzipReader = new GzipReader(r.pbin);
			if ((r.gzipEntry = r.gzipReader.getNextEntry()) != null) {
				in = new ByteCountingPushBackInputStream(
					new BufferedInputStream(r.gzipEntry.getInputStream(), 8192),
					32);
			} else {
				throw new ResourceNotAvailableException("GZip entry is invalid");
			}
		} else {
			in = r.pbin;
		}
		Payload payload = null;
		HttpHeader httpHeader = null;
		// essential metadata for non-HTTP response records.
		String contentType = null;
		String httpDate = null;
		if (ArcReaderFactory.isArcRecord(in)) {
			r.arcReader = ArcReaderFactory.getReaderUncompressed();
			r.arcReader.setUriProfile(UriProfile.RFC3986_ABS_16BIT_LAX);
			r.arcReader.setBlockDigestEnabled(false);
			r.arcReader.setPayloadDigestEnabled(false);
			r.arcRecord = r.arcReader.getNextRecordFrom(in, offset);
			if (r.arcRecord != null) {
				payload = r.arcRecord.getPayload();
				if (payload != null) {
					httpHeader = r.arcRecord.getHttpHeader();
				}
				if (httpHeader != null) {
					r.payloadStream = httpHeader.getPayloadInputStream();
					r.length = httpHeader.payloadLength;
					r.status = httpHeader.statusCode;
				} else if (payload != null) {
					r.payloadStream = payload.getInputStreamComplete();
					r.length = payload.getTotalLength();
					r.status = 200;
				} else {
					r.payloadStream = new ByteArrayInputStream(new byte[0]);
					r.length = 0;
					r.status = 200;
				}
			}
		} else if (WarcReaderFactory.isWarcRecord(in)) {
			r.warcReader = WarcReaderFactory.getReaderUncompressed();
			r.warcReader
				.setWarcTargetUriProfile(UriProfile.RFC3986_ABS_16BIT_LAX);
			r.warcReader.setBlockDigestEnabled(false);
			r.warcReader.setPayloadDigestEnabled(false);
			r.warcRecord = r.warcReader.getNextRecordFrom(in, offset);
			if (r.warcRecord != null) {
				WARCRecordType rectype = getWARCRecordType(r.warcRecord);
				if (rectype == WARCRecordType.response || rectype == WARCRecordType.revisit) {
					payload = r.warcRecord.getPayload();
					if (payload != null) {
						httpHeader = r.warcRecord.getHttpHeader();
					}
					if (httpHeader != null) {
						r.payloadStream = httpHeader.getPayloadInputStream();
						r.length = httpHeader.payloadLength;
						r.status = httpHeader.statusCode;
					} else if (payload != null) {
						r.payloadStream = payload.getInputStreamComplete();
						r.length = payload.getTotalLength();
						r.status = 200;
					} else {
						r.payloadStream = new ByteArrayInputStream(new byte[0]);
						r.length = 0;
						if (rectype == WARCRecordType.revisit)
							r.status = 0; // look in the original
						else
							r.status = 200;
					}
				} else if (rectype == WARCRecordType.metadata || rectype == WARCRecordType.resource) {
					// record body is the payload, assume 200 status.
					payload = r.warcRecord.getPayload();
					r.payloadStream = payload.getInputStreamComplete();
					r.length = payload.getTotalLength();
					r.status = 200;
					HeaderLine ctHeader = r.warcRecord.getHeader("content-type");
					if (ctHeader != null) {
						contentType = ctHeader.value;
					}
					HeaderLine dateHeader = r.warcRecord.getHeader(HEADER_KEY_DATE);
					if (dateHeader != null) {
						try {
							// translate ISOZ date in WARC-Date header to standard HTTP date.
							Date d = DateUtils.parseDate(dateHeader.value, new String[] { "yyyy-MM-dd'T'HH:mm:ss'Z'" });
							httpDate = org.archive.util.DateUtils.getRFC1123Date(d);
						} catch (ParseException ex) {
							//ignore.
						}
					}
				}
			}
		} else {
			throw new ResourceNotAvailableException("Unknown archive record");
		}
		if (r.payloadStream == null) {
			r.close();
			r = null;
		} else {
			r.setInputStream(r.payloadStream);
			if (httpHeader != null) {
				r.headers = new Hashtable<String, String>();
				for (HeaderLine headerLine : httpHeader.getHeaderList()) {
					String name = headerLine.name.toLowerCase();
					if (name.equals("transfer-encoding")) {
						if (HttpHeaderOperation.HTTP_CHUNKED_ENCODING_HEADER
							.equals(headerLine.value.toUpperCase())) {
							r.setChunkedEncoding();
						}
					}
					r.headers.put(name, headerLine.value);
				}
			} else {
				// metadata, resource or old-style revisit
				if (contentType != null || httpDate != null) {
					r.headers = new Hashtable<String, String>();
					if (contentType != null)
						r.headers.put("Content-Type", contentType);
					if (httpDate != null)
						r.headers.put("Date", httpDate);
				}
			}
		}
		return r;
	}

	@Override
	public Map<String, String> getHttpHeaders() {
		return headers;
	}

	@Override
	public long getRecordLength() {
		return length;
	}

	@Override
	public int getStatusCode() {
		return status;
	}

	@Override
	public String getRefersToTargetURI() {
		if (warcRecord != null) {
			HeaderLine h = warcRecord.getHeader("WARC-Refers-To-Target-URI");
			if (h != null)
				return h.value;
		}
		return null;
	}

	@Override
	public String getRefersToDate() {
		if (warcRecord != null) {
			HeaderLine h = warcRecord.getHeader("WARC-Refers-To-Date");
			if (h != null) {
				Date date = ArchiveUtils.parse14DigitISODate(h.value, null);
				if (date != null) {
					return ArchiveUtils.get14DigitDate(date);
				}
			}
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		if (warcRecord != null) {
			warcRecord.close();
		}
		if (warcReader != null) {
			warcReader.close();
		}
		if (arcRecord != null) {
			arcRecord.close();
		}
		if (arcReader != null) {
			arcReader.close();
		}
		if (gzipEntry != null) {
			gzipEntry.close();
		}
		if (gzipReader != null) {
			gzipReader.close();
		}
		if (pbin != null) {
			pbin.close();
		}
	}

}
