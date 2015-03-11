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
package org.archive.wayback.replay.html;

import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.accesspoint.AccessPointAdapter;
import org.archive.wayback.archivalurl.ArchivalURLJSStringTransformerReplayRenderer;
import org.archive.wayback.archivalurl.ArchivalUrlSAXRewriteReplayRenderer;

/**
 * Abstracts creation of specialized ResultURIConverters based on particular
 * flags.
 * <p>
 * Currently {@code ContextResultURIConverterFactory} is used in two different
 * contexts, and it receives critically different information as {@code flags}
 * argument for each context.
 * <ol>
 * <li>customizing the way parent request handler builds
 * {@link ResultURIConverter} for each of its children
 *  - receives {@code replayURIPrefix} as {@code flags}.
 * ({@link AccessPointAdapter})</li>
 * <li>passing requested resource type (often called <i>flags</i>) information to
 * {@link ReplayParseContext}
 * - receives resource type (such as {@code "cs_"}) as {@code flags}.
 * ({@link ArchivalUrlSAXRewriteReplayRenderer} and
 * {@link ArchivalURLJSStringTransformerReplayRenderer}).</li>
 * </ol>
 * <p>Using single interface in semantically different contexts leads to
 * a lot of confusion and awkward code. Redesign is highly desired.
 * Current plan is to do away with usage 2, and design better interface for
 * usage 1.</p>
 * <p>
 * {@link ReplayURLTransformer} replaces use #2 of this interface. It is much simpler
 * to implement and configure. #2 use of this interface is deprecated.
 * </p>
 * @author brad
 * @see ReplayParseContext#makeConverter
 * @see AccessPointAdapter#getUriConverter
 */
public interface ContextResultURIConverterFactory {
	public ResultURIConverter getContextConverter(String flags);
}
