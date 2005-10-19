/* Resource
 *
 * Created on 2005/10/18 14:00:00
 *
 * Copyright (C) 2005 Internet Archive.
 *
 * This file is part of the Wayback Machine (crawler.archive.org).
 *
 * Wayback Machine is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Wayback Machine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Wayback Machine; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.archive.wayback.core;

import org.archive.io.arc.ARCRecord;

/**
 * Slightly more than an ARCRecord. This class is designed to be an abstraction
 * to allow the Wayback to operator with non-ARC file format resources. Probably
 * the interface required will end up looking very much like ARCRecord, but can
 * be reimplemented to handle new ARC formats or non-ARC formats. At the moment,
 * users of this class just grab the ARCRecord out and use it directly.
 * 
 * @author Brad Tofel
 * @version $Date$, $Revision$
 */
public class Resource {

	ARCRecord arcRecord = null;

	/**
	 * Constructor
	 * 
	 * @param rec
	 */
	public Resource(final ARCRecord rec) {
		super();
		arcRecord = rec;
	}

	/**
	 * @return the ARCRecord underlying this Resource.
	 */
	public ARCRecord getArcRecord() {
		return arcRecord;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
