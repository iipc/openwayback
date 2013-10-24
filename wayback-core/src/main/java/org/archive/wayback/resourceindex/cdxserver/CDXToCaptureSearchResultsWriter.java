package org.archive.wayback.resourceindex.cdxserver;

import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang.math.NumberUtils;
import org.archive.cdxserver.CDXQuery;
import org.archive.format.cdx.CDXLine;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.CaptureSearchResults;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.resourceindex.filters.SelfRedirectFilter;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.Timestamp;
import org.archive.wayback.util.url.UrlOperations;

public class CDXToCaptureSearchResultsWriter extends CDXToSearchResultWriter {
	
	public final static String REVISIT_VALUE = "warc/revisit";
	
	protected CaptureSearchResults results = null;
	
	protected String targetTimestamp;
	protected int flip = 1;
	protected boolean done = false;
	protected CaptureSearchResult closest = null;
	protected SelfRedirectFilter selfRedirFilter = null;
	
	protected CaptureSearchResult lastResult = null;
	
	protected HashMap<String, CaptureSearchResult> digestToOriginal;
	protected HashMap<String, LinkedList<CaptureSearchResult>> digestToRevisits;
	
	protected boolean resolveRevisits = false;
	protected boolean seekSingleCapture = false;
	protected boolean isReverse = false;
	
	public CDXToCaptureSearchResultsWriter(CDXQuery query,
										   boolean resolveRevisits, 
										   boolean seekSingleCapture)
	{
		super(query);
		
		this.resolveRevisits = resolveRevisits;
		this.seekSingleCapture = seekSingleCapture;
		this.isReverse = query.isReverse();
	}
	
	public void setTargetTimestamp(String timestamp)
	{		
		targetTimestamp = timestamp;
		
		if (isReverse) {
			flip = -1;
		}
	}

	@Override
    public void begin() {
		results = new CaptureSearchResults();
		
		if (resolveRevisits) {
			if (isReverse) {
				digestToRevisits = new HashMap<String, LinkedList<CaptureSearchResult>>();
			} else {
				digestToOriginal = new HashMap<String, CaptureSearchResult>();
			}
		}
    }

	@Override
    public int writeLine(CDXLine line) {
		FastCaptureSearchResult result = new FastCaptureSearchResult();
		
		String timestamp = line.getTimestamp();
		String originalUrl = line.getOriginalUrl();
		String statusCode = line.getStatusCode();
		
		if (lastResult != null) {
			if (lastResult.getCaptureTimestamp().equals(timestamp) && 
			    lastResult.getOriginalUrl().equals(originalUrl) &&
			    lastResult.getHttpCode().equals(statusCode)) {
				// Skip this
				return 0;
			}
		}
				
		result.setUrlKey(line.getUrlKey());
		result.setCaptureTimestamp(timestamp);
		result.setOriginalUrl(originalUrl);
		
		// Special case: filter out captures that have userinfo
		boolean hasUserInfo = (UrlOperations.urlToUserInfo(result.getOriginalUrl()) != null);
		
		if (hasUserInfo) {
			return 0;
		}
		
		result.setRedirectUrl(line.getRedirect());
		result.setHttpCode(statusCode);
		
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
		
		boolean isRevisit = false;
		
		if (resolveRevisits) {
			isRevisit = result.getFile().equals(CDXLine.EMPTY_VALUE) ||
						result.getMimeType().equals(REVISIT_VALUE);
			
			String digest = result.getDigest();
			
			if (isRevisit) {
				if (!isReverse) {
					CaptureSearchResult payload = digestToOriginal.get(digest);
					if (payload != null) {
						result.flagDuplicateDigest(payload);
					}
				} else {
					LinkedList<CaptureSearchResult> revisits = digestToRevisits.get(digest);
					if (revisits == null) {
						revisits = new LinkedList<CaptureSearchResult>();
						digestToRevisits.put(digest, revisits);
					}
					revisits.add(result);
				}
			} else {
				if (!isReverse) {
					digestToOriginal.put(digest, result);
				} else {
					LinkedList<CaptureSearchResult> revisits = digestToRevisits.remove(digest);
					if (revisits != null) {
						for (CaptureSearchResult revisit : revisits) {
							revisit.flagDuplicateDigest(result);
						}
					}
				}
			}
		}
		
//		String payloadFile = line.getField(RevisitResolver.origfilename);
//		
//		if (!payloadFile.equals(CDXLine.EMPTY_VALUE)) {
//			FastCaptureSearchResult payload = new FastCaptureSearchResult();
//			payload.setFile(payloadFile);
//			payload.setOffset(NumberUtils.toLong(line.getField(RevisitResolver.origoffset), -1));
//			payload.setCompressedLength(NumberUtils.toLong(line.getField(RevisitResolver.origlength), -1));
//			result.flagDuplicateDigest(payload);
//		}
		
		if ((targetTimestamp != null) && (closest == null)) {
			closest = determineClosest(result);
		}
		
		results.addSearchResult(result, !isReverse);
		lastResult = result;
		
		// Short circuit the load if seeking single capture
		if (seekSingleCapture && resolveRevisits) {
			if (closest != null) {
				// If not a revisit, we're done
				if (!isRevisit) {
					done = true;
				// Else make sure the revisit is resolved
				} else if (result.getDuplicatePayload() != null) {
					done = true;
				}
			}
		}
		
		return 1;
    }
	
	@Override
	public boolean isAborted()
	{
		return done;
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
		
		CaptureSearchResult lastResult = getLastAdded();
		
		
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
    	
    	if (!results.isEmpty()) {
    		// If no target timestamp, always return the latest capture, otherwise first or last based on reverse state
    		if (targetTimestamp != null) {
    			return getLastAdded();
    		} else {
    			return results.getResults().getLast();
    		}
    	}
    	
    	return null;
    }
    
    protected CaptureSearchResult getLastAdded()
    {
		if (!isReverse) {
			return results.getResults().getLast();
		} else {
			return results.getResults().getFirst();
		}
    }
    
    @Override
    public CaptureSearchResults getSearchResults()
    {
    	return results;
    }

	public SelfRedirectFilter getSelfRedirFilter() {
		return selfRedirFilter;
	}

	public void setSelfRedirFilter(SelfRedirectFilter selfRedirFilter) {
		this.selfRedirFilter = selfRedirFilter;
	}
}
