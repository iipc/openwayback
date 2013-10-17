package org.archive.wayback.resourceindex.cdxserver;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.util.URIUtil;
import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.writer.HttpCDXWriter;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.StandardCDXLineFactory;
import org.archive.url.UrlSurtRangeComputer.MatchType;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader.BadHttpStatusException;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.http.ApacheHttp31SLRFactory;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.AdministrativeAccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.exception.WaybackException;
import org.archive.wayback.memento.MementoConstants;
import org.archive.wayback.memento.MementoHandler;
import org.archive.wayback.memento.MementoUtils;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.util.webapp.AbstractRequestHandler;
import org.archive.wayback.webapp.PerfStats;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.ServletRequestBindingException;

public class EmbeddedCDXServerIndex extends AbstractRequestHandler implements MementoHandler, ResourceIndex {

	private static final Logger LOGGER = Logger.getLogger(
			EmbeddedCDXServerIndex.class.getName());
	
	protected CDXServer cdxServer;
	protected int timestampDedupLength = 0;
	protected int limit = 0;
		
	private SelfRedirectFilter selfRedirFilter;
	
	protected String remoteCdxPath;
	
	private HTTPSeekableLineReaderFactory remoteCdxHttp = new ApacheHttp31SLRFactory();
	private StandardCDXLineFactory cdxLineFactory = new StandardCDXLineFactory("cdx11");
	private String remoteAuthCookie;
	
	enum PerfStat
	{
		IndexLoad;
	}
	
//	public void init()
//	{
//		initAuthCookie();
//	}
//	
//	protected String initAuthCookie()
//	{
//		if (cdxServer == null) {
//			return;
//		}
//		
//		AuthChecker check = cdxServer.getAuthChecker();
//		if (!(check instanceof PrivTokenAuthChecker)) {
//			return;
//		}
//		
//		List<String> list = ((PrivTokenAuthChecker)check).getAllCdxFieldsAccessTokens();
//		
//		if (list == null || list.isEmpty()) {
//			return;
//		}
//		
//		return CDXServer.CDX_AUTH_TOKEN + ": " + list.get(0);
//	}
	
	@Override
    public SearchResults query(WaybackRequest wbRequest)
            throws ResourceIndexNotAvailableException,
            ResourceNotInArchiveException, BadQueryException,
            AccessControlException {
		try {
			PerfStats.timeStart(PerfStat.IndexLoad);
			return doQuery(wbRequest);
		} finally {
			PerfStats.timeEnd(PerfStat.IndexLoad);
		}
	}
	
