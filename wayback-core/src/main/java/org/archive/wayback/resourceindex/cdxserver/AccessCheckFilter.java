package org.archive.wayback.resourceindex.cdxserver;

import org.archive.cdxserver.auth.AuthToken;
import org.archive.cdxserver.filter.CDXAccessFilter;
import org.archive.cdxserver.filter.CDXFilter;
import org.archive.format.cdx.CDXLine;
import org.archive.util.io.RuntimeIOException;
import org.archive.wayback.accesscontrol.oracleclient.CustomPolicyOracleFilter;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.exception.AdministrativeAccessControlException;
import org.archive.wayback.exception.RobotAccessControlException;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;
import org.archive.wayback.util.url.UrlOperations;

/**
 * Standard {@link CDXAccessFilter} implementation useful for most cases.
 * <p>
 * 2014-11-06: Disabled per-{@code urlkey} caching of <i>include</i> result (
 * {@code lastKey} and {@code cachedValue}). It assumes exclusion is per-URL
 * basis, which is not necessarily true (access control oracle allows for
 * excluding captures by date-range, for example). Such optimization should be
 * done in ExclusionFilter.
 * </p>
 * <p>
 * 2014-11-06: The second CDXFilter (@code cdxFilter2) is soon to be dropped. If
 * more than one {@code CDXFilter}s are needed, bundle them up in one composite
 * CDXFilter.
 * </p>
 */
public class AccessCheckFilter implements CDXAccessFilter {

	protected ExclusionFilter adminFilter;
	protected ExclusionFilter robotsFilter;
	protected CDXFilter cdxFilter;
	// being dropped
	protected CDXFilter cdxFilter2;

//	protected CaptureSearchResult resultTester;

	protected AuthToken authToken;

	protected String lastKey;
	protected boolean cachedValue = false;

	/**
	 * Initialize with {@code AuthToken}, two {@code ExclusionFilter}s and just one {@link CDXFilter}.
	 * @param token provides user privilege information
	 * @param adminFilter administrative exclusion filter
	 * @param robotsFilter robots exclusion filter
	 * @param cdxFilter CDX filter for narrowing down the visible archive space.
	 */
	public AccessCheckFilter(AuthToken token, ExclusionFilter adminFilter, ExclusionFilter robotsFilter, CDXFilter cdxFilter) {
		this(token, adminFilter, robotsFilter, cdxFilter, null);
	}

	/**
	 * Initializes with {@code AuthToken}, two {@code ExclusionFilter}s and two {@code CDXFilter}s.
	 * Both {@code cdxFilter} and {@code cdxFilter2} must pass for capture to be included (i.e. they are AND).
	 * @param token provides user privilege information
	 * @param adminFilter administrative exclusion filter
	 * @param robotsFilter robots exclusion filter
	 * @param cdxFilter CDX filter for narrowing down the visible archive space.
	 * @param cdxFilter2 Second CDX filter.
	 * @obsolete 2014-11-06 Use one CDXFilter version.
	 */
	public AccessCheckFilter(AuthToken token, ExclusionFilter adminFilter,
			ExclusionFilter robotsFilter, CDXFilter cdxFilter,
			CDXFilter cdxFilter2) {

		this.authToken = token;

		this.adminFilter = adminFilter;
		this.robotsFilter = robotsFilter;

		this.cdxFilter = cdxFilter;
		this.cdxFilter2 = cdxFilter2;

//		this.resultTester = new FastCaptureSearchResult();
	}

	public boolean include(CaptureSearchResult resultTester, boolean throwOnFail) {
		int status = ExclusionFilter.FILTER_INCLUDE;

		// Admin Excludes
		if (adminFilter != null) {
			status = adminFilter.filterObject(resultTester);
		}

		if (status != ExclusionFilter.FILTER_INCLUDE) {
			if (throwOnFail) {
				throw new RuntimeIOException(403,
					new AdministrativeAccessControlException(
						resultTester.getOriginalUrl() +
								" is not available in the Wayback Machine."));
			} else {
//				lastKey = resultTester.getUrlKey();
//				return cachedValue;
				return false;
			}
		}

		// Robot Excludes
		if (robotsFilter != null && !authToken.isIgnoreRobots()) {
			status = robotsFilter.filterObject(resultTester);
		}

		if (status != ExclusionFilter.FILTER_INCLUDE) {
			if (throwOnFail) {
				throw new RuntimeIOException(403,
					new RobotAccessControlException(
						resultTester.getOriginalUrl() +
								" is blocked by the sites robots.txt file"));
			} else {
//				lastKey = resultTester.getUrlKey();
//				return cachedValue;
				return false;
			}
		}

//		lastKey = resultTester.getUrlKey();
//		cachedValue = true;
//
//		return cachedValue;
		return true;
	}

//	public boolean include(String urlKey, String originalUrl,
//			boolean throwOnFail) {
//
//		if (lastKey != null && lastKey.equals(urlKey)) {
//			return cachedValue;
//		}
//
//		cachedValue = false;
//
//		if (UrlOperations.urlToScheme(originalUrl) == null) {
//			originalUrl = UrlOperations.HTTP_SCHEME + originalUrl;
//		}
//
//		resultTester.setUrlKey(urlKey);
//		resultTester.setOriginalUrl(originalUrl);
//
//		return include(resultTester, throwOnFail);
//	}

	@Override
	public boolean includeUrl(String urlKey, String originalUrl) {
//		return include(urlKey, originalUrl, true);
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

	/**
	 * Adapts CDXLine to CaptureSearchResult interface. Fetches
	 * {@code originalUrl}, {@code captureTimestamp} and {@code robotFlags} from
	 * the {@code CDXLine} adopted (minimum required for known existing filter
	 * implementations). It also have {@code setRobotFlag} call modify
	 * {@code robotflags} field in the underlining {@code CDXLine} (necessary
	 * for soft-block feature).
	 * <p>
	 * TODO: Unfortunately this is not as lightweight as it should have been.
	 * Only if CaptureSearchResult was an interface.
	 * </p>
	 * <p>
	 * Caveat: it only overrides those methods used by known existing filter
	 * implementations.
	 * </p>
	 * @see CustomPolicyOracleFilter
	 */
	protected static class CDXSearchResult extends FastCaptureSearchResult {
		final CDXLine cdxLine;

		public CDXSearchResult(CDXLine cdxLine) {
			this.cdxLine = cdxLine;
		}

		@Override
		public String getUrlKey() {
			return cdxLine.getUrlKey();
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


	@Override
	public boolean includeCapture(CDXLine line) {

//		if (!include(line.getUrlKey(), line.getOriginalUrl(), false)) {
//			return false;
//		}
		CDXSearchResult searchResult = new CDXSearchResult(line);
		if (!include(searchResult, false))
			return false;

		// TODO: cdxFilter should be applied *before* exclusion filter.

		if (cdxFilter != null && !cdxFilter.include(line))
			return false;
		if (cdxFilter2 != null && !cdxFilter2.include(line))
			return false;

		return true;
	}
}
