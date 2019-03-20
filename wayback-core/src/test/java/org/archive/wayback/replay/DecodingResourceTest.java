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

package org.archive.wayback.replay;

import junit.framework.TestCase;
import org.apache.commons.codec.binary.Base64;
import org.archive.wayback.core.Resource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class DecodingResourceTest extends TestCase {

    public void testGzipDecoding() throws IOException {
        byte[] data = Base64.decodeBase64("H4sIANdKklwAAzMEALfv3IMBAAAA");
        assertEquals('1', DecodingResource.forEncoding("GZIP", new BytesResource(data)).read());
    }

    public void testBrotliDecoding() throws IOException {
        byte[] data = Base64.decodeBase64("DwCAMQM=");
        assertEquals('1', DecodingResource.forEncoding("br", new BytesResource(data)).read());
    }

    private static class BytesResource extends Resource {

        private final byte[] bytes;

        BytesResource(byte[] bytes) {
            this.bytes = bytes;
            setInputStream(new ByteArrayInputStream(bytes));
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public int getStatusCode() {
            return 200;
        }

        @Override
        public long getRecordLength() {
            return bytes.length;
        }

        @Override
        public Map<String, String> getHttpHeaders() {
            return Collections.emptyMap();
        }
    }
}