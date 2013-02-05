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
package org.archive.wayback.util.bdb;

import java.util.NoSuchElementException;

import org.archive.util.iterator.CloseableIterator;

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
