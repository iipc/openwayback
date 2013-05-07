package org.archive.wayback.replay.html.rewrite;

import java.util.List;
import java.util.StringTokenizer;

import org.archive.wayback.replay.html.ReplayParseContext;
import org.archive.wayback.replay.html.StringTransformer;

public class RewritingStringTransformer implements StringTransformer {

	private List<RewriteRule> policyRules;

	public List<RewriteRule> getPolicyRules() {
		return policyRules;
	}

	public void setPolicyRules(List<RewriteRule> policyRules) {
		this.policyRules = policyRules;
	}

	public String transform(ReplayParseContext rpContext, String input) {

		if (policyRules == null) {
			return input;
		}
		
		if (!rpContext.isInScriptText() && !rpContext.isInJS()) {
			return input;
		}

		String allPolicies = rpContext.getOraclePolicy();

		if (allPolicies == null) {
			return input;
		}

		StringTokenizer tokens = new StringTokenizer(allPolicies, ",");

		while (tokens.hasMoreElements()) {
			String policy = tokens.nextToken();

			for (RewriteRule rule : policyRules) {
				if (policy.startsWith(rule.getBeanName())) {
					input = rule.rewrite(rpContext, policy, input);
				}
			}
		}

		return input;
	}
}
