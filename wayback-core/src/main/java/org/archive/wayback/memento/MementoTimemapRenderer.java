package org.archive.wayback.memento;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;

public interface MementoTimemapRenderer {

	public boolean renderMementoTimemap(WaybackRequest wbRequest, HttpServletRequest request, HttpServletResponse response) throws ResourceIndexNotAvailableException, AccessControlException;
}
