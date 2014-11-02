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
import org.archive.util.io.RuntimeIOException;
import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.accesscontrol.oracleclient.CustomPolicyOracleFilter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.exception.AdministrativeAccessControlException;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.url.UrlOperations;
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

	/**
	 * Tentative {@link CDXAccessFilter} implementation supporting date-range
	 * access check. Standard implementation {@link AccessCheckFilter} caches
	 * result b urlkey, and does not pass captureTimestamp to ExclusionFilter.
	 * This implementation also allows ExclusionFilter to modify robotflags
	 * field.
	 * <p>
	 * We use this inner class until access check framework is refactored - I
	 * don't want to cause performance problem to existing implementation by
	 * simply removing caching.
	 * </p>
	 * <p>
	 * Caveat: It is not fully sorted out how to pass {@code oraclePolicy} value
	 * set by {@link CustomPolicyOracleFilter} to final
	 * {@link CaptureSearchResult}. We need to either add a field to CDXLine, or
	 * make the second call to Oracle in later step.
	 * </p>
	 */
	protected static class CDXAccessFilterImpl implements CDXAccessFilter {

		private ExclusionFilter adminFilter;
		private CDXFilter cdxFilter;

		public CDXAccessFilterImpl(AuthToken token,
				ExclusionFilter adminFilter, CDXFilter cdxFilter) {
			this.adminFilter = adminFilter;
			this.cdxFilter = cdxFilter;
		}

		@Override
		public boolean includeUrl(String urlKey, String originalUrl) {
			if (UrlOperations.urlToScheme(originalUrl) == null) {
				originalUrl = UrlOperations.HTTP_SCHEME + originalUrl;
			}

			CaptureSearchResult resultTester = new FastCaptureSearchResult();
			resultTester.setUrlKey(urlKey);
			resultTester.setOriginalUrl(originalUrl);
			// null captureTimestamp signifies per-URL access-check.
			resultTester.setCaptureTimestamp(null);

			return include(resultTester, true);
		}

		@Override
		public boolean includeCapture(CDXLine line) {
			CDXSearchResult searchResult = new CDXSearchResult(line);
			// TODO: ExclusionFilter may set oraclePolicy to line.
			// How to carry the value to later step where CDXLine is
			// finally converted to FastCaptureSearchResult?
			if (!include(searchResult, false)) {
				return false;
			}
			if (cdxFilter != null && !cdxFilter.include(line)) {
				return false;
			}
			return true;
		}

		/**
		 * Adapts CDXLine to CaptureSearchResult interface.
		 * <p>
		 * TODO: Unfortunately this is not as lightweight as it should have
		 * been. Only if CaptureSearchResult was an interface.
		 * </p>
		 * <p>
		 * Caveat: it only overrides those methods used by existing filter
		 * implementations ({@code CustomPolicyOracleFilter}).
		 * </p>
		 */
		protected class CDXSearchResult extends FastCaptureSearchResult {
			final CDXLine cdxLine;

			public CDXSearchResult(CDXLine cdxLine) {
				this.cdxLine = cdxLine;
			}

			@Override
			public final String getOriginalUrl() {
				return cdxLine.getOriginalUrl();
			}

			// CustomPolicyOracleFilter calls getCaptureDate(),
			// which is implemented by CaptureSearchResult on top
			// of getCaptureTimestamp()
			@Override
			public final String getCaptureTimestamp() {
				return cdxLine.getTimestamp();
			}

			@Override
			public final void setRobotFlag(char flag) {
				String robotFlags = cdxLine.getRobotFlags();
				if (robotFlags == null || robotFlags.equals("-")) {
					setRobotFlags(Character.toString(flag));
				} else {
					if (robotFlags.indexOf(flag) == -1) {
						setRobotFlags(robotFlags + flag);
					}
				}
			}

			@Override
			public final void setRobotFlags(String robotFlags) {
				// CDXLine does not have setter for robotFlags,
				// but setField method is available.
				cdxLine.setField(CDXLine.robotflags, robotFlags);
			}
		}

		public boolean include(CaptureSearchResult resultTester,
				boolean throwOnFail) {
			int status = ExclusionFilter.FILTER_INCLUDE;

			// Admin Excludes
			if (adminFilter != null) {
				status = adminFilter.filterObject(resultTester);
			}

			if (status != ExclusionFilter.FILTER_INCLUDE) {
				if (throwOnFail) {
					throw new RuntimeIOException(403,
						new AdministrativeAccessControlException(resultTester
							.getOriginalUrl() +
								" is not available in the Wayback Machine."));
				} else {
					return false;
				}
			}

			return true;
		}

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

			ExclusionFilterFactory filterFactory = ap.getExclusionFactory();
			ExclusionFilter apFilter = filterFactory != null ? filterFactory
				.get() : null;

			return new CDXAccessFilterImpl(token, apFilter, prefixFilter);
		} else {
			logger.severe(String.format(
				"token is not of type APContextAuthToken, but %s."
						+ " AccessPoint-specific exclusion is not applied. Check %s.createAuthToken()",
						token.getClass().getName(),
						EmbeddedCDXServerIndex.class.getSimpleName()));
			return new CDXAccessFilterImpl(token, null, null);
		}
	}
}
