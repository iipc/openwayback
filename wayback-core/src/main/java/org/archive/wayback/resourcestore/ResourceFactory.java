package org.archive.wayback.resourcestore;

import java.io.File;
import java.io.IOException;

import org.archive.io.ArchiveRecord;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.io.arc.ARCRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.wayback.core.Resource;
import org.archive.wayback.exception.ResourceNotAvailableException;

public class ResourceFactory {
	
	public static Resource getResource(File file, long offset) 
	throws IOException, ResourceNotAvailableException {

		Resource r = null;
		String name = file.getName();
		if(name.endsWith(LocalResourceStore.ARC_EXTENSION) || 
				name.endsWith(LocalResourceStore.ARC_GZ_EXTENSION)) {

			ARCReader reader = ARCReaderFactory.get(file);
			ArchiveRecord rec = reader.get(offset);
			if(!(rec instanceof ARCRecord)) {
				throw new ResourceNotAvailableException("Bad ARCRecord format");
			}
			ArcResource ar = new ArcResource((ARCRecord) rec, reader);
			ar.parseHeaders();
			r = ar;

		} else if(name.endsWith(LocalResourceStore.WARC_EXTENSION) || 
				name.endsWith(LocalResourceStore.WARC_GZ_EXTENSION)) {

			WARCReader reader = WARCReaderFactory.get(file);
			ArchiveRecord rec = reader.get(offset);
			if(!(rec instanceof ARCRecord)) {
				throw new ResourceNotAvailableException("Bad ARCRecord format");
			}
			WarcResource wr = new WarcResource((WARCRecord) rec, reader);
			wr.parseHeaders();
			r = wr;
		}		
		
		return r;
	}
}
