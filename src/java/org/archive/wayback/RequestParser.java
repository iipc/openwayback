package org.archive.wayback;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.core.WMRequest;

public interface RequestParser {
	public WMRequest parseRequest(final HttpServletRequest request);
}
