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