    public SearchResults doQuery(WaybackRequest wbRequest)
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
        	throw new BadQueryException("Unknown Query Type");
        }

        try {
        	if (remoteCdxPath != null) {
        		this.remoteCdxServerQuery(resultWriter.getQuery(), waybackAuthToken, resultWriter);
        	} else {
        		cdxServer.getCdx(resultWriter.getQuery(), waybackAuthToken, resultWriter);
        	}
	        
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
        	
        	throw new ResourceIndexNotAvailableException(rte.getMessage());
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
				
		query.setLimit(limit);
		//query.setSort(CDXQuery.SortType.reverse);
		
		String statusFilter = "!statuscode:(500|502|504)";
		
		if (wbRequest.isReplayRequest()) {
			if (wbRequest.isBestLatestReplayRequest()) {
				statusFilter = "statuscode:[23]..";
			}
			
			if (wbRequest.isTimestampSearchKey()) {		
				query.setClosest(wbRequest.getReplayTimestamp());
			}
		} else {
			if (timestampDedupLength > 0) {
				//query.setCollapse(new String[]{"timestamp:" + timestampDedupLength});
				query.setCollapseTime(timestampDedupLength);
			}
		}
		
		query.setFilter(new String[]{statusFilter});
		
		return query;
	}
	
	protected void remoteCdxServerQuery(CDXQuery query, AuthToken authToken, CDXToSearchResultWriter resultWriter) throws IOException, AccessControlException
	{
		HTTPSeekableLineReader reader = null;
		
		//Do local access/url validation check
		String urlkey = selfRedirFilter.getCanonicalizer().urlStringToKey(query.getUrl());
		
		cdxServer.getAuthChecker().createAccessFilter(authToken).includeUrl(urlkey, query.getUrl());
		
		try {
			
			StringBuilder sb = new StringBuilder(remoteCdxPath);
			
			sb.append("?url=");
			//sb.append(URLEncoder.encode(query.getUrl(), "UTF-8"));
			sb.append(query.getUrl());
			sb.append("&limit=");
			sb.append(query.getLimit());
			sb.append("&filter=");
			sb.append(query.getFilter()[0]);
			
			if (!query.getClosest().isEmpty()) {
				sb.append("&closest=");
				sb.append(query.getClosest());
			}
			
			if (query.getCollapseTime() > 0) {
				sb.append("&collapseTime=");
				sb.append(query.getCollapseTime());
			}
			
			sb.append("&gzip=true");
			
			String finalUrl = URIUtil.encodePathQuery(sb.toString(), "UTF-8");
			
			reader = this.remoteCdxHttp.get(finalUrl);
			
			if (remoteAuthCookie != null) {
				reader.setCookie(CDXServer.CDX_AUTH_TOKEN + "=" + remoteAuthCookie);
			}
			
			reader.setSaveErrHeader(HttpCDXWriter.RUNTIME_ERROR_HEADER);
			
			reader.seekWithMaxRead(0, true, -1);
			
			if (LOGGER.isLoggable(Level.FINE)) {
				String cacheInfo = reader.getHeaderValue("X-Page-Cache");
				if (cacheInfo != null && cacheInfo.equals("HIT")) {
					LOGGER.fine("CACHED");
				}
			}
			
			String rawLine = null;
			
			resultWriter.begin();
			
			while ((rawLine = reader.readLine()) != null && !resultWriter.isAborted()) {
				CDXLine line = cdxLineFactory.createStandardCDXLine(rawLine, StandardCDXLineFactory.cdx11);
				resultWriter.writeLine(line);
			}
			
			resultWriter.end();
		} catch (BadHttpStatusException badStatus) {
			if (reader != null) {
				String header = reader.getErrHeader();
				
				if (header != null) {
					if (header.contains("Robot")) {
						throw new RobotAccessControlException(query.getUrl() + " is blocked by the sites robots.txt file");
					} else if (header.contains("AdministrativeAccess")) {
						throw new AdministrativeAccessControlException(query.getUrl() + " is not available in the Wayback Machine.");
					}
				}
			}
			
			throw badStatus;
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	protected CDXToSearchResultWriter getCaptureSearchWriter(WaybackRequest wbRequest)
	{
		final CDXQuery query = createQuery(wbRequest);
		
		boolean resolveRevisits = wbRequest.isReplayRequest();

		// For now, not using seek single capture to allow for run time checking of additional records  
		boolean seekSingleCapture = resolveRevisits && wbRequest.isTimestampSearchKey();
		//boolean seekSingleCapture = resolveRevisits && (wbRequest.isTimestampSearchKey() || (wbRequest.isBestLatestReplayRequest() && !wbRequest.hasMementoAcceptDatetime()));
		
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
            HttpServletRequest request, HttpServletResponse response) throws WaybackException, IOException {
		
		try {
			PerfStats.timeStart(PerfStat.IndexLoad);
			
			String format = wbRequest.getMementoTimemapFormat();
			
			if ((format != null) && format.equals(MementoConstants.FORMAT_LINK)) {
				SearchResults cResults = wbRequest.getAccessPoint().queryIndex(wbRequest);
				MementoUtils.printTimemapResponse((CaptureSearchResults)cResults, wbRequest, response);				
				return true;
			}
			
			CDXQuery query = new CDXQuery(wbRequest.getRequestUrl());
			
			query.setOutput(wbRequest.getMementoTimemapFormat());
			
			String from = wbRequest.get(MementoConstants.PAGE_STARTS);
			
			if (from != null) {
				query.setFrom(from);
			}
			
			try {
		        query.fill(request);
	        } catch (ServletRequestBindingException e1) {
	        	//Ignore
	        }
	
    		cdxServer.getCdx(request, response, query);
    		
		} finally {
			PerfStats.timeEnd(PerfStat.IndexLoad);
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
	
	@Override
    public void addTimegateHeaders(
    		HttpServletResponse response,
            CaptureSearchResults results,
            WaybackRequest wbRequest,
            
            boolean includeOriginal) {

		MementoUtils.addTimegateHeaders(response, results, wbRequest, includeOriginal);
			
		// Add custom JSON header
		CaptureSearchResult result = results.getClosest();
		
		JSONObject obj = new JSONObject();
		
		JSONObject closestSnapshot = new JSONObject();
		
		try {
			obj.put("wb_url", MementoUtils.getMementoPrefix(wbRequest.getAccessPoint()) + wbRequest.getAccessPoint().getUriConverter().makeReplayURI(result.getCaptureTimestamp(), wbRequest.getRequestUrl()));
			obj.put("timestamp", result.getCaptureTimestamp());
			obj.put("status", result.getHttpCode());
			closestSnapshot.put("closest", obj);
		} catch (JSONException je) {
			
		}
		String json = closestSnapshot.toString();
		json = json.replace("\\/", "/");
		response.setHeader("X-Link-JSON", json);
    }

	public String getRemoteCdxPath() {
		return remoteCdxPath;
	}

	public void setRemoteCdxPath(String remoteCdxPath) {
		this.remoteCdxPath = remoteCdxPath;
	}

	public String getRemoteAuthCookie() {
		return remoteAuthCookie;
	}

	public void setRemoteAuthCookie(String remoteAuthCookie) {
		this.remoteAuthCookie = remoteAuthCookie;
	}
	
	public HTTPSeekableLineReaderFactory getRemoteCdxHttp() {
		return remoteCdxHttp;
	}

	public void setRemoteCdxHttp(HTTPSeekableLineReaderFactory remoteCdxHttp) {
		this.remoteCdxHttp = remoteCdxHttp;
	}
}
