package org.archive.wayback.replay.html.rewrite;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.archive.wayback.archivalurl.FastArchivalUrlReplayParseEventHandler;
import org.archive.wayback.replay.html.ReplayParseContext;

/**
 * {@link RewriteRule} for disabling inline {@code SCRIPT} block
 * by zero-based positiional index. It replaces entire element
 * content of designated {@code SCRIPT} with {@code removeMsg}.
 * Typically set up as one of {@code jsBlockTrans} of
 * {@link FastArchivalUrlReplayParseEventHandler}.
 * @see FastArchivalUrlReplayParseEventHandler
 * @see DisableJSIncludeRewriteRule
 * @see RewritingStringTransformer
 */
public class DisableJSBlocksRewriteRule extends RewriteRule {

	private static final Logger LOGGER = Logger
		.getLogger(DisableJSBlocksRewriteRule.class.getName());

	protected String removeMsg = "/* Script Removed for Wayback Machine replay */\n";

	public String getRemoveMsg() {
		return removeMsg;
	}

	public void setRemoveMsg(String removeMsg) {
		this.removeMsg = removeMsg;
	}

	public String rewrite(ReplayParseContext context, String policy,
			String input) {

		boolean rewrite = false;
		String scriptIndexStr = String.valueOf(context.getJSBlockCount());

		// Test against all params specified, ex. disable-script(0,1)
		// will test to see if script block is 0 or 1 and if so, perform rewrite
		int index = policy.indexOf('[', getName().length());

		if (index >= 0) {
			StringTokenizer tokens = new StringTokenizer(
				policy.substring(index + 1), " ]");
			while (tokens.hasMoreTokens()) {
				if (tokens.nextToken().equals(scriptIndexStr)) {
					rewrite = true;
					break;
				}
			}
		} else {
			//TODO: Do nothing? Disable first script block?
		}

		if (rewrite) {
			LOGGER.info(policy + ": disable script block " + scriptIndexStr +
					", replacing it with \"" + removeMsg + "\"");
			input = removeMsg;
		}

		return input;
	}
}
