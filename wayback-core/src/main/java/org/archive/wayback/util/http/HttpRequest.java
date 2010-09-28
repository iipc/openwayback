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
