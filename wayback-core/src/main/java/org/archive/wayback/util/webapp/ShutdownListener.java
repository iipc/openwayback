/**
 * 
 */
package org.archive.wayback.util.webapp;

/**
 * Interface representing a desire to be notified when the containing
 * ServletContext is being destroyed.
 * 
 * @author brad
 *
 */
public interface ShutdownListener {
	/**
	 * Called when the ServletContext is being destroyed.
	 */
	public void shutdown();
}
