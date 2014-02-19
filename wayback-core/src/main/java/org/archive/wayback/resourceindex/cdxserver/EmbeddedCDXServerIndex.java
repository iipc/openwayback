package org.archive.wayback.resourceindex.cdxserver;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.URIException;
import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.writer.HttpCDXWriter;
import org.archive.format.cdx.CDXInputSource;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.MultiCDXInputSource;
import org.archive.format.cdx.StandardCDXLineFactory;
import org.archive.format.gzip.zipnum.ZipNumParams;
import org.archive.url.UrlSurtRangeComputer.MatchType;
import org.archive.util.binsearch.SeekableLineReaderIterator;
import org.archive.util.binsearch.impl.HTTPSeekableLineReader;
import org.archive.util.binsearch.impl.HTTPSeekableLineReaderFactory;
import org.archive.util.binsearch.impl.http.ApacheHttp31SLRFactory;
import org.archive.util.io.RuntimeIOException;
import org.archive.util.iterator.CloseableIterator;
import org.archive.util.iterator.SortedCompositeIterator;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
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
		
	protected UrlCanonicalizer canonicalizer = null;
	protected SelfRedirectFilter selfRedirFilter;
	
	protected String remoteCdxPath;
	
	private HTTPSeekableLineReaderFactory remoteCdxHttp = new ApacheHttp31SLRFactory();
	private StandardCDXLineFactory cdxLineFactory = new StandardCDXLineFactory("cdx11");
	
	private String remoteAuthCookie;
	private String remoteAuthCookieIgnoreRobots;
	
	protected CDXInputSource extraSource;
	
	protected String preferContains;

	protected boolean tryFuzzyMatch = false;

	protected List<String> ignoreRobotPaths;
	
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
	
	protected AuthToken createAuthToken(WaybackRequest wbRequest, String urlkey)
	{
		AuthToken waybackAuthToken = new APContextAuthToken(wbRequest.getAccessPoint());
        waybackAuthToken.setAllCdxFieldsAllow();
        
    	boolean ignoreRobots = wbRequest.isCSSContext() || wbRequest.isIMGContext() || wbRequest.isJSContext();
    	
    	if (ignoreRobots) {
    		waybackAuthToken.setIgnoreRobots(true);
    	}
    	
		if (ignoreRobotPaths != null) {
			for (String path : ignoreRobotPaths) {
				if (urlkey.startsWith(path)) {
		    		waybackAuthToken.setIgnoreRobots(true);
					break;
				}
			}
		}
    	
    	return waybackAuthToken;
	}
	
    public SearchResults doQuery(WaybackRequest wbRequest)
            throws ResourceIndexNotAvailableException,
            ResourceNotInArchiveException, BadQueryException,
            AccessControlException {
			

    	//Compute url key (surt)
		String urlkey = null;
		
		// If no canonicalizer is set, use selfRedirFilter's canonicalizer
		// Either selfRedirFilter or a canonicalizer must be set
		
		UrlCanonicalizer canon = getCanonicalizer();
		
		if (canon == null && selfRedirFilter != null) {
			canon = selfRedirFilter.getCanonicalizer();
		}
		
		if (canon == null) {
			throw new IllegalArgumentException("Unable to find canonicalizer, canonicalizer property or selfRedirFilter property must be set");
		}
		
		try {
			urlkey = canon.urlStringToKey(wbRequest.getRequestUrl());
		} catch (URIException ue) {
			throw new BadQueryException(ue.toString());
		}

		//Do local access/url validation check		
        //AuthToken waybackAuthToken = new AuthToken(wbRequest.get(CDXServer.CDX_AUTH_TOKEN));
        AuthToken waybackAuthToken = createAuthToken(wbRequest, urlkey);
        
        CDXToSearchResultWriter resultWriter = null;
        SearchResults searchResults = null;
        
        if (wbRequest.isReplayRequest() || wbRequest.isCaptureQueryRequest()) {
        	resultWriter = this.getCaptureSearchWriter(wbRequest, waybackAuthToken, false);
        } else if (wbRequest.isUrlQueryRequest()) {
        	resultWriter = this.getUrlSearchWriter(wbRequest);
        } else {
        	throw new BadQueryException("Unknown Query Type");
        }

        try {
        	loadWaybackCdx(urlkey, wbRequest, resultWriter.getQuery(), waybackAuthToken, resultWriter, false);
        	
            if (resultWriter.getErrorMsg() != null) {
            	throw new BadQueryException(resultWriter.getErrorMsg());
            }
        	
            searchResults = resultWriter.getSearchResults();
            
            if ((searchResults.getReturnedCount() == 0) && (wbRequest.isReplayRequest() || wbRequest.isCaptureQueryRequest()) && tryFuzzyMatch) {
            	resultWriter = this.getCaptureSearchWriter(wbRequest, waybackAuthToken, true);
            	
            	if (resultWriter != null) {    	
	            	loadWaybackCdx(urlkey, wbRequest, resultWriter.getQuery(), waybackAuthToken, resultWriter, true);
	            	
	                searchResults = resultWriter.getSearchResults();
            	}
            }
            
            if (searchResults.getReturnedCount() == 0) {
            	throw new ResourceNotInArchiveException(wbRequest.getRequestUrl() + " was not found");
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
        	
        	rte.printStackTrace(); // for now, for better debugging        	
        	throw new ResourceIndexNotAvailableException(rte.toString());
        }
                
		return searchResults;
	}

	protected void loadWaybackCdx(String urlkey, WaybackRequest wbRequest, CDXQuery query, AuthToken waybackAuthToken,
            CDXToSearchResultWriter resultWriter, boolean fuzzy) throws IOException, AccessControlException {
		
    	if ((remoteCdxPath != null) && !wbRequest.isUrlQueryRequest()) {
    		try {
    			wbRequest.setTimestampSearchKey(false); // Not supported for remote requests, caching the entire cdx
    			this.remoteCdxServerQuery(urlkey, resultWriter.getQuery(), waybackAuthToken, resultWriter);
    			return;
    			
    		} catch (IOException io) {
    			//Try again below
    		} catch (RuntimeIOException rte) {
            	Throwable cause = rte.getCause();
            	
            	if (cause instanceof AccessControlException) {
            		throw (AccessControlException)cause;
            	} else {
        			LOGGER.warning(rte.toString());
            	}
    		}
    	}
    	
   		cdxServer.getCdx(resultWriter.getQuery(), waybackAuthToken, resultWriter);
    }

	protected CDXQuery createQuery(WaybackRequest wbRequest, boolean isFuzzy)
	{
		// Create cdx query that is sent to cdx server
		// The query specifies standard cdx server params described at:
		// https://github.com/internetarchive/wayback/tree/master/wayback-cdx-server
		
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
		} else if (wbRequest.isCaptureQueryRequest()) {
			// Add support for range calendar queries:
			// eg: /2005-2007*/
			// by mapping request start and end timestamp
			// to cdx server from= and to= params
			String start = wbRequest.getStartTimestamp();
			if (start != null) {
				query.setFrom(start);
			}
			String end = wbRequest.getEndTimestamp();
			if (end != null) {
				query.setTo(end);
			}
		}
		
		if (timestampDedupLength > 0) {
			//query.setCollapse(new String[]{"timestamp:" + timestampDedupLength});
			query.setCollapseTime(timestampDedupLength);
		}
		
		query.setFilter(new String[]{statusFilter});
		
		return query;
	}
	
	protected void remoteCdxServerQuery(String urlkey, CDXQuery query, AuthToken authToken, CDXToSearchResultWriter resultWriter) throws IOException, AccessControlException
	{
		HTTPSeekableLineReader reader = null;		
		
		// This will throw AccessControlException if blocked
		// (in fact, it throws RuntimeIOException wrapping AccessControlException)
		cdxServer.getAuthChecker().createAccessFilter(authToken).includeUrl(urlkey, query.getUrl());
		
		
		CloseableIterator<String> iter = null;
		
		try {
			
			StringBuilder sb = new StringBuilder(remoteCdxPath);
			
			sb.append("?url=");
			sb.append(URLEncoder.encode(query.getUrl(), "UTF-8"));
			//sb.append(query.getUrl());
//			sb.append("&limit=");
//			sb.append(query.getLimit());
			sb.append("&filter=");
			sb.append(URLEncoder.encode(query.getFilter()[0], "UTF-8"));
			
//			if (!query.getClosest().isEmpty()) {
//				sb.append("&closest=");
//				sb.append(query.getClosest().substring(0, 4));
//			}
			
			if (query.getCollapseTime() > 0) {
				sb.append("&collapseTime=");
				sb.append(query.getCollapseTime());
			}
			
			sb.append("&gzip=true");
			
			String finalUrl = sb.toString();
			
			reader = this.remoteCdxHttp.get(finalUrl);
			
			if (remoteAuthCookie != null) {
				
				String cookie;
				
				if (authToken.isIgnoreRobots() && (remoteAuthCookieIgnoreRobots != null)) {
					cookie = remoteAuthCookieIgnoreRobots;
				} else {
					cookie = remoteAuthCookie;
				}
				
				reader.setCookie(CDXServer.CDX_AUTH_TOKEN + "=" + cookie);
			}
			
			reader.setSaveErrHeader(HttpCDXWriter.RUNTIME_ERROR_HEADER);
			
			reader.seekWithMaxRead(0, true, -1);
			
			iter = createRemoteIter(urlkey, reader);
								
			resultWriter.begin();
			
			while (iter.hasNext() && !resultWriter.isAborted()) {
				String rawLine = iter.next();
				CDXLine line = cdxLineFactory.createStandardCDXLine(rawLine, StandardCDXLineFactory.cdx11);
				resultWriter.writeLine(line);
			}
			
			resultWriter.end();
			iter.close();

		} finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	protected CloseableIterator<String> createRemoteIter(
			String urlkey,
            HTTPSeekableLineReader reader) throws IOException {
		
		CloseableIterator<String> iter = new SeekableLineReaderIterator(reader);
		
		String cacheInfo = reader.getHeaderValue("X-Page-Cache");
		
		if ((cacheInfo != null) && cacheInfo.equals("HIT")) {
			if (LOGGER.isLoggable(Level.FINE)) {
				LOGGER.fine("CACHED");
			}
		}
			
		if (extraSource != null) {
			ZipNumParams params = new ZipNumParams();
			CloseableIterator<String> extraIter = extraSource.getCDXIterator(urlkey, urlkey, urlkey, params);
			
			if (extraIter.hasNext()) {
				SortedCompositeIterator<String> sortedIter = new SortedCompositeIterator<String>(MultiCDXInputSource.defaultComparator);
				sortedIter.addIterator(iter);
				sortedIter.addIterator(extraIter);
				return sortedIter;
			}
		}
		
		return iter;
    }

	protected CDXToCaptureSearchResultsWriter getCaptureSearchWriter(WaybackRequest wbRequest, AuthToken waybackAuthToken, boolean isFuzzy)
	{
		final CDXQuery query = createQuery(wbRequest, isFuzzy);
		
		if (isFuzzy && query == null) {
			return null;
		}
		
		boolean resolveRevisits = wbRequest.isReplayRequest();

		// For now, not using seek single capture to allow for run time checking of additional records  
		//boolean seekSingleCapture = resolveRevisits && wbRequest.isTimestampSearchKey();
		boolean seekSingleCapture = false;
		//boolean seekSingleCapture = resolveRevisits && (wbRequest.isTimestampSearchKey() || (wbRequest.isBestLatestReplayRequest() && !wbRequest.hasMementoAcceptDatetime()));
		
		CDXToCaptureSearchResultsWriter captureWriter = new CDXToCaptureSearchResultsWriter(query, resolveRevisits, seekSingleCapture, preferContains);
						
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

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
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
	
	public String getRemoteAuthCookieIgnoreRobots() {
		return remoteAuthCookieIgnoreRobots;
	}

	public void setRemoteAuthCookieIgnoreRobots(String remoteAuthCookieIgnoreRobots) {
		this.remoteAuthCookieIgnoreRobots = remoteAuthCookieIgnoreRobots;
	}

	public HTTPSeekableLineReaderFactory getRemoteCdxHttp() {
		return remoteCdxHttp;
	}

	public void setRemoteCdxHttp(HTTPSeekableLineReaderFactory remoteCdxHttp) {
		this.remoteCdxHttp = remoteCdxHttp;
	}

	public CDXInputSource getExtraSource() {
		return extraSource;
	}

	public void setExtraSource(CDXInputSource extraSource) {
		this.extraSource = extraSource;
	}

	public String getPreferContains() {
		return preferContains;
	}

	public void setPreferContains(String preferContains) {
		this.preferContains = preferContains;
	}

	public List<String> getIgnoreRobotPaths() {
		return ignoreRobotPaths;
	}

	public void setIgnoreRobotPaths(List<String> ignoreRobotPaths) {
		this.ignoreRobotPaths = ignoreRobotPaths;
	}

	public boolean isTryFuzzyMatch() {
		return tryFuzzyMatch;
	}

	public void setTryFuzzyMatch(boolean tryFuzzyMatch) {
		this.tryFuzzyMatch = tryFuzzyMatch;
	}
}
