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

/**
 *
 * Provide a wrapper for a Resource that is gzip encoded, that is,
 * Resources that have the header:
 * Content-Type: gzip
 *
 * Used by TextReplayRenderers and other ReplayRenderers that add content to the resulting output
 *
 */

package org.archive.wayback.replay;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.archive.wayback.core.Resource;

/**
 * A wrapper Resource that decodes content encoded with {@code deflate}.
 * Supports both standard deflate encoding (with zlib headers)
 * and non-standard encoding (without zlib headers).
 */
public class InflatingResource extends DecodingResource {

	private static final Logger LOGGER = Logger.getLogger(InflatingResource.class.getName());

	/** value of {@code Content-Encoding} header */
	public static final String CONTENT_ENCODING_NAME = "deflate";

	public InflatingResource(Resource source) {
		super(source);
		// Some implementation of "deflate" just implement RFC-1951 (compression
		// algorithm), ignoring RFC-1950 (zlib header). As InflaterInputStream does
		// not check header at instantiation, we need to check the header ourselves.
		source.mark(2);
		byte[] zlibHeader = new byte[2];
		try {
			int n = source.read(zlibHeader);
			try {
				source.reset();
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING, "reset() failed after peeking first two bytes",
					ex);
			}
			if (n < 2) {
				// unlikely be zlib compressed.
				setInputStream(source);
				return;
			}
			if (zlibHeader[0] != (byte)0x78) {
				// assume header-less deflate
				setInputStream(new InflaterInputStream(source, new Inflater(true)));
				return;
			}
			// deflate with zlib header
			setInputStream(new InflaterInputStream(source));
		} catch (IOException ex) {
			setInputStream(source);
		}
	}

}
