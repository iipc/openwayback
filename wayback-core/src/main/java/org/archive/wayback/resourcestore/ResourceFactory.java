package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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

	public static Resource getResource(File file, long offset)
			throws IOException, ResourceNotAvailableException {

		Resource r = null;
		String name = file.getName();
		if (name.endsWith(LocalResourceStore.OPEN_EXTENSION)) {
			name = name.substring(0, name.length()
					- LocalResourceStore.OPEN_EXTENSION.length());
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
		String name = url.getFile();
		if (isArc(name)) {

			ARCReader reader = ARCReaderFactory.get(url, offset);
			r = ARCArchiveRecordToResource(reader.get(),reader);

		} else if (isWarc(name)) {

			WARCReader reader = WARCReaderFactory.get(url, offset);
			r = WARCArchiveRecordToResource(reader.get(),reader);

		} else {
			throw new ResourceNotAvailableException("Unknown extension");
		}
		return r;
	}

	private static boolean isArc(final String name) {

		return (name.endsWith(LocalResourceStore.ARC_EXTENSION)
				|| name.endsWith(LocalResourceStore.ARC_GZ_EXTENSION));
	}

	private static boolean isWarc(final String name) {

		return (name.endsWith(LocalResourceStore.WARC_EXTENSION)
			|| name.endsWith(LocalResourceStore.WARC_GZ_EXTENSION));	
	}
	
	private static Resource ARCArchiveRecordToResource(ArchiveRecord rec,
			ARCReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof ARCRecord)) {
			throw new ResourceNotAvailableException("Bad ARCRecord format");
		}
		ArcResource ar = new ArcResource((ARCRecord) rec, reader);
		ar.parseHeaders();
		return ar;
	}

	private static Resource WARCArchiveRecordToResource(ArchiveRecord rec,
			WARCReader reader) throws ResourceNotAvailableException, IOException {

		if (!(rec instanceof WARCRecord)) {
			throw new ResourceNotAvailableException("Bad WARCRecord format");
		}
		WarcResource wr = new WarcResource((WARCRecord) rec, reader);
		wr.parseHeaders();
		return wr;
	}
}
