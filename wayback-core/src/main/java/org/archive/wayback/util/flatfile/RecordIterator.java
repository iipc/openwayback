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
package org.archive.wayback.util.flatfile;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.archive.util.iterator.CloseableIterator;

/**
* Iterator that returns sequential lines from a file.
*
* @author brad
* @version $Date$, $Revision$
*/
public class RecordIterator implements CloseableIterator<String> {
	private BufferedReader br;

	protected String next = null;

	protected boolean done = false;

	/**
	 * @param br
	 */
	public RecordIterator(BufferedReader br) {
		this.br = br;
		if(br == null) {
			done = true;
		}
	}

	public boolean hasNext() {
		if (next != null)
			return true;
		if(done) {
			return false;
		}
		try {
			String nextLine = br.readLine();
			if (nextLine == null) {
				close();
			} else {
				next = nextLine;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return (next != null);
	}

	public String next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}
		// 'next' is guaranteed non-null by a hasNext() which returned true
		String returnString = this.next;
		this.next = null;
		return returnString;
	}
	/**
	 * @throws IOException
	 */
	public void close() throws IOException {
		if(done == false) {
			done = true;
			br.close();
		}
	}
	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new RuntimeException("unsupported remove");
	}
}
