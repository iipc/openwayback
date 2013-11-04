package org.archive.wayback.resourceindex.cdxserver;

import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.filter.CDXAccessFilter;
import org.archive.cdxserver.filter.CDXFilter;
import org.archive.format.cdx.CDXLine;
import org.archive.util.io.RuntimeIOException;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.exception.AdministrativeAccessControlException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.url.UrlOperations;

public class AccessCheckFilter implements CDXAccessFilter {
	
	protected ExclusionFilter adminFilter;
	protected ExclusionFilter robotsFilter;
	protected CDXFilter prefixFilter1;
	protected CDXFilter prefixFilter2;
	
	protected CaptureSearchResult resultTester;
	
	protected AuthToken authToken;
	
	protected String lastKey;
	protected boolean cachedValue = false;

	public AccessCheckFilter(
			AuthToken token, 
			ExclusionFilter adminFilter,
			ExclusionFilter robotsFilter,
			CDXFilter prefixFilter1,
			CDXFilter prefixFilter2) {
	    
		this.authToken = token;
		
	    this.adminFilter = adminFilter;
	    this.robotsFilter = robotsFilter;
	    
	    this.prefixFilter1 = prefixFilter1;
	    this.prefixFilter2 = prefixFilter2;
	    
	    this.resultTester = new FastCaptureSearchResult();
    }
	
	public boolean include(String urlKey, String originalUrl, boolean throwOnFail) {
		
		if ((lastKey != null) && lastKey.equals(urlKey)) {
			return cachedValue;
		}
		
		cachedValue = false;
		
		if (UrlOperations.urlToScheme(originalUrl) == null) {
			originalUrl = UrlOperations.HTTP_SCHEME + originalUrl;
		}

		resultTester.setUrlKey(urlKey);
		resultTester.setOriginalUrl(originalUrl);
		
		return include(resultTester, throwOnFail);
	}
		
	public boolean include(CaptureSearchResult resultTester, boolean throwOnFail)
	{			
		int status = ExclusionFilter.FILTER_INCLUDE;
			
		// Admin Excludes
		if (adminFilter != null) {
			status = adminFilter.filterObject(resultTester);
		}
		
		if (status != ExclusionFilter.FILTER_INCLUDE) {
			if (throwOnFail) {
				throw new RuntimeIOException(403, new AdministrativeAccessControlException(resultTester.getOriginalUrl() + " is not available in the Wayback Machine."));
			} else {
				lastKey = resultTester.getUrlKey();
				return cachedValue;
			}
		}
		
		// Robot Excludes
		if ((robotsFilter != null) && !authToken.isIgnoreRobots()) {
			status = robotsFilter.filterObject(resultTester);
		}
		
		if (status != ExclusionFilter.FILTER_INCLUDE) {
			if (throwOnFail) {
				throw new RuntimeIOException(403, new RobotAccessControlException(resultTester.getOriginalUrl() + " is blocked by the sites robots.txt file"));
			} else {
				lastKey = resultTester.getUrlKey();
				return cachedValue;
			}
		}
		
		lastKey = resultTester.getUrlKey();
		cachedValue = true;
		
		return cachedValue;
    }
	
	@Override
	public boolean includeUrl(String urlKey, String originalUrl)
	{
		return include(urlKey, originalUrl, true);
	}

	@Override
    public boolean includeCapture(CDXLine line) {
		
	    if (!include(line.getUrlKey(), line.getOriginalUrl(), false)) {
	    	return false;
	    }
	    
		// Custom Prefix Filters
		if (prefixFilter1 != null) {
			if (!prefixFilter1.include(line)) {
				return false;
			}
		}
		
		if (prefixFilter2 != null) {
			if (!prefixFilter2.include(line)) {
				return false;
			}
		}
		
		return true;
    }
}
