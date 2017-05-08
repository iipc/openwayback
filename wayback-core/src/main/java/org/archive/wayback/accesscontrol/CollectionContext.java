/**
 *
 */
package org.archive.wayback.accesscontrol;

/**
 * View of exclusion context for ExclusionFilterFactory.
 */
public interface CollectionContext {
	/**
	 * Return name of this exclusion context.
	 * @return the name of this exclusion context
	 */
	public String getCollectionContextName();
}
