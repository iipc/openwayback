package org.archive.wayback.resourceindex.cdxserver;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.memento.MementoTimemapRenderer;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.springframework.web.bind.ServletRequestBindingException;

public class EmbeddedCDXServerIndex implements MementoTimemapRenderer, ResourceIndex {

	protected CDXServer cdxServer;
	protected int timestampDedupLength = 0;
		
	private SelfRedirectFilter selfRedirFilter;
	
	@Override
    public SearchResults query(WaybackRequest wbRequest)
            throws ResourceIndexNotAvailableException,
            ResourceNotInArchiveException, BadQueryException,
            AccessControlException {
		
		if (wbRequest.isCaptureQueryRequest() || wbRequest.isReplayRequest()) {
			return doCaptureQuery(wbRequest);
		}
		
		return null;
    }
	
	protected CDXQuery createQuery(WaybackRequest wbRequest)
	{
		CDXQuery query = new CDXQuery(wbRequest.getRequestUrl());
		
		if (timestampDedupLength > 0) {
			query.setCollapse(new String[]{"timestamp:" + timestampDedupLength});
		}
		
		String statusFilter;
		
		if (wbRequest.isBestLatestReplayRequest()) {
			statusFilter = "statuscode:[23]..";
			query.setReverse(true);
			query.setLimit(1);
		} else {
			//if (wbRequest.isReplayRequest()) {
			//	query.setClosest(wbRequest.getReplayTimestamp());
			//}
			
			statusFilter = "!statuscode:(500|502|504)";
			
			if (wbRequest.isTimestampSearchKey()) {
				query.setFastLatest(true);
			}
		}
		
		//String filenameFilter = "!filename:(-EXTRACTION-|-HISTORICAL-)";
		String[] filters = new String[]{statusFilter};
		
		query.setFilter(filters);
		
		return query;
	}

	private SearchResults doCaptureQuery(WaybackRequest wbRequest) 
			throws ResourceNotInArchiveException, BadQueryException, ResourceIndexNotAvailableException, AccessControlException {
		
		final CDXQuery query = createQuery(wbRequest);
		
		CDXToCaptureSearchResultsWriter captureWriter = new CDXToCaptureSearchResultsWriter(query);
				
        captureWriter.setTargetTimestamp(wbRequest.getReplayTimestamp(), query.isReverse());
        
        captureWriter.setSelfRedirFilter(selfRedirFilter);
                
        //AuthToken waybackAuthToken = new AuthToken(wbRequest.get(CDXServer.CDX_AUTH_TOKEN));
        AuthToken waybackAuthToken = new AuthToken();
        waybackAuthToken.setAllCdxFieldsAllow();

        try {    	
	        cdxServer.getCdx(query, waybackAuthToken, captureWriter);
	        
        } catch (IOException e) {
        	throw new ResourceIndexNotAvailableException(e.toString());
        } catch (RuntimeException rte) {
        	Throwable cause = rte.getCause();
        	
        	if (cause instanceof AccessControlException) {
        		throw (AccessControlException)cause;
        	}
        	
        	if (cause instanceof IOException) {
        		throw new ResourceIndexNotAvailableException(cause.toString());
        	}
        	
        	throw rte;
        }
        
        if (captureWriter.getErrorMsg() != null) {
        	throw new BadQueryException(captureWriter.getErrorMsg());
        }
        
        CaptureSearchResults captureResults = captureWriter.getCaptureSearchResults();
        
        if (captureResults.isEmpty()) {
        	throw new ResourceNotInArchiveException(wbRequest.getRequestUrl() + " was not found");
        }
        
		return captureResults;
    }

	@Override
    public boolean renderMementoTimemap(WaybackRequest wbRequest,
            HttpServletRequest request, HttpServletResponse response) throws ResourceIndexNotAvailableException, AccessControlException {
		
		CDXQuery query = new CDXQuery(wbRequest.getRequestUrl());
		
		try {
	        query.fill(request);
        } catch (ServletRequestBindingException e1) {

        }
		
		//query.setOutput(wbRequest.getMementoTimemapFormat());

        try {    	
    		cdxServer.getCdx(request, response, query);
        } catch (Exception e) {
        	//CDX server handles its own output
        }

		return true;
    }
	
	@Override
    public void shutdown() throws IOException {
	    // TODO Auto-generated method stub
    }

	public CDXServer getCdxServer() {
		return cdxServer;
	}

	public void setCdxServer(CDXServer cdxServer) {
		this.cdxServer = cdxServer;
	}

	public int getTimestampDedupLength() {
		return timestampDedupLength;
	}

	public void setTimestampDedupLength(int timestampDedupLength) {
		this.timestampDedupLength = timestampDedupLength;
	}

	public SelfRedirectFilter getSelfRedirFilter() {
		return selfRedirFilter;
	}

	public void setSelfRedirFilter(SelfRedirectFilter selfRedirFilter) {
		this.selfRedirFilter = selfRedirFilter;
	}
}
