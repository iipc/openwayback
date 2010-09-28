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
package org.archive.wayback.util.partition;

import java.util.Date;

/**
 * @author brad
 * @param <T> Specific Class which can be mapped to a Date, and added to a 
 * Partition
 *
 */
public interface ElementPartitionMap<T> {
	/**
	 * Convert an element to a Date ex:
	 * 
	 *    return element.getDate();
	 *    
	 * @param element the element to convert
	 * @return the Date for the element
	 */
	public Date elementToDate(T element);
	
	/**
	 * Add the element to a partition, possible modifying the Partition in some
	 * way. ex:
	 * 
	 * 	partition.add(element);
	 *  partition.addTotal(1);
	 * 
	 * @param element to be added
	 * @param partition to which the element should be added
	 */
	public void addElementToPartition(T element, Partition<T> partition);

}
