package org.archive.wayback.resourcestore.locationdb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.util.CloseableIterator;
import org.archive.wayback.util.flatfile.FlatFile;

public class FlatFileResourceFileLocationDB implements ResourceFileLocationDB  {
	private String path = null;
	private FlatFile flatFile = null;
	private String delimiter = "\t";
	

	public void addNameUrl(String name, String url) throws IOException {
		// NO-OP
	}

	public long getCurrentMark() throws IOException {
		return 0;
	}

	public CloseableIterator<String> getNamesBetweenMarks(long start, long end)
			throws IOException {
		return null;
	}

	public String[] nameToUrls(String name) throws IOException {
		ArrayList<String> urls = new ArrayList<String>();
		String prefix = name + delimiter;
		Iterator<String> itr = flatFile.getRecordIterator(prefix);
		while(itr.hasNext()) {
			String line = itr.next();
			if(line.startsWith(prefix)) {
				urls.add(line.substring(prefix.length()));
			} else {
				break;
			}
		}
		if(itr instanceof CloseableIterator) {
			CloseableIterator<String> citr = (CloseableIterator<String>) itr;
			citr.close();
		}
		String[] a = new String[urls.size()];
		for(int i=0; i < urls.size(); i++) {
			a[i] = urls.get(i);
		}
		return a;
	}

	public void removeNameUrl(String name, String url) throws IOException {
		// NO-OP
	}

	public void shutdown() throws IOException {
		// NO-OP
	}

	/**
	 * @param path the path to set
	 */
	public void setPath(String path) {
		this.path = path;
		flatFile = new FlatFile(path);
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param delimter the delimiter to set
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * @return the delimiter
	 */
	public String getDelimiter() {
		return delimiter;
	}
}
