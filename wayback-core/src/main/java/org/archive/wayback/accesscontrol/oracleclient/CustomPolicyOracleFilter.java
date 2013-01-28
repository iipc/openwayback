/*
 * Oracle Filter Implementation that supports custom policies in addition to 
 * allow, block, block-message and robots
 * 
 * The policy is stored in the CaptureSearchResult
 */

package org.archive.wayback.accesscontrol.oracleclient;

import java.util.Date;
import java.util.logging.Logger;

import org.archive.accesscontrol.RobotsUnavailableException;
import org.archive.accesscontrol.RuleOracleUnavailableException;
import org.archive.util.ArchiveUtils;
import org.archive.wayback.core.CaptureSearchResult;

public class CustomPolicyOracleFilter extends OracleExclusionFilter {

	public static final String CAPTURE_ORACLE_POLICY = "oracle-policy";
	
	private static final Logger LOGGER = Logger.getLogger(
			CustomPolicyOracleFilter.class.getName());	

	enum Policy {
		ALLOW("allow"), 
		BLOCK_HIDDEN("block"), 
		BLOCK_MESSAGE("block-message"),
		ROBOTS("robots");

		Policy(String policy) {
			this.policy = policy;
		}
		
		boolean matches(String other)
		{
			return (other.equals(this.policy));
		}

		String policy;
	}

	protected int defaultFilter = FILTER_INCLUDE;

	public CustomPolicyOracleFilter(String oracleUrl, String accessGroup, String proxyHostPort) {
		super(oracleUrl, accessGroup, proxyHostPort);
	}

	@Override
	public int filterObject(CaptureSearchResult o) {
		String url = o.getOriginalUrl();
		Date captureDate = o.getCaptureDate();
		Date retrievalDate = new Date();

		String policy;
		try {
			policy = client.getPolicy(ArchiveUtils.addImpliedHttpIfNecessary(url), captureDate, retrievalDate, accessGroup);
			
			o.put(CAPTURE_ORACLE_POLICY, policy);
			
			if (policy == null) {
				return defaultFilter;
			}
			
			if (Policy.ALLOW.matches(policy)) {
				return handleAllow();
			}
			
			// Block page but silently, as if it wasn't found			
			if (Policy.BLOCK_HIDDEN.matches(policy)) {
				return FILTER_EXCLUDE;
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
			LOGGER.warning("Oracle Unavailable/not running, default to allow all until it responds. Details: " + e.toString());
		}
		
		return defaultFilter;
	}
}
