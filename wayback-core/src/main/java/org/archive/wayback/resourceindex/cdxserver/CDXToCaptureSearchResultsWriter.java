package org.archive.wayback.resourceindex.cdxserver;

import org.apache.commons.lang.math.NumberUtils;
import org.archive.cdxserver.CDXQuery;
import org.archive.cdxserver.processor.RevisitResolver;
import org.archive.cdxserver.writer.CDXWriter;
import org.archive.format.cdx.CDXLine;
import org.archive.format.cdx.FieldSplitFormat;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.Timestamp;

public class CDXToCaptureSearchResultsWriter extends CDXWriter {
	
	protected final CaptureSearchResults results = new CaptureSearchResults();
	protected String msg;
	
	protected String targetTimestamp;
	protected int flip = 1;
	protected CaptureSearchResult closest = null;
	protected SelfRedirectFilter selfRedirFilter = null;
	
	protected CDXQuery query;
	
	public CDXToCaptureSearchResultsWriter(CDXQuery query)
	{
		this.query = query;
	}
	
	public void setTargetTimestamp(String timestamp, boolean reverse)
	{
		// If closest query, first result is always closest
		if (!query.getClosest().isEmpty()) {
			return;
		}
		
		targetTimestamp = timestamp;
		if (reverse) {
			flip = -1;
		}
	}

	@Override
    public void begin() {
	    // TODO Auto-generated method stub
    }

	@Override
    public void trackLine(CDXLine line) {
	    // TODO Auto-generated method stub
    }
	
	public void printError(String msg)
	{
		this.msg = msg;
	}
	
	public String getErrorMsg()
	{
		return msg;
	}

	@Override
    public int writeLine(CDXLine line) {
		FastCaptureSearchResult result = new FastCaptureSearchResult();
		result.setUrlKey(line.getUrlKey());
		result.setCaptureTimestamp(line.getTimestamp());
		result.setOriginalUrl(line.getOriginalUrl());
		
		result.setRedirectUrl(line.getRedirect());
		result.setHttpCode(line.getStatusCode());
		
		if (selfRedirFilter != null && !result.getRedirectUrl().equals(CDXLine.EMPTY_VALUE)) {
			if (selfRedirFilter.filterObject(result) != ObjectFilter.FILTER_INCLUDE) {
				return 0;
			}
		}
		
		result.setMimeType(line.getMimeType());
		result.setDigest(line.getDigest());
		result.setOffset(NumberUtils.toLong(line.getOffset(), -1));
		result.setCompressedLength(NumberUtils.toLong(line.getLength(), -1));
		result.setFile(line.getFilename());
		result.setRobotFlags(line.getRobotFlags());
		
		String payloadFile = line.getField(RevisitResolver.origfilename, null);
		
		if (payloadFile != null) {
			FastCaptureSearchResult payload = new FastCaptureSearchResult();
			payload.setFile(payloadFile);
			payload.setOffset(NumberUtils.toLong(line.getField(RevisitResolver.origoffset), -1));
			payload.setCompressedLength(NumberUtils.toLong(line.getField(RevisitResolver.origlength), -1));
			result.flagDuplicateDigest(payload);
		}
		
		if ((targetTimestamp != null) && (closest == null)) {
			closest = determineClosest(result);
		}
		
		results.addSearchResult(result, (flip == 1));
		return 1;
    }
	
	protected CaptureSearchResult determineClosest(CaptureSearchResult nextResult)
	{		
		int compare = targetTimestamp.compareTo(nextResult.getCaptureTimestamp()) * flip;
		
		if (compare == 0) {
			return nextResult;
		} else if (compare > 0) {
			// Too early to tell
			return null;
		}
		
		// First result that is greater/less than target
		if (results.isEmpty()) {
			return nextResult;
		}
		
		CaptureSearchResult lastResult = results.getResults().getLast();
		
		// Now compare date diff
		long nextTime = nextResult.getCaptureDate().getTime();
		long lastTime = lastResult.getCaptureDate().getTime();
		
		long targetTime = Timestamp.parseAfter(targetTimestamp).getDate().getTime();
		
		if (Math.abs(nextTime - targetTime) < Math.abs(lastTime - targetTime)) {
			return nextResult;
		} else {
			return lastResult;
		}
	}

	@Override
    public void writeResumeKey(String resumeKey) {
	    // TODO Auto-generated method stub
    }

    public void end() {
		results.setClosest(this.getClosest());
		results.setReturnedCount(results.getResults().size());
		results.setMatchingCount(results.getResults().size());
    }
    
    public CaptureSearchResult getClosest()
    {
    	if (closest != null) {
    		return closest;
    	}
    	
    	// If closest query, first result is closest
    	if (!query.getClosest().isEmpty()) {
    		return results.getResults().getFirst();
    	}
    	
    	if (!results.isEmpty()) {
    		return results.getResults().getLast();
    	}
    	
    	return null;
    }
    
    public CaptureSearchResults getCaptureSearchResults()
    {
    	return results;
    }

	@Override
    public FieldSplitFormat modifyOutputFormat(FieldSplitFormat format) {
	    return format;
    }

	public SelfRedirectFilter getSelfRedirFilter() {
		return selfRedirFilter;
	}

	public void setSelfRedirFilter(SelfRedirectFilter selfRedirFilter) {
		this.selfRedirFilter = selfRedirFilter;
	}
}
