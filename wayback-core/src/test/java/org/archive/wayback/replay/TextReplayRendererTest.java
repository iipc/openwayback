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
import org.archive.wayback.core.Resource;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TextReplayRendererTest extends TestCase {

    public void testDecodeResource() throws IOException {
        byte[] data = Base64.decodeBase64("H4sIANdKklwAAzMEALfv3IMBAAAA");
        BytesResource source = new BytesResource(data);
        source.getHttpHeaders().put("Content-Encoding", "gzip");
        Resource decoded = TextReplayRenderer.decodeResource(source);
        assertEquals("1", IOUtils.toString(decoded));
    }

    public void testFalseContentEncodingHeaderShouldLeaveContentUnmodified() throws IOException {
        byte[] data = "Not actually gzipped".getBytes(UTF_8);
        BytesResource source = new BytesResource(data);
        source.getHttpHeaders().put("Content-Encoding", "gzip");
        Resource decoded = TextReplayRenderer.decodeResource(source);
        assertEquals("Not actually gzipped", IOUtils.toString(decoded));
    }
}