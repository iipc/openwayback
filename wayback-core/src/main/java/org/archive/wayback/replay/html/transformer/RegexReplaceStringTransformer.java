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

import org.archive.wayback.accesscontrol.oracleclient.CustomPolicyOracleFilter;
import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

public class RegexReplaceStringTransformer implements StringTransformer {
	private String regex = "";
	private String replacement = "";
	private Pattern pattern = null;
	private String ruleId;

	public String transform(ReplayParseContext context, String input) {
		
		if (ruleId != null) {
			String policy = context.getData(CustomPolicyOracleFilter.CAPTURE_ORACLE_POLICY);
			
			if (policy != null && policy.contains("disable-rewrite-" + ruleId)) {
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

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
}
