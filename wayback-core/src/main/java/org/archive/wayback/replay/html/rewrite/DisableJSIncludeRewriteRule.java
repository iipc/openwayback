package org.archive.wayback.replay.html.rewrite;

import org.archive.wayback.replay.html.ReplayParseContext;

public class DisableJSIncludeRewriteRule extends RewriteRule {
	
	@Override
	public String rewrite(ReplayParseContext context, String policy,
			String inputSrc) {
		
		int index = policy.indexOf('=', getBeanName().length());
		
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
