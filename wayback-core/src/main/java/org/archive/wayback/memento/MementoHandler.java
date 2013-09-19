package org.archive.wayback.memento;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

public interface MementoHandler {

	public boolean renderMementoTimemap(WaybackRequest wbRequest, HttpServletRequest request, HttpServletResponse response) throws WaybackException, IOException;

	public void addTimegateHeaders(HttpServletResponse response, CaptureSearchResults results, WaybackRequest wbr, boolean includeOriginal);
}
