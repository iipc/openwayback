/**
 * 
 */
package org.archive.wayback;

import javax.servlet.http.HttpServletRequest;

import org.archive.wayback.webapp.AccessPoint;

/**
 * Experimental extension of ReplayURIConverter for reflecting request
 * information to URI rewrite.
 * 
 * This interface is meant to replace
 * {@link AccessPoint#decorateURIConverter(ReplayURIConverter, HttpServletRequest, org.archive.wayback.core.WaybackRequest)}
 * , which turned out to be cumbersome for the purpose. Currently not utilized by wayback-core
 * framework, but only tested with some individual AccessPoint implementation.
 */
public interface DynamicReplayURIConverter extends ReplayURIConverter {
	/**
	 * Return new ReplayURIConverter adopted to current request {@code httpRequest}.
	 * @param httpRequest
	 * @return
	 */
	public ReplayURIConverter adaptToRequest(HttpServletRequest httpRequest);
}
