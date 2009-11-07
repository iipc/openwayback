/* HttpResponse
 *
 * $Id$
 *
 * Created on 5:44:56 PM Mar 2, 2009.
 *
 * Copyright (C) 2009 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * ProxyServletCore is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * ProxyServletCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with ProxyServletCore; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
