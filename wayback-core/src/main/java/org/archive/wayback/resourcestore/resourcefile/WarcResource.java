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
package org.archive.wayback.resourcestore.resourcefile;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.archive.format.arc.ARCConstants;
import org.archive.format.warc.WARCConstants.WARCRecordType;
import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.RecoverableIOException;
import org.archive.io.warc.WARCRecord;
import org.archive.util.DateUtils;
import org.archive.util.LaxHttpParser;
import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.HttpHeaderOperation;

public class WarcResource extends Resource {
	private WARCRecord rec = null;
	private ArchiveReader reader = null;
	private Map<String, String> headers = null;
	private long length = 0;
	private int status = 0;
	private boolean parsedHeaders = false;
	public WarcResource(WARCRecord rec, ArchiveReader reader) {
		this.rec = rec;
		this.reader = reader;
	}

	/**
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

	public void parseHeaders() throws IOException {
		if(parsedHeaders) {
			return;
		}
		
		// If warc or arc record is 0 length, don't do any more parsing!
		// Hopefully caller code will check this before proceeding as well
		if (getRecordLength() <= 0) {
			parsedHeaders = true;
			return;
		}
		
		// WARCRecord should have getRecordType() method returning WARCRecordType.
		String rectypeStr = (String)rec.getHeader().getHeaderValue("WARC-Type");
		WARCRecordType rectype;
		try {
		    rectype = WARCRecordType.valueOf(rectypeStr);
		} catch (IllegalArgumentException ex) {
		    throw new RecoverableIOException("unrecognized WARC-Type \"" + rectypeStr + "\"");
		}
		
		if (rectype == WARCRecordType.response || rectype == WARCRecordType.revisit) {
		    byte [] statusBytes = LaxHttpParser.readRawLine(rec);
		    int eolCharCount = getEolCharsCount(statusBytes);
		    if (eolCharCount <= 0) {
		        throw new RecoverableIOException("Failed to read http status where one " +
		                " was expected: " + new String(statusBytes));
		    }
		    String statusLineStr = EncodingUtil.getString(statusBytes, 0,
		            statusBytes.length - eolCharCount, ARCConstants.DEFAULT_ENCODING);
		    if ((statusLineStr == null) ||
		            !StatusLine.startsWithHTTP(statusLineStr)) {
		        throw new RecoverableIOException("Failed parse of http status line.");
		    }
		    StatusLine statusLine = new StatusLine(statusLineStr);

		    this.status = statusLine.getStatusCode();

		    Header[] tmpHeaders = LaxHttpParser.parseHeaders(rec,
		            ARCConstants.DEFAULT_ENCODING);
		    headers = new Hashtable<String,String>();
		    this.setInputStream(rec);
		    for(Header header: tmpHeaders) {
		        headers.put(header.getName(), header.getValue());
		        if(header.getName().toUpperCase().contains(
		                HttpHeaderOperation.HTTP_TRANSFER_ENC_HEADER)) {
		            if(header.getValue().toUpperCase().contains(
		                    HttpHeaderOperation.HTTP_CHUNKED_ENCODING_HEADER)) {
		                setChunkedEncoding();
		            }
		        }
		    }
		} else if (rectype == WARCRecordType.metadata || rectype == WARCRecordType.resource) {
		    status = 200;
		    headers = new HashMap<String, String>();
		    String ct = (String)rec.getHeader().getHeaderValue("Content-Type");
		    if (ct != null) {
		        headers.put("Content-Type", ct);
		    }
		    // necessary?
		    String date = rec.getHeader().getDate();
		    if (date != null) {
		        try {
		            Date d = org.apache.commons.lang.time.DateUtils.parseDate(date, new String[] { "yyyy-MM-dd'T'HH:mm:ss'Z'"});
		            String httpDate = DateUtils.getRFC1123Date(d);
		            headers.put("Date", httpDate);
		        } catch (ParseException ex) {
		            //
		        }
		    }
		    setInputStream(rec);
		}
		parsedHeaders = true;
	}


	@Override
	public Map<String, String> getHttpHeaders() {
		return headers;
	}

	public ArchiveRecordHeader getWarcHeaders() {
		return rec.getHeader();
	}

	@Override
	public long getRecordLength() {
		if ((length == 0) && (rec.getHeader() != null)) {
			length = rec.getHeader().getContentLength();
		}
		return length;
	}

	@Override
	public int getStatusCode() {
		return status;
	}

	@Override
	public void close() throws IOException {
		rec.close();
		reader.close();
	}
}
