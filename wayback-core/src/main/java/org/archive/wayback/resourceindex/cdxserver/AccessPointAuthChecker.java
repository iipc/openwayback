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
import org.archive.cdxserver.filter.FilenamePrefixFilter;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.accesscontrol.oracleclient.CustomPolicyOracleFilter;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.webapp.AccessPoint;

/**
 * {@link AuthChecker} implementation that runs {@link ExclusionFilter} provided
 * through {@link AccessPoint#getExclusionFactory()} as well as the filtering provided
 * by {@link WaybackAPAuthChecker}.
 * <p>
 * This is primarily meant for running {@link CustomPolicyOracleFilter} in {@link CDXServer}
 * rather than in {@link EmbeddedCDXServerIndex}, making {@link WaybackAPAuthChecker}
 * obsolete.
 * </p>
 * Needs {@link APContextAuthToken} as token.
 * </p>
 */
public class AccessPointAuthChecker extends PrivTokenAuthChecker {
	
	private static final Logger logger = Logger.getLogger(AccessPointAuthChecker.class.getName());

	protected ExclusionFilterFactory adminExclusions;

	protected final FilenamePrefixFilter buildPrefixFilter(
			List<String> prefixes, boolean exclusion) {
		return prefixes != null ? new FilenamePrefixFilter(prefixes, exclusion)
				: null;
	}

	@Override
	public CDXAccessFilter createAccessFilter(AuthToken token) {
		ExclusionFilter adminFilter = adminExclusions != null ? adminExclusions.get() : null;

		if (token instanceof APContextAuthToken) {
			AccessPoint ap = ((APContextAuthToken)token).getAccessPoint();

			FilenamePrefixFilter include = buildPrefixFilter(
				ap.getFileIncludePrefixes(), false);
			FilenamePrefixFilter exclude = buildPrefixFilter(
				ap.getFileExcludePrefixes(), true);

			ExclusionFilterFactory filterFactory = ap.getExclusionFactory();
			ExclusionFilter apFilter = filterFactory != null ? filterFactory.get()
					: null;

			return new AccessCheckFilter(token, adminFilter, apFilter, include,
				exclude);
		} else {
			logger.severe(
				String.format("token is not of type APContextAuthToken, but %s." +
						" AccessPoint-specific exclusion is not applied. Check %s.createAuthToken()",
						token.getClass().getName(), EmbeddedCDXServerIndex.class.getSimpleName()));
			return new AccessCheckFilter(token, adminFilter, null, null, null);
		}
	}

	public ExclusionFilterFactory getAdminExclusions() {
		return adminExclusions;
	}

	public void setAdminExclusions(ExclusionFilterFactory adminExclusions) {
		this.adminExclusions = adminExclusions;
	}

}
