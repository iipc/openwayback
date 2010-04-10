/* ElementPartitionMap
 *
 * $Id$:
 *
 * Created on Apr 8, 2010.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of Wayback.
 *
 * Wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
