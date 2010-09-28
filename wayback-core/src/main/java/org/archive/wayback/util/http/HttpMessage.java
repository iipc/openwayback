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
