/**
 *
 */
package org.archive.wayback.resourceindex.cdxserver;

import java.util.List;
import java.util.logging.Logger;

import org.archive.cdxserver.CDXServer;
import org.archive.cdxserver.auth.AuthChecker;
import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.auth.PrivTokenAuthChecker;
import org.archive.cdxserver.filter.CDXAccessFilter;
import org.archive.cdxserver.filter.CDXFilter;
import org.archive.cdxserver.filter.FilenamePrefixFilter;
import org.archive.format.cdx.CDXLine;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.accesscontrol.oracleclient.CustomPolicyOracleFilter;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.webapp.AccessPoint;

/**
 * {@link AuthChecker} implementation that runs {@link ExclusionFilter} provided
 * through {@link AccessPoint#getExclusionFactory()} as well as the filtering
 * provided by {@link WaybackAPAuthChecker}.
 * <p>
 * This is primarily meant for running {@link CustomPolicyOracleFilter} in
 * {@link CDXServer} rather than in {@link EmbeddedCDXServerIndex}, making
 * {@link WaybackAPAuthChecker} obsolete. Current implementation is very
 * specific to this usage.
 * </p>
 * Needs {@link APContextAuthToken} as token.</p>
 */
public class AccessPointAuthChecker extends PrivTokenAuthChecker {

	private static final Logger logger = Logger
		.getLogger(AccessPointAuthChecker.class.getName());

	protected ExclusionFilterFactory fallbackExclusionFactory;

	/**
	 * {@link ExclusionFilterFactory} used if token passed to
	 * {@link #createAccessFilter(AuthToken)} is not an instance of
	 * {@link APContextAuthToken} (CDX server query, for example).
	 * @param fallbackExclusionFactory
	 */
	public void setFallbackExclusionFactory(
			ExclusionFilterFactory fallbackExclusionFactory) {
		this.fallbackExclusionFactory = fallbackExclusionFactory;
	}

	public ExclusionFilterFactory getFallbackExclusionFactory() {
		return fallbackExclusionFactory;
	}

	/**
	 * {@link CDXFilter} on prefix of filename field.
	 * <p>
	 * It first accepts all captures that have any of {@code includePrefixes} as
	 * filename prefixes, then rejects captures that have any of
	 * {@code excludePrefixes}. If {@code includePrefixes} is {@code null} (as
	 * opposed to empty), all captures are accepted.
	 * </p>
	 */
	protected class CombinedFilenamePrefixFilter implements CDXFilter {
		FilenamePrefixFilter includeFilter;
		FilenamePrefixFilter excludeFilter;

		public CombinedFilenamePrefixFilter(List<String> includePrefixes,
				List<String> excludePrefixes) {
			if (includePrefixes != null)
				this.includeFilter = new FilenamePrefixFilter(includePrefixes,
					false);
			if (excludePrefixes != null)
				this.excludeFilter = new FilenamePrefixFilter(excludePrefixes,
					true);
		}

		@Override
		public boolean include(CDXLine line) {
			return (includeFilter == null || includeFilter.include(line)) &&
					(excludeFilter == null || excludeFilter.include(line));
		}
	}

	@Override
	public CDXAccessFilter createAccessFilter(AuthToken token) {
		if (token instanceof APContextAuthToken) {
			AccessPoint ap = ((APContextAuthToken)token).getAccessPoint();

			CDXFilter prefixFilter = new CombinedFilenamePrefixFilter(
				ap.getFileIncludePrefixes(), ap.getFileExcludePrefixes());

			ExclusionFilter apFilter = null;
			try {
				apFilter = ap.createExclusionFilter();
			} catch (Exception ex) {
				// FIXME: createExclusionFilter throws a sub-class of
				// AccessControlException when it fails to initialize
				// the exclusion component. Unfortunately it cannot be
				// thrown from this method because AuthChecker interface
				// is part of cdx-server project which cannot depend on
				// wayback-core, where AccessControlException is defined.
				// Yet, we should not just ignore this error because
				// exclusion system failure can be highly risky.
				// For now, we throw AccessControlException wrapped in
				// RuntimeException, assuming caller is catching it.
				// (See EmbeddedCDXServerIndex#doQuery(WaybackRequest))
				// Probably we should define a new exception in cdx-server.
				throw new RuntimeException(ex);
			}
			return new AccessCheckFilter(token, apFilter, null, prefixFilter);
		} else {
			ExclusionFilter apFilter = null;
			if (fallbackExclusionFactory != null) {
				apFilter = fallbackExclusionFactory.get();
			}
			return new AccessCheckFilter(token, apFilter, null, null);
		}
	}
}
