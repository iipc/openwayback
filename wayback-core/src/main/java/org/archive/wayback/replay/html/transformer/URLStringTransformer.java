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
package org.archive.wayback.replay.html.transformer;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

/**
 * StringTransformer for translating URLs.
 * <p>input is a URL (strictly speaking, URI), typically from an HTML attribute.</p>
 * <p>As translation is simply delegated to {@link ReplayParseContext} passed to
 * {@link #transform} method, this class is merely a holder
 * of <code>flags</code> value representing a type of context pointed resource is used.
 * This is necessary because StringTransformer interface does not allow for communicating
 * this information to {@link ReplayParseContext#contextualizeUrl(String, String)}.</p>
 * </p>
 * <p>It delegates translation to {@code jsTransformer} if given string
 * is <code>javascript:</code>.  This is used by FastArchivalUrlRelayParserEventHandler
 * for rewriting HREF attributes.</p>
 * <p>Possible Refactoring:
 * <ul>
 * <li>communicate flags information through ReplayParserContext?</li>
 * <li>let FastArchivalUrlReplayParseEventHandler call contextualizeUrl(String, String)
 * directly?</li>
 * <li>move this class to non-static inner class of FastArchivalUrlReplayParseEventHandler.
 * Perhaps it doesn't need to implement StringTransformer at all
 * (sub-class MetaRefreshUrlStringTransformer needs separate rewrite.)</li>
 * </ul>
 * </p>
 * @see org.archive.wayback.archivalurl.FastArchivalUrlReplayParseEventHandler
 * @see MetaRefreshUrlStringTransformer
 * @author brad
 *
 */
public class URLStringTransformer implements StringTransformer {
	private String flags;
	private StringTransformer jsTransformer = null;
	/** Default constructor */
	public URLStringTransformer() {}
	/** 
	 * Flag-setting constructor.
	 * @param flags String representing how resource is used
	 * (ex. "<code>im_</code>", "<code>cs_</code>")
	 */
	public URLStringTransformer(String flags) {
		this.flags = flags;
	}
	
	public String transform(ReplayParseContext context, String url) {
	    if (!context.isRewriteSupported(url)) {
	    	return url;
	    }
	    
		if(url.startsWith(ReplayParseContext.JAVASCRIPT_PREFIX)) {
			if(jsTransformer == null) {
				return url;
			}
			StringBuilder sb = new StringBuilder(url.length());
			sb.append(ReplayParseContext.JAVASCRIPT_PREFIX);
			String jsFragment = url.substring(
					ReplayParseContext.JAVASCRIPT_PREFIX.length());
			sb.append(jsTransformer.transform(context, jsFragment));
			return sb.toString();
		}
		return context.contextualizeUrl(url, flags);
	}

	/** @return the flags */
	public String getFlags() {
		return flags;
	}

	/** @param flags the flags to set */
	public void setFlags(String flags) {
		this.flags = flags;
	}

	public StringTransformer getJsTransformer() {
		return jsTransformer;
	}
	/**
	 * transformer for <code>javascript:</code> URIs.
	 * <p>if unspecified (<code>null</code>), <code>javascript:</code>
	 * URI is left unprocessed.</p>
	 * @param jsTransformer StringTransformer
	 */
	public void setJsTransformer(StringTransformer jsTransformer) {
		this.jsTransformer = jsTransformer;
	}

}
