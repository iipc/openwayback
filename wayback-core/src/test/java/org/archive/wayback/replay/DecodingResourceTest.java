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
import org.apache.commons.io.IOUtils;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.*;

public class DecodingResourceTest extends TestCase {

    public void testGzipDecoding() throws IOException {
        byte[] data = Base64.decodeBase64("H4sIANdKklwAAzMEALfv3IMBAAAA");
        DecodingResource resource = DecodingResource.forEncoding("GZIP", new BytesResource(data));
        assertNotNull(resource);
        assertEquals("1", IOUtils.toString(resource));
    }

    public void testBrotliDecoding() throws IOException {
        byte[] data = Base64.decodeBase64("DwCAMQM=");
        DecodingResource resource = DecodingResource.forEncoding("br", new BytesResource(data));
        assertNotNull(resource);
        assertEquals("1", IOUtils.toString(resource));
    }

    public void testUnknownEncodingShouldReturnNull() throws IOException {
        byte[] data = "anything".getBytes(UTF_8);
        DecodingResource resource = DecodingResource.forEncoding("bogus", new BytesResource(data));
        assertNull(resource);
    }

}