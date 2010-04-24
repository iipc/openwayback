/* CaptureSearchResultPartitionMap
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

package org.archive.wayback.partition;

import java.util.Date;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.partition.ElementPartitionMap;
import org.archive.wayback.util.partition.Partition;

/**
 * @author brad
 *
 */
public class CaptureSearchResultPartitionMap 
	implements ElementPartitionMap<CaptureSearchResult> {

	public void addElementToPartition(CaptureSearchResult element,
			Partition<CaptureSearchResult> partition) {
		partition.add(element);
		partition.addTotal(1);
		if(element.isClosest()) {
			partition.setContainsClosest(true);
		}
	}

	public Date elementToDate(CaptureSearchResult element) {
		return element.getCaptureDate();
	}

}
