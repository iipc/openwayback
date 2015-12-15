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

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

/**
 * Translates absolute URLs found in JavaScript code block.
 * <p>Looks for http/https absolute URLs in JavaScript code and translates
 * them with {@link ReplayParseContext#contextualizeUrl(String)}.</p>
 * <p>You can customize the pattern for finding URLs with {@code regex} property.
 * Regular expression must have at least one <em>capturing group</em>, and the first
 * capturing group is assumed to enclose URL to be rewritten.
 * (new feature 2014-04-22) Any matching text preceding and
 * following the first group will be preserved in the output.</p>
 * <p>For example: if you want to replace protocol-relative URL in addition to
 * regular full URL in JavaScript, you could use conservative regex like:
 * <pre>
 * "[\"']((?:https?:)?//(?:[^/]+@)?[^@:/]+(?:\\.[^@:/]+)+(?:[0-9]+)?)"
 * </pre>
 * Note single/double quote preceding URL is preserved in 2014-04-22 version and on.</p>
 * <p>TODO: org.archive.wayback.archivalurl.ArchivalUrlJSReplayRenderer has
 * similar code.  can be consolidated, like ArchivalURLJSStringTransformerReplayRenderer?</p>
 * <p>May 1, 2014: slight design change:
 * Now JSStringTransformer does not run it's own should-rewrite check and sends all matching
 * text to {@link ReplayParseContext#contextualizeUrl(String)}. More specifically it no longer
 * be affected by {@code rewriteHttpsOnly} flag. This is a design choice to keep
 * {@code StringTransformer} detached from replay mode knowledge and focus on find-and-replace URLs
 * </p>
 * @author brad
 *
 */
public class JSStringTransformer implements StringTransformer {
	private static final Logger LOGGER = Logger
		.getLogger(JSStringTransformer.class.getName());

	private final static Pattern defaultHttpPattern = Pattern
	.compile("(https?:\\\\?/\\\\?/[A-Za-z0-9:_@.-]+)");

	private Pattern pattern = defaultHttpPattern;
	private String escaping;
	// we could expose SourceEscaping interface and sourceEscaping for higher
	// degree of customization
	private SourceEscaping sourceEscaping;

	public interface SourceEscaping {
		public String unescape(String text);
		public String escape(String text);
	}
	
	/**
	 * SourceEscaping implemented with commons-lang {@link StringEscapeUtils}
	 */
	public class CommonsLangEscaping implements SourceEscaping {
		private Method escapeMethod;
		private Method unescapeMethod;

		/**
		 * Initialize with escaping scheme name.
		 * See {@code escape}... methods of {@link StringEscapeUtils} for supported
		 * escaping scheme names. {@code null} or empty string are valid, and will
		 * make a no-op escaping.
		 * @param name escaping scheme name, ex. "JavaScript"
		 */
		public CommonsLangEscaping(String name) {
			if (name == null || name.isEmpty()) {
				escapeMethod = unescapeMethod = null;
			} else {
				if (name.equals("javascript")) {
					name = "JavaScript";
				} else {
					if (Character.isLowerCase(name.charAt(0))) {
						name = name.substring(0, 1).toUpperCase() +
								name.substring(1);
					}
				}
				try {
					escapeMethod = StringEscapeUtils.class.getMethod("escape" +
							name, String.class);
				} catch (NoSuchMethodException ex) {
					throw new IllegalArgumentException(
						"StringEscapeUtils.escape" + name, ex);
				}
				try {
					unescapeMethod = StringEscapeUtils.class.getMethod(
						"unescape" + name, String.class);
				} catch (NoSuchMethodException ex) {
					throw new IllegalArgumentException(
						"StringEscapeUtils.unescape" + name, ex);
				}
			}
		}
		
		public String unescape(String text) {
			if (unescapeMethod != null) {
				try {
					return (String)unescapeMethod.invoke(null, text);
				} catch (Exception ex) {
					LOGGER.warn("Error unescaping text \"" + text + "\" with " +
							unescapeMethod, ex);
				}
			}
			return text;
		}

		public String escape(String text) {
			if (escapeMethod != null) {
				try {
					return (String)escapeMethod.invoke(null, text);
				} catch (Exception ex) {
					LOGGER.warn("Error escaping text \"" + text +
							"\" with " + escapeMethod, ex);
				}
			}
			return text;
		}
	}

	/**
	 * a regular expression for searching URLs in the target resource.
	 * @param regex
	 */
	public void setRegex(String regex) {
		pattern = Pattern.compile(regex);
	}

	public String getRegex() {
		return pattern.pattern();
	}

	/**
	 * Naming of escaping scheme applied to extracted text.
	 * @param escaping escaping scheme name, such as {@code JavaScript},
	 * {@code Xml}
	 * @see StringEscapeUtils
	 */
	public void setEscaping(String escaping) {
		this.sourceEscaping = new CommonsLangEscaping(escaping);
		this.escaping = escaping;
	}

	public String getEscaping() {
		return escaping;
	}

	public String transform(ReplayParseContext context, String input) {
		StringBuffer replaced = new StringBuffer(input.length());
		Matcher m = pattern.matcher(input);
		while (m.find()) {
			String rawUrl = m.group(1);
			String pre = input.substring(m.start(), m.start(1));
			String post = input.substring(m.end(1), m.end());

			String origUrl = sourceEscaping != null ? sourceEscaping.unescape(rawUrl) : rawUrl;
			String url = context.contextualizeUrl(origUrl);

			if (url != origUrl) {
				// reverse some changes made to url by contextualizeUrl method, that
				// may break assumptions in subsequent JavaScript processing.
				// eg. "http://example.org" -> "/20140101012345/http://example.org/"
				// eg. "https://domain" + ".example.org" -> "http://domain/" + ".example.org"
				// eg. "https://domain." + "example.org" -> "http://domain" + "example.org"

				// remove trailing "/" if origUrl doesn't have it.  As Wayback does not need
				// trailing slash, it may make sense to this everywhere.  Just doing this fix
				// in JavaScript for now.
				if (url.endsWith("/") && !origUrl.endsWith("/")) {
					url = url.substring(0, url.length() - 1);
				}

				// add trailing "." (removed by canonicalizer) back, if origUrl has it.
				if (origUrl.endsWith(".") && !url.endsWith(".")) {
					url = url + ".";
				}
				
				if (sourceEscaping != null) {
					url = sourceEscaping.escape(url);
				}
			} else {
				// use the original rawUrl
				url = rawUrl;
			}
			m.appendReplacement(replaced, Matcher.quoteReplacement(pre + url + post));
		}
		m.appendTail(replaced);
		return replaced.toString();
	}
}
