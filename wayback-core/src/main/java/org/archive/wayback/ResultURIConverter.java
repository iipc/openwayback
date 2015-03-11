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
package org.archive.wayback;

/**
 * Interface for implementations that convert a string datespec and URL into
 * an absolute URL that will replay the specified URL at the specified date.
 * <p>
 * This interface has also been used for URL-rewriting. It lead to
 * confusion and convoluted code. Now it has been refactored into two
 * interfaces: ReplayURIConverter and ReplayURLTransformer.
 * @author brad
 * @see ReplayURIConverter
 * @see ReplayURLTransformer
 */
public interface ResultURIConverter {
	/**
	 * return an absolute URL that will replay URL url at time datespec.
	 * 
	 * @param datespec 14-digit timestamp for the desired Resource
	 * (also often include context flags such as "{@code cs_}")
	 * @param url for the desired Resource
	 * @return absolute replay URL
	 */
	public String makeReplayURI(final String datespec, final String url);
}
