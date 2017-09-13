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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.Resource;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringBuilderTransformer;

/**
 * {@link ReplayRenderer} that rewrites content with {@link StringBuilderTransformer},
 * and inserts {@code jspInserts} at the beginning.
 * 
 * With StringBuilderTransformer, you can take advantage of new URL rewrite interface provided by
 * RelayParseContext.
 * For example, legacy org.archive.wayback.archivalurl.ArchivalUrlCSSReplayRenderer can be replaced
 * with TransformerReplayRenderer configured with BlockCSSStringTransformer. It can rewrite URLs
 * found in CSS with context flags.
 * 
 * This is a more general, more efficient re-implementation of ArchivalURLJSStringTransformerReplayRenderer.
 */
public class TransformerReplayRenderer extends TextReplayRenderer {

	protected StringBuilderTransformer transformer;
	
	/**
	 * @param httpHeaderProcessor which should process HTTP headers
	 */
	public TransformerReplayRenderer(HttpHeaderProcessor httpHeaderProcessor) {
		super(httpHeaderProcessor);
	}
	
	public StringBuilderTransformer getTransformer() {
		return transformer;
	}

	/**
	 * Set StringBuilderTransformer used for rewriting resource content.
	 * @param transformer
	 */
	public void setTransformer(StringBuilderTransformer transformer) {
		this.transformer = transformer;
	}

	@Override
	protected void updatePage(TextDocument page, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ReplayParseContext context,
			Resource resource, CaptureSearchResults results)
			throws ServletException, IOException {
		if (transformer != null) {
			transformer.transform(context, page.sb);
		}
		page.insertAtStartOfDocument(buildInsertText(page, httpRequest,
				httpResponse, context.getWaybackRequest(), results, context.getCaptureSearchResult(), resource));
	}
}
