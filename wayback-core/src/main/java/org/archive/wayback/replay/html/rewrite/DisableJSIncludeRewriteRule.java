package org.archive.wayback.replay.html.rewrite;

import org.archive.wayback.archivalurl.FastArchivalUrlReplayParseEventHandler;
import org.archive.wayback.replay.html.ReplayParseContext;

/**
 * Special {@link RewriteRule} for disabling script inclusion
 * ({@code <SCRIPT SRC="..."></SCRIPT>}).
 * Policy name shall have script filename, followed by "{@code =}".
 * If {@code SRC} attribute value contains the filename,
 * {@code SCRIPT} tag will be disabled.
 * <p>
 * {@link FastArchivalUrlReplayParseEventHandler} has special
 * handling of {@code SCRIPT} tag. It passes the value of
 * {@code SRC} attribute as {@code inputSrc}. When this rule
 * returns {@code null} or empty string, {@code SCRIPT} tag
 * is disabled by replacing {@code SRC} attribute value with
 * empty text.
 * </p>
 * @see FastArchivalUrlReplayParseEventHandler
 * @see DisableJSBlocksRewriteRule
 * @see RewritingStringTransformer
 */
public class DisableJSIncludeRewriteRule extends RewriteRule {

	@Override
	public String rewrite(ReplayParseContext context, String policy,
			String inputSrc) {

		int index = policy.indexOf('=', getName().length());

		if (index >= 0) {
			String filename = policy.substring(index + 1);

			// If inputSrc contains filename, exclude it!
			if ((inputSrc != null) && inputSrc.indexOf(filename) >= 0) {
				return null;
			}
		}

		return inputSrc;
	}
}
