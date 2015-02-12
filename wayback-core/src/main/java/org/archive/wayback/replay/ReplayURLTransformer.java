/**
 * 
 */
package org.archive.wayback.replay;

import org.archive.wayback.ReplayRenderer;
import org.archive.wayback.ReplayURIConverter;
import org.archive.wayback.ResultURIConverter;
import org.archive.wayback.replay.html.ReplayParseContext;

/**
 * An extension point for rewriting URLs found in a resource being replayed.
 * {@link ReplayRenderer} implementations call {@code translate} method on
 * each URL found in a resource being replayed
 * (through {@link ReplayParseContext#contextualizeUrl(String, String)}).
 * <p>
 * While this interface is defined as a separate
 * class for better distinction between URI projection and URL rewriting,
 * standard {@link ReplayURIConverter} implementations also provide baseline
 * implementation of this interface, and current ReplayParseContext code
 * assumes such implementation.  Primary reason for this design is that
 * instance of this interface must be passed as {@link ResultURIConverter}
 * so as not to break interface compatibility, for the time being.
 * While we may way want to change this design in future major release,
 * it may be useful to have two implementation close to each other.
 * </p>
 * @see ReplayRenderer
 * @see ReplayURIConverter
 */
public interface ReplayURLTransformer {
	/**
	 * Return URL for replaying {@code url} found in resource context
	 * {@code replayContext}, with replay context flags {@code contextFlags}.
	 * @param replayContext
	 * @param url URL, possibly in relative form.
	 * @param contextFlags replay context flags such as "{@code im_}"
	 * @return URL rewritten for replay
	 */
	public String transform(ReplayContext replayContext, String url,
			String contextFlags);
}
