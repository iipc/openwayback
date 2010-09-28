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

import java.io.IOException;
import java.io.InputStream;

import org.archive.util.anvl.ANVLRecord;
import org.archive.wayback.util.http.BadRequestException;

public class HttpResponse {
	private HttpResponseMessage message = null;
	private ANVLRecord headers = null;
	private InputStream bodyInputStream = null;
	public HttpResponse(HttpResponseMessage message, ANVLRecord headers, 
			InputStream bodyInputStream) {

		this.message = message;
		this.headers = headers;
		this.bodyInputStream = bodyInputStream; 
	}
	public HttpResponseMessage getMessage() {
		return message;
	}
	public ANVLRecord getHeaders() {
		return headers;
	}
	public InputStream getBodyInputStream() {
		return bodyInputStream;
	}
	public static HttpResponse load(InputStream in) 
	throws BadRequestException, IOException {

		HttpResponseMessage message = HttpMessage.loadResponse(in);
		ANVLRecord headers = ANVLRecord.load(in);
		return new HttpResponse(message,headers,in);
	}
}
