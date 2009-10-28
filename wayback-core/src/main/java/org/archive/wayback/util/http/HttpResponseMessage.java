/* HttpResponseMessage
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

import org.archive.wayback.util.ByteOp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class HttpResponseMessage extends HttpMessage {
	private static byte[] HTTP_304 = {'3', '0', '4'};
	private byte[] version = null;
	private byte[] code = null;
	private byte[] text = null;
	public HttpResponseMessage(byte[] version, byte[] code, byte[] text) {
		this.version = version;
		this.code = code;
		this.text = text;
	}
	public String getVersion() {
		return new String(version);
	}
	public String getCode() {
		return new String(code);
	}
	public String getText() {
		return new String(text);
	}
	public boolean isNotModified() {
		return ByteOp.cmp(code, HTTP_304);
	}
	public byte[] getBytes(boolean addCrLf) {
		byte[][] fields = {version,code,text};
		return concatBytes(fields, addCrLf);
	}
}
