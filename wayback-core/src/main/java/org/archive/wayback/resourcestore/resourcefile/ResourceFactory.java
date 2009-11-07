/* ResourceFactory
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback-core.
 *
 * wayback-core is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback-core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-core; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.resourcestore.resourcefile;

import java.io.File;
import java.io.IOException;
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
		if (isArc(name)) {

			ARCReader reader = ARCReaderFactory.get(file,offset);
			r = ARCArchiveRecordToResource(reader.get(),reader);

		} else if (isWarc(name)) {

			WARCReader reader = WARCReaderFactory.get(file,offset);
			r = WARCArchiveRecordToResource(reader.get(),reader);

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
			ARCReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof ARCRecord)) {
			throw new ResourceNotAvailableException("Bad ARCRecord format");
		}
		ArcResource ar = new ArcResource((ARCRecord) rec, reader);
		ar.parseHeaders();
		return ar;
	}

	public static Resource WARCArchiveRecordToResource(ArchiveRecord rec,
			WARCReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof WARCRecord)) {
			throw new ResourceNotAvailableException("Bad WARCRecord format");
		}
		WarcResource wr = new WarcResource((WARCRecord) rec, reader);
		wr.parseHeaders();
		return wr;
	}
}
