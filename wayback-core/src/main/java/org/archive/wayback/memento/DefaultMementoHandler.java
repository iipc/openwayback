package org.archive.wayback.memento;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.WaybackException;

public class DefaultMementoHandler implements MementoHandler {

	@Override
    public boolean renderMementoTimemap(WaybackRequest wbRequest,
            HttpServletRequest request, HttpServletResponse response)
            throws WaybackException, IOException {
		
		SearchResults cResults = wbRequest.getAccessPoint().queryIndex(wbRequest);
		
		MementoUtils.printTimemapResponse((CaptureSearchResults)cResults, wbRequest, response);
		return true;
    }
	
	@Override
	public void addTimegateHeaders(HttpServletResponse response,
			CaptureSearchResults results, WaybackRequest wbRequest, boolean includeOriginal) {
		
		MementoUtils.addTimegateHeaders(response, results, wbRequest, includeOriginal);
	}
}
