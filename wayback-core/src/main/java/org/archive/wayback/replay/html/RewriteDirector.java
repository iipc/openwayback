/**
 * 
 */
package org.archive.wayback.replay.html;

import org.archive.wayback.accesscontrol.CollectionContext;
import org.archive.wayback.core.CaptureSearchResult;

/**
 * {@code RewriteDirector} returns string representing rewrite rules to be
 * applied to the resource being replayed.
 */
public interface RewriteDirector {
	/**
	 * Return rewrite directive for {@code capture}.
	 * @param context TODO
	 * @param capture A capture to be rewritten
	 * @return rewrite directive (list of rule references)
	 */
	public String getRewriteDirective(CollectionContext context, CaptureSearchResult capture);
}
