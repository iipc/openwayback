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
package org.archive.wayback.resourcestore.resourcefile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;

/**
 * Static factory class for constructing ARC/WARC Resources from 
 * File/URL + offset.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class ResourceFactory {

	public static Resource getResource(String urlOrPath, long offset)
	throws IOException, ResourceNotAvailableException {
		if(urlOrPath.startsWith("http://")) {
			return getResource(new URL(urlOrPath), offset);
		} else {
			// assume local path:
			return getResource(new File(urlOrPath), offset);
		}
	}

	public static Resource getResource(File file, long offset)
			throws IOException, ResourceNotAvailableException {

		Resource r = null;
		String name = file.getName();
		if (name.endsWith(ArcWarcFilenameFilter.OPEN_SUFFIX)) {
			name = name.substring(0, name.length()
					- ArcWarcFilenameFilter.OPEN_SUFFIX.length());
		}
		RandomAccessFile raf = new RandomAccessFile(file, "r");
		raf.seek(offset);
		InputStream is = new FileInputStream(raf.getFD());
		String fPath = file.getAbsolutePath();
		if (isArc(name)) {
			ArchiveReader reader = ARCReaderFactory.get(fPath, is, false);
			r = ARCArchiveRecordToResource(reader.get(), reader);

		} else if (isWarc(name)) {

			ArchiveReader reader = WARCReaderFactory.get(fPath, is, false);
			r = WARCArchiveRecordToResource(reader.get(), reader);

		} else {
			throw new ResourceNotAvailableException("Unknown extension");
		}

		return r;
	}
	public static Resource getResource(URL url, long offset)
	throws IOException, ResourceNotAvailableException {
		
		Resource r = null;
		// TODO: allow configuration of timeouts -- now using defaults..
		TimeoutArchiveReaderFactory tarf = new TimeoutArchiveReaderFactory();
		ArchiveReader reader = tarf.getArchiveReader(url,offset);
		if(reader instanceof ARCReader) {
			ARCReader areader = (ARCReader) reader;
			r = ARCArchiveRecordToResource(areader.get(),areader);
		
		} else if(reader instanceof WARCReader) {
			WARCReader wreader = (WARCReader) reader;
			r = WARCArchiveRecordToResource(wreader.get(),wreader);
			
		} else {
			throw new ResourceNotAvailableException("Unknown ArchiveReader");
		}
		return r;
	}
	
	
	private static boolean isArc(final String name) {

		return (name.endsWith(ArcWarcFilenameFilter.ARC_SUFFIX)
				|| name.endsWith(ArcWarcFilenameFilter.ARC_GZ_SUFFIX));
	}

	private static boolean isWarc(final String name) {

		return (name.endsWith(ArcWarcFilenameFilter.WARC_SUFFIX)
			|| name.endsWith(ArcWarcFilenameFilter.WARC_GZ_SUFFIX));	
	}
	
	public static Resource ARCArchiveRecordToResource(ArchiveRecord rec,
			ArchiveReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof ARCRecord)) {
			throw new ResourceNotAvailableException("Bad ARCRecord format");
		}
		ArcResource ar = new ArcResource((ARCRecord) rec, reader);
		ar.parseHeaders();
		return ar;
	}

	public static Resource WARCArchiveRecordToResource(ArchiveRecord rec,
			ArchiveReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof WARCRecord)) {
			throw new ResourceNotAvailableException("Bad WARCRecord format");
		}
		WarcResource wr = new WarcResource((WARCRecord) rec, reader);
		wr.parseHeaders();
		return wr;
	}
}
