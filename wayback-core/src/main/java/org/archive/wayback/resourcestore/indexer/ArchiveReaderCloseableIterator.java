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
package org.archive.wayback.resourcestore.indexer;

import java.io.IOException;
import java.util.Iterator;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.util.iterator.CloseableIterator;

public class ArchiveReaderCloseableIterator implements CloseableIterator<ArchiveRecord> {
	private ArchiveReader reader = null;
	private Iterator<ArchiveRecord> itr = null;
	public ArchiveReaderCloseableIterator(ArchiveReader reader, Iterator<ArchiveRecord> itr) {
		this.reader = reader;
		this.itr = itr;
	}
	public boolean hasNext() {
		return itr.hasNext();
	}
	public ArchiveRecord next() {
		return itr.next();
	}
	public void remove() {
		itr.remove();
	}
	public void close() throws IOException {
		reader.close();
	}
}
