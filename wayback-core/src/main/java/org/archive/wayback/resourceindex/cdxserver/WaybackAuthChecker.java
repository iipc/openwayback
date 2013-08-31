package org.archive.wayback.resourceindex.cdxserver;

import org.apache.commons.httpclient.URIException;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.auth.PrivTokenAuthChecker;
import org.archive.cdxserver.cdx.filter.FilenamePrefixFilter;
import org.archive.format.cdx.CDXLine;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.accesscontrol.robotstxt.redis.RedisRobotExclusionFilterFactory;
import org.archive.wayback.accesscontrol.staticmap.StaticMapExclusionFilterFactory;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.exception.AdministrativeAccessControlException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

public class WaybackAuthChecker extends PrivTokenAuthChecker {
	
	protected StaticMapExclusionFilterFactory adminExclusions;
	protected RedisRobotExclusionFilterFactory robotsExclusions;
	protected UrlCanonicalizer canonicalizer = null;
	protected FilenamePrefixFilter prefixFilter = null;

	@Override
    public boolean checkUrlAccess(String url) {
		ExclusionFilter filter = null;
		
		FastCaptureSearchResult result = new FastCaptureSearchResult();
		result.setOriginalUrl(url);
		
		int status = ExclusionFilter.FILTER_EXCLUDE;
		
		String urlKey = url;
		
		try {
			
			if (!url.startsWith("http://") && !url.startsWith("https://")) {
				url = "http://" + url;
			}
			
			// Must set canonicalizer
			urlKey = canonicalizer.urlStringToKey(url);			
			result.setUrlKey(urlKey);
			result.setOriginalUrl(url);
			
			// Admin Excludes
			filter = adminExclusions.get();
			status = filter.filterObject(result);
			
			if (status != ExclusionFilter.FILTER_INCLUDE) {
				throw new RuntimeException(new AdministrativeAccessControlException(url + " is not available in the Wayback Machine."));
			}
			
			// Robot Excludes
			filter = robotsExclusions.get();
			status = filter.filterObject(result);
			
			if (status != ExclusionFilter.FILTER_INCLUDE) {
				throw new RuntimeException(new RobotAccessControlException(url + " is blocked by the sites robots.txt file"));
			}
			
		} catch (URIException e) {
			//Is this right?
			return false;
		}
		
		return true;
    }
	
	public StaticMapExclusionFilterFactory getAdminExclusions() {
		return adminExclusions;
	}

	public void setAdminExclusions(StaticMapExclusionFilterFactory adminExclusions) {
		this.adminExclusions = adminExclusions;
	}

	public RedisRobotExclusionFilterFactory getRobotsExclusions() {
		return robotsExclusions;
	}

	public void setRobotsExclusions(
	        RedisRobotExclusionFilterFactory robotsExclusions) {
		this.robotsExclusions = robotsExclusions;
	}

	public UrlCanonicalizer getCanonicalizer() {
		return canonicalizer;
	}

	public void setCanonicalizer(UrlCanonicalizer canonicalizer) {
		this.canonicalizer = canonicalizer;
	}

	public FilenamePrefixFilter getPrefixFilter() {
		return prefixFilter;
	}

	public void setPrefixFilter(FilenamePrefixFilter prefixFilter) {
		this.prefixFilter = prefixFilter;
	}

	@Override
    public boolean isCaptureAllowed(CDXLine line, AuthToken auth) {		
	    if (prefixFilter == null) {
	    	return true;
	    }
	    
		if (this.isAllUrlAccessAllowed(auth)) {
			return true;
		}
	    
	    return prefixFilter.include(line);
    }
}
