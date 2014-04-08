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

/**
 * Transforms text with certain rewrite rules.
 * <p>Input text may be entire HTML, JavaScript block, or as small as
 * single attribute value.</p>
 */
public interface StringTransformer {
	/**
	 * transforms text with certain rewrite rules.
	 * <p>Refactoring: change <code>input</code> type to <code>CharSequence</code>.
	 * @param context ReplayParseContext
	 * @param input text data to be transformed
	 * @return transformed text
	 */
	public String transform(ReplayParseContext context, String input);
}
