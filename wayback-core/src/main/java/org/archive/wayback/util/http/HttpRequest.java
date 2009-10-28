/* HttpRequest
 *
 * $Id$
 *
 * Created on 4:49:10 PM Dec 16, 2008.
 *
 * Copyright (C) 2008 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * SocksProxyCore is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * SocksProxyCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with SocksProxyCore; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.util.http;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.archive.util.anvl.ANVLRecord;
import org.archive.wayback.util.http.BadRequestException;
import org.archive.wayback.util.ByteOp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class HttpRequest {

	private static int MAX_HEADER_SIZE = 10240;
	
	private HttpRequestMessage message = null;
	private ANVLRecord headers = null;

	private byte[] originalHeaders = null;

	public HttpRequest(HttpRequestMessage message, byte[] originalHeaders)
	throws IOException {

		this.originalHeaders = originalHeaders;
		this.message = message;
		// If we want to keep the headers - we're not using them:
		ByteArrayInputStream bais = new ByteArrayInputStream(originalHeaders);
		headers = ANVLRecord.load(bais);
	}

	/**
	 * @return the headers
	 */
	public ANVLRecord getHeaders() {
		return headers;
	}

	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(ANVLRecord headers) {
		this.headers = headers;
	}

	/**
	 * @return the inputBytes
	 */
	public byte[] getOriginalHeaders() {
		return originalHeaders;
	}
	public HttpRequestMessage getMessage() {
		return message;
	}
	/**
	 * @return the method
	 */
	public String getMethod() {
		return message.getMethod();
	}

	/**
	 * @return the url
	 */
	public String getPath() {
		return message.getPath();
	}

	public static HttpRequest load(InputStream in) 
	throws IOException, BadRequestException {

		HttpRequestMessage message = HttpMessage.loadRequest(in);
		
		byte[] buffer = new byte[MAX_HEADER_SIZE];

		int r = in.read(buffer, 0, MAX_HEADER_SIZE);
		if(r == MAX_HEADER_SIZE) {
			throw new BadRequestException("Request too long");
		}
		return new HttpRequest(message, ByteOp.copy(buffer,0,r));
	}
}
