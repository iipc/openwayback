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
package org.archive.wayback.accesscontrol;

import org.archive.wayback.resourceindex.filters.ExclusionFilter;
/**
 * The factory for {@link ExclusionFilter} object.
 * Life cycle of ExclusionFilter is one replay session request. This interface is used
 * for creating an ExclusionFilter instance for each replay session.
 * <p>
 * Migrating to more flexible and easier-to-implement {@link ContextExclusionFilterFactory}.
 * Please consider implementing it instead.
 * </p>
 */
public interface ExclusionFilterFactory {
	/**
	 * @return an ObjectFilter object that filters records based on
	 * some set of exclusion rules
	 */
	public ExclusionFilter get();
	/**
	 * close any resources used by this ExclusionFilter system.
	 */
	public void shutdown();
}
