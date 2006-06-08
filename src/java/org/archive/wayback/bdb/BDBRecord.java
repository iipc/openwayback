/* BDBRecord
 *
 * $Id$
 *
 * Created on 1:43:15 PM May 15, 2006.
 *
 * Copyright (C) 2006 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.bdb;

import com.sleepycat.je.DatabaseEntry;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBRecord {
	private DatabaseEntry key;
	private DatabaseEntry value;
	/**
	 * @param key
	 * @param value
	 */
	public BDBRecord(DatabaseEntry key, DatabaseEntry value) {
		this.key = key;
		this.value = value;
	}
	/**
	 * @return Returns the key.
	 */
	public DatabaseEntry getKey() {
		return key;
	}
	/**
	 * @return Returns the value.
	 */
	public DatabaseEntry getValue() {
		return value;
	}
}
