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

import org.archive.wayback.replay.html.StringTransformer;

/**
 * {@link StringTransformer} that aggregates multiple sub-<code>StringTransformer</code>s that
 * are applied in sequential manner.
 * <p>Despite the name, this class has nothing to do with regular expression.
 * sub-StringTransformers can be any <code>StringTransformer</code> regardless of
 * being regular-expression base or not.</p>
 * <p>2014-05-11: renamed to {@link CompositeStringTransformer}</p>.
 * @deprecated 2014-11-05 use {@link CompositeStringTransformer}
 */
public class MultiRegexReplaceStringTransformer extends CompositeStringTransformer {
}
