package org.archive.wayback.localresourcestore;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.archive.io.arc.ARCLocation;
import org.archive.io.arc.ARCReader;
import org.archive.io.arc.ARCReaderFactory;
import org.archive.wayback.ResourceStore;
import org.archive.wayback.core.Resource;

public class LocalARCResourceStore implements ResourceStore {
	private static final String RESOURCE_PATH = "resourcestore.arcpath";

	private static final String ARCTAIL = ".arc.gz";

	private String path = null;

	public LocalARCResourceStore() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void init(Properties p) throws Exception {
		String configPath = (String) p.get(RESOURCE_PATH);
		if ((configPath == null) || (configPath.length() < 1)) {
			throw new IllegalArgumentException("Failed to find "
					+ RESOURCE_PATH);
		}
		path = configPath;

	}

	public Resource retrieveResource(ARCLocation location) throws IOException {
		String arcName = location.getName();
		if (!arcName.endsWith(ARCTAIL)) {
			arcName += ARCTAIL;
		}
		File arcFile = new File(arcName);
		if (!arcFile.isAbsolute()) {
			arcFile = new File(this.path, arcName);
		}
		if (!arcFile.exists() || !arcFile.canRead()) {
			throw new IOException("Cannot find ARC file ("
					+ arcFile.getAbsolutePath() + ")");
		} else {
			ARCReader reader = ARCReaderFactory.get(arcFile);
			Resource r = new Resource(reader.get(location.getOffset()));
			return r;
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
