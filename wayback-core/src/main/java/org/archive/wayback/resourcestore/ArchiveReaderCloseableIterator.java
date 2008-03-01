package org.archive.wayback.resourcestore;

import java.io.IOException;
import java.util.Iterator;

import org.archive.io.ArchiveReader;
import org.archive.io.ArchiveRecord;
import org.archive.wayback.util.CloseableIterator;

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
