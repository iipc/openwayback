package org.archive.wayback;

import java.io.IOException;
import java.util.Properties;

import org.archive.io.arc.ARCLocation;
import org.archive.wayback.core.Resource;

public interface ResourceStore {
	public Resource retrieveResource(ARCLocation location) throws IOException;

	public void init(Properties p) throws Exception;
}
