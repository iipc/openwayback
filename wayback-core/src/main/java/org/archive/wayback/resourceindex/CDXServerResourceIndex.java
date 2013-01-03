package org.archive.wayback.resourceindex;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.archive.util.zip.OpenJDK7GZIPInputStream;
import org.archive.wayback.ResourceIndex;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.SearchResults;
import org.archive.wayback.core.UrlSearchResult;
import org.archive.wayback.core.UrlSearchResults;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.exception.AccessControlException;
import org.archive.wayback.exception.BadQueryException;
import org.archive.wayback.exception.ResourceIndexNotAvailableException;
import org.archive.wayback.exception.ResourceNotInArchiveException;
import org.archive.wayback.resourceindex.cdx.format.CDXFlexFormat;
import org.archive.wayback.resourceindex.filterfactory.ExclusionCaptureFilterGroup;
import org.archive.wayback.resourceindex.filters.ClosestResultTrackingFilter;
import org.archive.wayback.resourceindex.filters.WARCRevisitAnnotationFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.webapp.CustomResultFilterFactory;

public class CDXServerResourceIndex implements ResourceIndex {
	
	protected String cdxServerPath;
	protected String captureQuery = "";
	protected String urlQuery = "";
	
	public void init()
	{
		
	}
	
	protected String getQueryString(WaybackRequest request) throws UnsupportedEncodingException
	{
		StringBuilder builder = new StringBuilder();
		builder.append("url=");
		builder.append(URLEncoder.encode(request.getRequestUrl(), "UTF-8"));
		builder.append("&");
		
		if (request.isUrlQueryRequest()) {
			builder.append(urlQuery);
		} else {
			builder.append(captureQuery);
		}
		
		return builder.toString();
	}

	@Override
	public SearchResults query(WaybackRequest request)
	throws ResourceIndexNotAvailableException,
	ResourceNotInArchiveException, BadQueryException,
	AccessControlException {
		
		CaptureSearchResults captureResults = null;
		UrlSearchResults urlResults = null;
		
		SearchResults results = null;
				
		if (request.isUrlQueryRequest()) {
			urlResults = new UrlSearchResults();
			results = urlResults;
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					WaybackRequest.REQUEST_URL_QUERY);
		} else {
			captureResults = new CaptureSearchResults();
			results = captureResults;
			
			results.putFilter(WaybackRequest.REQUEST_TYPE, 
					(request.isCaptureQueryRequest() ? WaybackRequest.REQUEST_CAPTURE_QUERY : WaybackRequest.REQUEST_REPLAY_QUERY));
			
		}
		
		BufferedReader reader = null;
		
		try {
			String url = cdxServerPath + "?" + getQueryString(request);
			URLConnection connection = new URL(url).openConnection();
			
			InputStream input = new OpenJDK7GZIPInputStream(connection.getInputStream());
			reader = new BufferedReader(new InputStreamReader(input));
			
			
			// Closest Tracking Filter
			ClosestResultTrackingFilter closestTracker = new ClosestResultTrackingFilter(request.getReplayDate().getTime());
			
			ExclusionCaptureFilterGroup exclusionGroup = new ExclusionCaptureFilterGroup(request, request.getAccessPoint().getSelfRedirectCanonicalizer());
			
			// Exclusions
			ObjectFilter<CaptureSearchResult> exclusionFilter = null;
					
			if (!exclusionGroup.getFilters().isEmpty()) {
				exclusionFilter = exclusionGroup.getFilters().get(0);
			}
			
			// WARC revisit
			WARCRevisitAnnotationFilter revisiter = new WARCRevisitAnnotationFilter();
			
			// Custom Filter Factory
			CustomResultFilterFactory customFilterFactory = request.getAccessPoint().getFilterFactory();
			ObjectFilter<CaptureSearchResult> customFilter = null;
			if (customFilterFactory != null) {
				customFilter = customFilterFactory.get(request.getAccessPoint());
			}
			
			String line;
			
			int count = 0;
			int numPassed = 0;
			
			while ((line = reader.readLine()) != null) {
				CaptureSearchResult result = CDXFlexFormat.parseCDXLineFlex(line);
				
				++count;
				
				String summaryLine = null;
				
				if (request.isUrlQueryRequest()) {
					summaryLine = reader.readLine();
				}
				
				if ((exclusionFilter != null) && (exclusionFilter.filterObject(result) != ObjectFilter.FILTER_INCLUDE)) {
					continue;
				}
				
				if ((customFilter != null) && (customFilter.filterObject(result) != ObjectFilter.FILTER_INCLUDE)) {
					continue;
				}
				
				++numPassed;
				
				revisiter.filterObject(result);
				
				if (request.isUrlQueryRequest()) {			
					UrlSearchResult urlResult = new UrlSearchResult();
					urlResult.setUrlKey(result.getUrlKey());
					urlResult.setOriginalUrl(result.getOriginalUrl());
					urlResult.setFirstCapture(result.getCaptureTimestamp());
					urlResults.addSearchResult(urlResult);
					
					if (summaryLine == null) {
						continue;
					}
					
					String[] summary = summaryLine.split(" ");
					
					if (summary.length > 0) {
						urlResult.setLastCapture(summary[0]);
					}
					if (summary.length > 1) {
						urlResult.setNumCaptures(summary[1]);
					}
					if (summary.length > 2) {
						urlResult.setNumVersions(summary[2]);
					}
				} else {
					closestTracker.filterObject(result);
					
					captureResults.addSearchResult(result);
				}
			}
						
			// Throw exclusion related exception if needed here
			exclusionGroup.annotateResults(results);
			
			if (numPassed == 0) {
				throw new ResourceNotInArchiveException("the URL " + request.getRequestUrl() + " is not in the archive.");
			}
			
			if (captureResults != null) {						
				captureResults.setClosest(closestTracker.getClosest());
			}
			
			results.setMatchingCount(count);
			results.setReturnedCount(numPassed);
			
			return results;
		} catch (EOFException eof) {
			return results;
		} catch (IOException e) {
			throw new ResourceIndexNotAvailableException(e.toString());
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					throw new ResourceIndexNotAvailableException(e.toString());
				}
			}			
		}
	}
	
	@Override
	public void shutdown() throws IOException {
		// TODO Auto-generated method stub

	}

	public String getCdxServerPath() {
		return cdxServerPath;
	}

	public void setCdxServerPath(String cdxServerPath) {
		this.cdxServerPath = cdxServerPath;
	}

	public String getCaptureQuery() {
		return captureQuery;
	}

	public void setCaptureQuery(String captureQuery) {
		this.captureQuery = captureQuery;
	}

	public String getUrlQuery() {
		return urlQuery;
	}

	public void setUrlQuery(String urlQuery) {
		this.urlQuery = urlQuery;
	}

}
