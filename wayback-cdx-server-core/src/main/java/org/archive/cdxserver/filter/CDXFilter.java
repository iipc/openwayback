package org.archive.cdxserver.filter;

import org.archive.format.cdx.CDXLine;

public interface CDXFilter {
	public boolean include(CDXLine line);
}
