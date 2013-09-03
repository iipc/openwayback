package org.archive.wayback.resourceindex.cdxserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.format.cdx.CDXLine;
import org.archive.url.UrlSurtRangeComputer.MatchType;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.memento.MementoConstants;
import org.archive.wayback.memento.MementoTimemapRenderer;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.util.webapp.AbstractRequestHandler;
import org.springframework.web.bind.ServletRequestBindingException;

public class EmbeddedCDXServerIndex extends AbstractRequestHandler implements MementoTimemapRenderer, ResourceIndex {

	protected CDXServer cdxServer;
	protected int timestampDedupLength = 0;
	protected int limit = 0;
		
	private SelfRedirectFilter selfRedirFilter;
	
	@Override
    public SearchResults query(WaybackRequest wbRequest)
            throws ResourceIndexNotAvailableException,
            ResourceNotInArchiveException, BadQueryException,
            AccessControlException {
			
	                
        //AuthToken waybackAuthToken = new AuthToken(wbRequest.get(CDXServer.CDX_AUTH_TOKEN));
        AuthToken waybackAuthToken = new AuthToken();
        waybackAuthToken.setAllCdxFieldsAllow();
        
        CDXToSearchResultWriter resultWriter = null;
        
        if (wbRequest.isReplayRequest() || wbRequest.isCaptureQueryRequest()) {
        	resultWriter = this.getCaptureSearchWriter(wbRequest);
        } else if (wbRequest.isUrlQueryRequest()) {
        	resultWriter = this.getUrlSearchWriter(wbRequest);
        } else {
        	return null;
        }

        try {    	
	        cdxServer.getCdx(resultWriter.getQuery(), waybackAuthToken, resultWriter);
	        
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
        
        if (resultWriter.getErrorMsg() != null) {
        	throw new BadQueryException(resultWriter.getErrorMsg());
        }
        
        SearchResults searchResults = resultWriter.getSearchResults();
        
        if (searchResults.getReturnedCount() == 0) {
        	throw new ResourceNotInArchiveException(wbRequest.getRequestUrl() + " was not found");
        }
        
		return searchResults;
	}

	protected CDXQuery createQuery(WaybackRequest wbRequest)
	{
		CDXQuery query = new CDXQuery(wbRequest.getRequestUrl());
		
		if (timestampDedupLength > 0) {
			//query.setCollapse(new String[]{"timestamp:" + timestampDedupLength});
			query.setCollapseTime(timestampDedupLength);
		}
		
		if (wbRequest.isBestLatestReplayRequest()) {
			String statusFilter = "statuscode:[23]..";
			query.setFilter(new String[]{statusFilter});
			query.setSort(CDXQuery.SortType.reverse);
			query.setLimit(1);
			return query;
		}
		
		query.setLimit(limit);
		
		//query.setFilter(new String[]{"!statuscode:(500|502|504)"});
		query.setFilter(new String[]{"statuscode:^[23-]"});
		
		if (wbRequest.isReplayRequest()) {
			//query.setResolveRevisits(true);		
			
			if (wbRequest.isTimestampSearchKey()) {
				query.setClosest(wbRequest.getReplayTimestamp());
				query.setSort(CDXQuery.SortType.reverse);
			}
		}
		
		return query;
	}
	
	protected CDXToSearchResultWriter getCaptureSearchWriter(WaybackRequest wbRequest)
	{
		final CDXQuery query = createQuery(wbRequest);
		
		boolean seekSingleCapture = (wbRequest.isReplayRequest() && wbRequest.isTimestampSearchKey());
		boolean resolveRevisits = wbRequest.isReplayRequest();
		
		CDXToCaptureSearchResultsWriter captureWriter = new CDXToCaptureSearchResultsWriter(query, resolveRevisits, seekSingleCapture);
				
        captureWriter.setTargetTimestamp(wbRequest.getReplayTimestamp());
        
        captureWriter.setSelfRedirFilter(selfRedirFilter);
        
        return captureWriter;
	}
	
	protected CDXToSearchResultWriter getUrlSearchWriter(WaybackRequest wbRequest)
	{	
		final CDXQuery query = new CDXQuery(wbRequest.getRequestUrl());
		
		query.setCollapse(new String[]{CDXLine.urlkey});
		query.setMatchType(MatchType.prefix);
		query.setShowGroupCount(true);
		query.setShowUniqCount(true);
		query.setLastSkipTimestamp(true);
		query.setFl("urlkey,original,timestamp,endtimestamp,groupcount,uniqcount");
		
		return new CDXToUrlSearchResultWriter(query);
    }

	@Override
    public boolean renderMementoTimemap(WaybackRequest wbRequest,
            HttpServletRequest request, HttpServletResponse response) throws ResourceIndexNotAvailableException, AccessControlException {
		
		String format = wbRequest.getMementoTimemapFormat();
		
		if ((format != null) && format.equals(MementoConstants.FORMAT_LINK)) {
			return false;
		}
		
		CDXQuery query = new CDXQuery();
		
		query.setOutput(wbRequest.getMementoTimemapFormat());
		
		try {
	        query.fill(request);
        } catch (ServletRequestBindingException e1) {
        	//Ignore
        }

        try {    	
    		cdxServer.getCdx(request, response, query);
        } catch (Exception e) {
        	//CDX server handles its own output
        }

		return true;
    }
	
	@Override
    public boolean handleRequest(HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) throws ServletException,
            IOException {
		
		CDXQuery query = new CDXQuery(httpRequest);		
		cdxServer.getCdx(httpRequest, httpResponse, query);
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

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}
}
