/* WarcResource
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
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
package org.archive.wayback.resourcestore.resourcefile;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.httpclient.util.EncodingUtil;
import org.archive.io.RecoverableIOException;
import org.archive.io.arc.ARCConstants;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.HttpHeaderOperation;

public class WarcResource extends Resource {
	private WARCRecord rec = null;
	private WARCReader reader = null;
	private Map<String, String> headers = null;
	private long length = 0;
	private int status = 0;
	private boolean parsedHeaders = false;
	public WarcResource(WARCRecord rec, WARCReader reader) {
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
		
        byte [] statusBytes = HttpParser.readRawLine(rec);
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

        Header[] tmpHeaders = HttpParser.parseHeaders(rec,
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
		parsedHeaders = true;
	}
	
	
	@Override
	public Map<String, String> getHttpHeaders() {
		return headers;
	}

	@Override
	public long getRecordLength() {
		// TODO Auto-generated method stub
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
