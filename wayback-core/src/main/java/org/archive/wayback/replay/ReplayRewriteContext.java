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

/**
 * Interface for URL-rewriting.
 * Implementation encapsulates replay URL projection scheme, replay
 * request being processed, and URL rewrite implementation that may be
 * customized through ReplayURLTransformer interface.
 * <p>
 * Still not sure about this interface. This is necessary to allow
 * HttpHeaderProcessor to call {@code contextualizeUrl} for rewriting
 * URL in {@code Location} header. It cannot receive ReplayURLTransformer
 * because it is intended for user customization. Perhaps {@code contextualizeUrl}
 * can simply be part of ReplayContext interface.
 * <p>
 */
public interface ReplayRewriteContext extends ReplayContext {
	/**
	 * Special {@code flags} value indicating URL is found in HTTP
	 * response header.
	 */
	public static final String HEADER_CONTEXT = "hd_";

	/**
	 * Rewrite URL {@code url} in accordance with current replay mode, taking
	 * replay context {@code flags} into account.
	 * <p>
	 * It is important to return the same String object {@code url} if no
	 * rewrite is necessary, so that caller can short-circuit to avoid expensive
	 * String operations.
	 * </p>
	 * @param url URL, candidate for rewrite. may contain escaping. must not be
	 *        {@code null}.
	 * @param flags <em>context</em> designator, such as {@code "cs_"}. can be
	 *        {@code null}.
	 * @return rewrittenURL, or {@code url} if no rewrite is necessary. never
	 *         {@code null}.
	 */
	public String contextualizeUrl(final String url, String flags);
}
