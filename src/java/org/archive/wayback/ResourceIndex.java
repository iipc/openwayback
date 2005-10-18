package org.archive.wayback;

import java.io.IOException;
import java.util.Properties;

import org.archive.wayback.core.ResourceResults;
import org.archive.wayback.core.WMRequest;
import org.archive.wayback.exception.WaybackException;

public interface ResourceIndex {
	public ResourceResults query(final WMRequest request) throws IOException,
			WaybackException;

	public void init(Properties p) throws Exception;
}
