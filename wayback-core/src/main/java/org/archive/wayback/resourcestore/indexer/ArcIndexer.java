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

import java.io.File;
import java.io.IOException;

import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.util.iterator.CloseableIterator;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.AdaptedIterator;
import org.archive.wayback.util.Adapter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

/**
 * Transforms an ARC file into Iterator<CaptureSearchResult>.
 * 
 * @author brad
 * @version $Date$, $Revision$
 */
public class ArcIndexer {

	private UrlCanonicalizer canonicalizer = null;
	
	public ArcIndexer() {
		canonicalizer = new AggressiveUrlCanonicalizer();
	}

	/**
	 * @param arc
	 * @return Iterator of SearchResults for input arc File
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(File arc)
	throws IOException {
		return iterator(ARCReaderFactory.get(arc));
	}

	/**
	 * @param pathOrUrl
	 * @return Iterator of SearchResults for input pathOrUrl
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(String pathOrUrl)
	throws IOException {
		File f = new File(pathOrUrl);
		if(f.isFile()) {
			return iterator(ARCReaderFactory.get(f));
		} else {
			return iterator(ARCReaderFactory.get(pathOrUrl));
		}
	}
	
	/**
	 * @param arcReader
	 * @return Iterator of SearchResults for input ARCReader
	 * @throws IOException
	 */
	public CloseableIterator<CaptureSearchResult> iterator(ARCReader arcReader)
	throws IOException {
		arcReader.setParseHttpHeaders(true);

		Adapter<ArchiveRecord,ARCRecord> adapter1 =
			new ArchiveRecordToARCRecordAdapter();

		ARCRecordToSearchResultAdapter adapter2 =
			new ARCRecordToSearchResultAdapter();
		adapter2.setCanonicalizer(canonicalizer);
		
		ArchiveReaderCloseableIterator itr1 = 
			new ArchiveReaderCloseableIterator(arcReader,arcReader.iterator());

		CloseableIterator<ARCRecord> itr2 = 
			new AdaptedIterator<ArchiveRecord,ARCRecord>(itr1,adapter1);
		
		return new AdaptedIterator<ARCRecord,CaptureSearchResult>(itr2,adapter2);
	}
	
	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	private class ArchiveRecordToARCRecordAdapter 
	implements Adapter<ArchiveRecord,ARCRecord> {

		/* (non-Javadoc)
		 * @see org.archive.wayback.util.Adapter#adapt(java.lang.Object)
		 */
		public ARCRecord adapt(ArchiveRecord o) {
			ARCRecord rec = null;
			if(o instanceof ARCRecord) {
				rec = (ARCRecord) o;
			}
			return rec;
		}
	}
}
