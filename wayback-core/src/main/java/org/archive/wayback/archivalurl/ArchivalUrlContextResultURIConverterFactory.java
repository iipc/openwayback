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
package org.archive.wayback.archivalurl;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.replay.html.ContextResultURIConverterFactory;

/**
 * {@link ContextResultURIConverterFactory} which creates a context specific
 * ArchivalUrlResultURIConverter, given a base ArchivalUrlResultURIConverter and
 * the flags to add.
 * <p>
 * Note that this class expects requested resource type flags (ex {@code "cs_"}
 * as an argument to {@link #getContextConverter(String)}. This is a very
 * specific usage and It will not be useful other than the context of rewriting
 * resources with specific non-default types of hyper-links (specifically,
 * {@link ArchivalUrlSAXRewriteReplayRenderer}, and
 * {@link ArchivalURLJSStringTransformerReplayRenderer}).
 * </p>
 * <p>
 * It is highly likely this class gets refactored, and may be discontinued as a
 * result.
 * </p>
 * <p>
 * Now this class is deprecated. {@link ArchivalUrlResultURIConverter} has taken over
 * this class.
 * @author brad
 * @deprecated 2015-01-16 use {@link ArchivalUrlResultURIConverter} if ever needed.
 */
public class ArchivalUrlContextResultURIConverterFactory 
	implements ContextResultURIConverterFactory {

	private ArchivalUrlResultURIConverter converter = null;

	/**
	 * @param converter base ArchivalURLURLConverter to wrap
	 */
	public ArchivalUrlContextResultURIConverterFactory(
			ArchivalUrlResultURIConverter converter) {
		this.converter = converter;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.replay.html.ContextResultURIConverterFactory#getContextConverter(java.lang.String)
	 */
	public ResultURIConverter getContextConverter(String flags) {
		return converter.getContextConverter(flags);
	}

}
