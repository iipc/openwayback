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
package org.archive.wayback.util;

/**
 * Interface for including, excluding, or aborting filtering
 *
 * @author brad
 * @version $Date$, $Revision$
 * @param <E> 
 */
public interface ObjectFilter<E> {
	/**
	 * constant indicating record should be included in the result set
	 */
	public static int FILTER_INCLUDE = 0;
	
	/**
	 * constant indicating record should be omitted from the result set
	 */
	public static int FILTER_EXCLUDE = 1;
	
	/**
	 * constant indicating record should be ommitted, and no more records should
	 * be processed.
	 */
	public static int FILTER_ABORT = 2;
	
	/** inpect record and determine if it should be included in the 
	 * results or not, or if processing of new records should stop.
	 * @param o Object which should be checked for inclusion/exclusion or abort
	 * @return int of FILTER_INCLUDE, FILTER_EXCLUDE, or FILTER_ABORT
	 */
	public int filterObject(E o);

}
