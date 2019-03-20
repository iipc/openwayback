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

import org.archive.wayback.core.Resource;
import org.brotli.dec.BrotliInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Provide a wrapper for a Resource that is gzip or brotli encoded, that is,
 * Resources that have the header:
 * Content-Type: gzip
 * Content-Type: br
 *
 * Used by TextReplayRenderers and other ReplayRenderers that add content to the resulting output
 *
 */

public class DecodingResource extends Resource {
    protected Resource source;

    private DecodingResource(Resource source, InputStream stream) {
        this.source = source;
        setInputStream(stream);
    }

    @Override
    public long getRecordLength() {
        return source.getRecordLength();
    }

    @Override
    public Map<String, String> getHttpHeaders() {
        return source.getHttpHeaders();
    }

    @Override
    public void close() throws IOException {
        source.close();
    }

    @Override
    public int getStatusCode() {
        return source.getStatusCode();
    }

    @Override
    public String getRefersToTargetURI() {
        return source.getRefersToTargetURI();
    }

    @Override
    public String getRefersToDate() {
        return source.getRefersToDate();
    }

    public static DecodingResource forEncoding(String contentEncoding, Resource source) throws IOException {
        switch (contentEncoding.toLowerCase()) {
            case "br":
                return new DecodingResource(source, new BrotliInputStream(source));
            case "gzip":
            case "x-gzip":
                return new DecodingResource(source, new GZIPInputStream(source));
            default:
                return null;
        }
    }
}
