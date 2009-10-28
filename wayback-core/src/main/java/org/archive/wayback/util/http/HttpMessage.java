/* HttpMessage
 *
 * $Id$
 *
 * Created on 5:48:40 PM Mar 2, 2009.
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

import org.archive.wayback.util.http.BadRequestException;
import org.archive.wayback.util.ByteOp;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */

public class HttpMessage {
	private static int MAX_MESSAGE_SIZE = 4096;
	public static byte SPACE = 32;
	public static byte CR = 13;
	public static byte LF = 10;

	public static byte[] readLine(InputStream in, int max)
	throws IOException, BadRequestException {

		byte[] buffer = new byte[max];
		int pos = 0;
		boolean found = false;
		while(pos < max) {
			int next = in.read();
			buffer[pos] = (byte) next;
			if(next == LF) {
				if(pos == 0) {
					throw new BadRequestException(
							"Message cannot start with LF");
				}
				if(buffer[pos - 1] == CR) {
					found = true;
					break;
				}
			}
			pos++;
		}
		if(!found) {
			throw new BadRequestException("Message too long without CRLF");
		}
		return ByteOp.copy(buffer,0,pos+1);
	}

	private static int[] findSpaces(byte[] buffer, int max) 
		throws BadRequestException {
		
		int spaces[] = new int[max];
		int found = 0;
		int offset = 0;
		int end = buffer.length - 2;
		while(offset < end) {
			if(buffer[offset] == SPACE) {
				spaces[found] = offset;
				found++;
			}
			if(found == max - 1) {
				break;
			}
			offset++;
		}
		if(found != max - 1) {
			throw new BadRequestException("Not enough fields(" + found +") " +
					"want("+max+") in (" + new String(buffer)+ ")");
		}
		return spaces;
	}
	
	public static byte[][] loadFields(byte[] buffer, int max) 
	throws BadRequestException {
		
		byte[][] fields = new byte[max][];
		int[] offsets = findSpaces(buffer, max);
		int start = 0;
		for(int i = 0; i < max - 1; i++) {
			fields[i] = ByteOp.copy(buffer, start, offsets[i] - start);
			start = offsets[i] + 1;
		}
		fields[max-1] = ByteOp.copy(buffer, start, (buffer.length - 2) - start);
		return fields;
	}

	public byte[] concatBytes(byte[][] fields, boolean addCrLf) {
		int length = 1;
		for(byte[] field : fields) {
			length += field.length + 1;
		}
		if(!addCrLf) {
			length -= 2;
		}
		byte[] buffer = new byte[length];
		int index = 0;
		for(byte[] field : fields) {
			System.arraycopy(field, 0,
					buffer, index, field.length);
			index += field.length;
			if(index < length) {
				buffer[index] = SPACE;
			}
			index++;
		}
		if(addCrLf) {
			buffer[length - 2] = CR;
			buffer[length - 1] = LF;
		}

		return buffer;
	}
	
	public static HttpResponseMessage loadResponse(byte[] buffer) 
		throws BadRequestException {

		byte[][] fields = loadFields(buffer,3);
		
		return new HttpResponseMessage(fields[0],fields[1],fields[2]);
	}

	public static HttpResponseMessage loadResponse(InputStream in) 
		throws BadRequestException, IOException {

		return loadResponse(readLine(in, MAX_MESSAGE_SIZE));
	}

	public static HttpRequestMessage loadRequest(byte[] buffer) 
		throws BadRequestException {

		byte[][] fields = loadFields(buffer,3);
	
		return new HttpRequestMessage(fields[0],fields[1],fields[2]);
	}

	public static HttpRequestMessage loadRequest(InputStream in) 
		throws BadRequestException, IOException {

		return loadRequest(readLine(in, MAX_MESSAGE_SIZE));
	}
}
