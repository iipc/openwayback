/* BDBRecordIterator
 *
 * $Id$
 *
 * Created on 1:27:24 PM May 15, 2006.
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
package org.archive.wayback.util.bdb;

import java.util.NoSuchElementException;

import org.archive.wayback.util.CloseableIterator;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 *
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class BDBRecordIterator implements CloseableIterator<BDBRecord> {
	
	DatabaseEntry key;
	DatabaseEntry value;
	boolean hitLast;
	boolean gotNext;
	Cursor cursor;
	boolean backward;
	BDBRecord record;

	/**
	 * @param cursor
	 * @param search 
	 * @throws DatabaseException 
	 */
	public BDBRecordIterator(Cursor cursor,String search) 
	throws DatabaseException {
		initialize(cursor,search,false);
	}
	/**
	 * @param cursor
	 * @param search 
	 * @param backward
	 * @throws DatabaseException 
	 */
	public BDBRecordIterator(Cursor cursor,String search,boolean backward) 
	throws DatabaseException {
		initialize(cursor,search,backward);
	}
	private void initialize(Cursor cursor,String search, boolean backward)
	throws DatabaseException {
		this.cursor = cursor;
		this.backward = backward;
		key = new DatabaseEntry();
		value = new DatabaseEntry();
		key.setData(search.getBytes());
		key.setPartial(false);
		OperationStatus status = cursor.getSearchKeyRange(key, value,
				LockMode.DEFAULT);
		if(backward && (status == OperationStatus.SUCCESS)) {
			// if we are in reverse, immediately back up one record:
			status = cursor.getPrev(key, value, LockMode.DEFAULT);
		}
		if(status == OperationStatus.SUCCESS) {
			gotNext = true;
		}
		record = new BDBRecord(key,value);
	}	

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if(hitLast) {
			return false;
		}
		if(cursor == null) {
			return false;
		}
		if(!gotNext) {
			OperationStatus status;
			// attempt to get the next:
			try {
				if(backward) {
					status = cursor.getPrev(key, value, LockMode.DEFAULT);
				} else {
					status = cursor.getNext(key, value, LockMode.DEFAULT);
				}
				if(status == OperationStatus.SUCCESS) {
					gotNext = true;
				} else {
					close();
				}
			} catch (DatabaseException e) {
				// SLOP: throw a runtime?
				e.printStackTrace();
				close();
			}			
		}
		return gotNext;
	}

	public void close() {
		if(!hitLast) {
			hitLast = true;
			try {
				cursor.close();
			} catch (DatabaseException e) {
				// TODO what to do?
				// let's just eat it for now..
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	public BDBRecord next() {
		if(!gotNext) {
			throw new NoSuchElementException();
		}
		gotNext = false;
		return record;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
