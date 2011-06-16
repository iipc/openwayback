package org.archive.wayback.resourceindex.ziplines;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HDFSBlockLoader implements BlockLoader {
	FileSystem fs = null;
	String defaultFSURI = null;
	public HDFSBlockLoader(String defaultFSURI) {
		this.defaultFSURI = defaultFSURI;
	}
	public void init() throws IOException, URISyntaxException {
		Configuration c = new Configuration();
		c.set("fs.default.name",defaultFSURI);
		fs = FileSystem.get(new URI(defaultFSURI),c);
	}

	public byte[] getBlock(String url, long offset, int length)
			throws IOException {
		Path path = new Path(url);
		FSDataInputStream s = fs.open(path);
		byte buffer[] = new byte[length];
		s.readFully(offset, buffer);
		return buffer;
	}

	/**
	 * @return the defaultFSURI
	 */
	public String getDefaultFSURI() {
		return defaultFSURI;
	}

	/**
	 * @param defaultFSURI the defaultFSURI to set
	 */
	public void setDefaultFSURI(String defaultFSURI) {
		this.defaultFSURI = defaultFSURI;
	}
}
