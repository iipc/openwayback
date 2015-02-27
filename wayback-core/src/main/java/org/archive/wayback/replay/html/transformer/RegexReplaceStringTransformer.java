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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;
import org.archive.wayback.replay.html.rewrite.RewriteRule;
import org.archive.wayback.replay.html.rewrite.RewritingStringTransformer;

/**
 * Replaces all occurrence of regular expression {@code regex} in {@code input}
 * with {@code replacement}. If the context has {@code disable-rewrite-}
 * <i>beanName</i> policy, transformation is disabled.
 *
 */
public class RegexReplaceStringTransformer extends RewriteRule implements
		StringTransformer {
	private String regex = "";
	private String replacement = "";
	private Pattern pattern = null;
	// being removed
	private String urlScope = null;

	public String transform(ReplayParseContext context, String input) {

		if (getName() != null) {
			String policy = context.getOraclePolicy();

			// TODO: move this mechanism to MultiRegexReplaceStringTransformer
			if (policy != null &&
					policy.contains("disable-rewrite-" + getName())) {
				return input;
			}
		}

		// being removed
		if (urlScope != null) {
			CaptureSearchResult result = context.getCaptureSearchResult();
			if (result != null && !result.getUrlKey().contains(urlScope)) {
				return input;
			}
		}

		if (pattern == null) {
			return input;
		}
		Matcher m = pattern.matcher(input);
		return m.replaceAll(replacement);
	}

	/**
	 * @return the regex
	 */
	public String getRegex() {
		return regex;
	}

	/**
	 * @param regex the regex to set
	 */
	public void setRegex(String regex) {
		this.regex = regex;
		pattern = Pattern.compile(regex);
	}

	/**
	 * @return the replacement
	 */
	public String getReplacement() {
		return replacement;
	}

	/**
	 * @param replacement the replacement to set
	 */
	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}

	@Override
	public String rewrite(ReplayParseContext context, String policy,
			String input) {

		return transform(context, input);
	}

	public String getUrlScope() {
		return urlScope;
	}

	/**
	 * {@code urlkey} substring for conditional application of
	 * this transformation. If specified, transformation is applied
	 * only when {@code urlkey} contains {@code urlScope}.
	 * <p>Caveat: this functionality is being removed in favor of
	 * more flexible and scalable {@link RewritingStringTransformer}
	 * framework.
	 * @param urlScope
	 */
	public void setUrlScope(String urlScope) {
		this.urlScope = urlScope;
	}
}
