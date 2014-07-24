package org.archive.wayback.resourcestore.jwat;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;
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
 * <p>Note: JWATResource does not meet {@link Resource} expectations 100%:
 * <ul>
 * <li>chunk-ed entity - returns chunked content as-is</li>
 * <li>old-style WARC revisit record (revisit without HTTP headers) -
 * mostly work ok, but assumes status=200.</li>
 * <li>{@code metadata}/{@code resource} WARC records - does not return
 * Content-Type in WARC record header.</li>
 * </ul>
 */
public class JWATResource extends Resource {

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
					r.status = 200;
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
			r.headers = new Hashtable<String, String>();
			if (httpHeader != null) {
				Iterator<HeaderLine> headerLines = httpHeader.getHeaderList()
					.iterator();
				HeaderLine headerLine;
				while (headerLines.hasNext()) {
					headerLine = headerLines.next();
					r.headers.put(headerLine.name.toLowerCase(),
						headerLine.value);
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
