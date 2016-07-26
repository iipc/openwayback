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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.archive.wayback.core.Resource;

/**
 * Provide a wrapper for a Resource that is gzip encoded, that is,
 * Resources that have the header:
 * Content-Type: gzip
 *
 * Used by TextReplayRenderers and other ReplayRenderers that add content to the resulting output
 */
public class GzipDecodingResource extends DecodingResource {
	
	private static final Logger LOGGER = Logger.getLogger(GzipDecodingResource.class.getName());

	public static final String GZIP = "gzip";
	
	public GzipDecodingResource(Resource source) {
		super(source);
		// 2 for GZIP MAGIC bytes.
		source.mark(2);
		try {
			this.setInputStream(new GZIPInputStream(source));
		} catch (IOException io) {
			// If can't read as gzip, might as well as send back raw data.
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("GZIPInputStream failed on " + source + " (" +
						io.getMessage() + ")");
			}
			try {
				source.reset();
			} catch (IOException ex) {
				LOGGER.log(Level.WARNING,
					"reset() failed after GZIPInputStream threw IOException (" +
							io.getMessage() + ") on " + source +
							" (likely to lose first few bytes)", ex);
			}
			this.setInputStream(source);
		}
	}

}
