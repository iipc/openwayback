/**
 * 
 */
package org.archive.wayback.accesscontrol;

import org.archive.wayback.resourceindex.filters.ExclusionFilter;

/**
 * Extended {@link ExclusionFilterFactory} interface for configuring
 * {@link ExclusionFilter} instance depending on context information.
 * <p>
 * If an {@code ExclusionFilterFactory} instance also implements this interface,
 * and if appropriate {@code ExclusionContext} is available, caller shall
 * {@link #getExclusionFilter(CollectionContext)} method instead of
 * {@link ExclusionFilterFactory#get()}. This is for migrating to new interface
 * maintaining compatibility with existing code. Eventually all
 * {@code ExclusionFilterFactory} implementation should migrate to this
 * interface.
 * </p>
 * <p>
 * Typically context corresponds to one of web archive collection hosted by the
 * same Wayback instance.
 * </p>
 */
public interface ContextExclusionFilterFactory extends ExclusionFilterFactory {
	/**
	 * Return {@link ExclusionFilter} for use in current replay session, based
	 * on {@code context}.
	 * @param context exclusion context view, must not be {@code null}.
	 * @return ExclusionFilter for current replay session.
	 */
	public ExclusionFilter getExclusionFilter(CollectionContext context);
}
