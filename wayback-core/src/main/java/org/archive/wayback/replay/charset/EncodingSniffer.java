/**
 * 
 */
package org.archive.wayback.replay.charset;

import org.archive.wayback.core.Resource;

/**
 * A step of character encoding sniffing.
 *
 */
public interface EncodingSniffer {
	public String sniff(Resource resource);
}
