package org.archive.wayback.accesscontrol.oracleclient;

import java.util.Date;
import java.util.logging.Logger;

import org.archive.accesscontrol.RobotsUnavailableException;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.accesspoint.AccessPointAdapter;
import org.archive.wayback.core.CaptureSearchResult;

/**
 * Oracle Filter Implementation that supports custom policies in addition to
 * allow, block, block-message and robots
 *
 * The policy is stored in the CaptureSearchResult
 *
 * <p>
 * Note: it's not clear how this class helps support custom policies.
 * {@code allow}, {@code block}, and {@code robots} are supported by base-class
 * and handled in the same way. Perhaps it's about {@code block-message}? This
 * class issues no message for {@code block}.
 * </p>
 * <p>
 * Although there's a factory for this class,
 * {@link CustomPolicyOracleFilterFactory}, creation is hard-coded in
 * {@link AccessPointAdapter} currently.
 * </p>
 * @see CustomPolicyOracleFilterFactory
 * @see AccessPointAdapter
 */
public class CustomPolicyOracleFilter extends OracleExclusionFilter {

	private static final Logger LOGGER = Logger
		.getLogger(CustomPolicyOracleFilter.class.getName());

	enum Policy {
		ALLOW("allow"),
		BLOCK_HIDDEN("block"),
		BLOCK_MESSAGE("block-message"),
		ROBOTS("robots");

		Policy(String policy) {
			this.policy = policy;
		}

		boolean matches(String other) {
			return (other.equals(this.policy));
		}

		String policy;
	}

	protected int defaultFilter = FILTER_INCLUDE;

	public CustomPolicyOracleFilter(String oracleUrl, String accessGroup,
			String proxyHostPort) {
		super(oracleUrl, accessGroup, proxyHostPort);
	}

	@Override
	public int filterObject(CaptureSearchResult o) {
		String url = o.getOriginalUrl();
		Date captureDate = o.getCaptureDate();
		Date retrievalDate = new Date();

		String policy;
		try {
			policy = client.getPolicy(
				ArchiveUtils.addImpliedHttpIfNecessary(url), captureDate,
				retrievalDate, accessGroup);

			o.setOraclePolicy(policy);

			if (policy == null) {
				return defaultFilter;
			}

			if (Policy.ALLOW.matches(policy)) {
				return handleAllow();
			}

			// Block page but silently, as if it wasn't found
			if (Policy.BLOCK_HIDDEN.matches(policy)) {
				o.setRobotFlag(CaptureSearchResult.CAPTURE_ROBOT_BLOCKED);
				//return FILTER_EXCLUDE;
				return FILTER_INCLUDE;
			}

			// Block page bit and display "access blocked" message
			if (Policy.BLOCK_MESSAGE.matches(policy)) {
				return handleBlock();
			}

			if (Policy.ROBOTS.matches(policy)) {
				return handleRobots();
			}
		} catch (RobotsUnavailableException e) {
			e.printStackTrace();
		} catch (RuleOracleUnavailableException e) {
			LOGGER.warning(
				"Oracle Unavailable/not running, default to allow all until it responds. Details: " +
						e.toString());
		}

		return defaultFilter;
	}
}
