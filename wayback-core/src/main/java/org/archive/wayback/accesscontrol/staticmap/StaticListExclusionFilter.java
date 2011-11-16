package org.archive.wayback.accesscontrol.staticmap;

import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.util.SURT;
import org.archive.wayback.UrlCanonicalizer;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.surt.SURTTokenizer;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.url.AggressiveUrlCanonicalizer;

public class StaticListExclusionFilter extends ExclusionFilter {
	private static final Logger LOGGER = Logger.getLogger(
			StaticMapExclusionFilter.class.getName());

	private String lastChecked = null;
	private boolean lastCheckedExcluded = false;
	private boolean notifiedSeen = false;
	private boolean notifiedPassed = false;
	TreeSet<String> exclusions = null;
	UrlCanonicalizer canonicalizer = new AggressiveUrlCanonicalizer();
	/**
	 * @param map where each String key is a SURT that is blocked.
	 */
	public StaticListExclusionFilter(TreeSet<String> exclusions, UrlCanonicalizer canonicalizer) {
		this.exclusions = exclusions;
		this.canonicalizer = canonicalizer;
	}
	
	protected boolean isExcluded(String surt) {
	    String possiblePrefix = exclusions.floor(surt);
	    return (possiblePrefix != null && surt.startsWith(possiblePrefix));
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.SearchResultFilter#filterSearchResult(org.archive.wayback.core.SearchResult)
	 */
	public int filterObject(CaptureSearchResult r) {
		if(!notifiedSeen) { 
			if(filterGroup != null) {
				filterGroup.setSawAdministrative();
			}
			notifiedSeen = true;
		}
		String surt;
		try {
			String url = canonicalizer.urlStringToKey(r.getOriginalUrl());
			surt = SURT.fromPlain(url);
//			surt = SURTTokenizer.prefixKey(url);
		} catch (URIException e) {
			
			//e.printStackTrace();
			return FILTER_EXCLUDE;
		}
		if(lastChecked != null) {
			if(lastChecked.equals(surt)) {
				if(lastCheckedExcluded) { 
					return ObjectFilter.FILTER_EXCLUDE;
				} else {
					// don't need to: already did last time...
					//filterGroup.setPassedAdministrative();
					return ObjectFilter.FILTER_INCLUDE;
				}
			}
		}
		lastChecked = surt;
		lastCheckedExcluded = isExcluded(surt);
		if(lastCheckedExcluded) {
			return ObjectFilter.FILTER_EXCLUDE;
		} else {
			if(!notifiedPassed) {
				if(filterGroup != null) {
					filterGroup.setPassedAdministrative();
				}
				notifiedPassed = true;
			}
			return ObjectFilter.FILTER_INCLUDE;
		}
			
	}
	
}
