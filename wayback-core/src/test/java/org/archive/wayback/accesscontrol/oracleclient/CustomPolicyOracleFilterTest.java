package org.archive.wayback.accesscontrol.oracleclient;

import java.util.Date;

import junit.framework.TestCase;

import org.archive.accesscontrol.AccessControlClient;
import org.archive.accesscontrol.RobotsUnavailableException;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.core.FastCaptureSearchResult;
import org.archive.wayback.resourceindex.filterfactory.ExclusionCaptureFilterGroup;

/**
 * Test of {@link CustomPolicyOracleFilter}.
 * <p>
 * Has provision of testing error notification through
 * {@link ExclusionCaptureFilterGroup}, but test is not implemented, as it looks
 * like unused in current deployment.
 * </p>
 */
public class CustomPolicyOracleFilterTest extends TestCase {

	CustomPolicyOracleFilter cut;
	ExclusionCaptureFilterGroup filterGroup = null;
	TestAccessControlClient acClient;

	class TestAccessControlClient extends AccessControlClient {
		String policyToReturn = "allow";
		public TestAccessControlClient() {
			super("");
		}
		@Override
		public String getPolicy(String url, Date captureDate,
				Date retrievalDate, String who)
						throws RobotsUnavailableException,
						RuleOracleUnavailableException {
			return policyToReturn;
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		cut = new CustomPolicyOracleFilter("", "group", null);
		cut.client = (acClient = new TestAccessControlClient());
	}

	public void testAllow() {
		// object properties are not really used except for originalUrl.
		CaptureSearchResult capture = new FastCaptureSearchResult();
		capture.setOriginalUrl("http://www.example.com/");

		int rv = cut.filterObject(capture);

		assertEquals(CustomPolicyOracleFilter.FILTER_INCLUDE, rv);
	}

	public void testBlock() {
		acClient.policyToReturn = "block";

		// object properties are not really used except for originalUrl.
		CaptureSearchResult capture = new FastCaptureSearchResult();
		capture.setOriginalUrl("http://www.example.com/");

		int rv = cut.filterObject(capture);

		// Now "block" returns FILTER_INCLUDE, "X" flag in robotflags.
		assertEquals(CustomPolicyOracleFilter.FILTER_INCLUDE, rv);
		assertEquals(
			Character.toString(CaptureSearchResult.CAPTURE_ROBOT_BLOCKED),
			capture.getRobotFlags());
	}

	public void testBlockMessage() {
		acClient.policyToReturn = "block-message";

		// object properties are not really used except for originalUrl.
		CaptureSearchResult capture = new FastCaptureSearchResult();
		capture.setOriginalUrl("http://www.example.com/");

		int rv = cut.filterObject(capture);

		// Now "block" returns FILTER_INCLUDE, "X" flag in robotflags.
		assertEquals(CustomPolicyOracleFilter.FILTER_EXCLUDE, rv);
	}

	public void testRobots() {
		// AccessControlClient translates "robots" policy into either
		// "allow" or "block" when robotLookupsEnable is true - which
		// are tested above. "robots" policy is considered as "allow".
		acClient.policyToReturn = "robots";

		// object properties are not really used except for originalUrl.
		CaptureSearchResult capture = new FastCaptureSearchResult();
		capture.setOriginalUrl("http://www.example.com/");

		int rv = cut.filterObject(capture);

		assertEquals(CustomPolicyOracleFilter.FILTER_INCLUDE, rv);
	}
}
