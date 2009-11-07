/* HttpRequestMessage
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


public class HttpRequestMessage extends HttpMessage {
	private static byte[] METHOD_HEAD = {'H', 'E', 'A', 'D'};
	private static byte[] METHOD_GET = {'G', 'E', 'T'};
	
	private byte[] method = null;
	private byte[] path = null;
	private byte[] version = null;
	public HttpRequestMessage(byte[] method, byte[] path, byte[] version) {
		this.method = method;
		this.path = path;
		this.version = version;
	}
	public String getMethod() {
		return new String(method);
	}
	public String getPath() {
		return new String(path);
	}
	public String getVersion() {
		return new String(version);
	}
	public HttpRequestMessage _clone() {
		return new HttpRequestMessage(method,path,version);
	}
	public void setPath(byte[] path) {
		this.path = path;
	}
	public byte[] getBytes(boolean addCrLf) {
		byte[][] fields = {method,path,version};
		return concatBytes(fields,addCrLf);
	}
	
//	public byte[] getBytes() {
//		// ' ' + ' ' + \r\n = 4
//		int length = path.length + method.length + version.length + 4;
//		int versionStart = path.length + method.length + 2;
//
//		byte[] buffer = new byte[length];
//
//		System.arraycopy(method, 0,
//				buffer, 0, method.length);
//
//		buffer[method.length] = SPACE;
//
//		System.arraycopy(path, 0,
//				buffer, method.length + 1, path.length);
//
//		buffer[versionStart - 1] = SPACE;
//
//		System.arraycopy(version, 0,
//				buffer, versionStart, version.length);
//		buffer[versionStart + version.length] = CR;
//		buffer[versionStart + version.length + 1] = LF;
//
//		return buffer;
//	}

	public boolean isHead() {
		return ByteOp.cmp(method,METHOD_HEAD);
	}
	public boolean isGet() {
		return ByteOp.cmp(method,METHOD_GET);
	}

//	public static HttpRequestMessage load(InputStream in)
//	throws BadRequestException, IOException {
//		return load(HttpMessage.readLine(in,MAX_SIZE));
//	}
//
//	public static HttpRequestMessage load(byte[] buffer)
//	throws BadRequestException {
//		
//		byte[] method = null;
//		byte[] path = null;
//		byte[] version = null;
//		
//		int length = buffer.length;
//		int end = length - 2;
//		int firstSpace = 0;
//		int lastSpace = end;
//		
//		
//		
//		// make sure ends in CRLF:
//		if((buffer[length - 2] != CR)
//			|| (buffer[length - 1] != LF)) {
//
//			throw new BadRequestException("Bed end of Message(no CRLF): "
//					+ new String(buffer));
//		}
//		
//		// find first ' ' (after METHOD):
//		while(firstSpace < end) {
//			if(buffer[firstSpace] == SPACE) {
//				method = ByteOp.copy(buffer, 0, firstSpace);
//				break;
//			}
//			firstSpace++;
//		}
//		
//		// find last ' ' (before VERSION):
//		while(lastSpace > firstSpace) {
//			if(buffer[lastSpace] == SPACE) {
//				version = ByteOp.copy(buffer, lastSpace + 1, end - (lastSpace+1));
//				break;
//			}
//			lastSpace--;
//		}
//		path = ByteOp.copy(buffer, firstSpace + 1, (lastSpace - firstSpace) - 1);
//		// make sure path has no spaces:
//		int position = 0;
//		while(position < path.length) {
//			if(path[position] == SPACE) {
//				throw new BadRequestException("Too many fields in Message: "
//						+ new String(buffer));
//			}
//			position++;
//		}
////		version = "HTTP/1.0".getBytes();
//		return new HttpRequestMessage(method, path, version);
//	}
}
