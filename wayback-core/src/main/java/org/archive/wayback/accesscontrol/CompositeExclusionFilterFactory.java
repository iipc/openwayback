/* CompositeExclusionFilterFactory
 *
 * $Id$
 *
 * Created on 4:53:58 PM Mar 19, 2007.
 *
 * Copyright (C) 2007 Internet Archive.
 *
 * This file is part of wayback.
 *
 * wayback is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * wayback is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with wayback-svn; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.archive.wayback.accesscontrol;

import java.util.ArrayList;
import java.util.Iterator;

import org.archive.wayback.core.SearchResult;
import org.archive.wayback.resourceindex.filters.CompositeExclusionFilter;
import org.archive.wayback.util.ObjectFilter;

/**
 * Class that provides SearchResult Filtering based on multiple
 * ExclusionFilterFactory instances by returning a single composite
 * SearchResultFilter based on the results of each ExclusionFilter.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CompositeExclusionFilterFactory implements ExclusionFilterFactory {

	private ArrayList<ExclusionFilterFactory> factories = 
		new ArrayList<ExclusionFilterFactory>();
	
	/**
	 * @param factory to be added to the composite
	 */
	public void addFactory(ExclusionFilterFactory factory) {
		factories.add(factory);
	}
	
	/* (non-Javadoc)
	 * @see org.archive.wayback.resourceindex.ExclusionFilterFactory#get()
	 */
	public ObjectFilter<SearchResult> get() {
		Iterator<ExclusionFilterFactory> itr = factories.iterator();
		CompositeExclusionFilter filter = new CompositeExclusionFilter();
		while(itr.hasNext()) {
			filter.addComponent(itr.next().get());
		}
		return filter;
	}


	/**
	 * @return the factories
	 */
	public ArrayList<ExclusionFilterFactory> getFactories() {
		return factories;
	}


	/**
	 * @param factories the factories to set
	 */
	public void setFactories(ArrayList<ExclusionFilterFactory> factories) {
		this.factories = factories;
	}

	/* (non-Javadoc)
	 * @see org.archive.wayback.accesscontrol.ExclusionFilterFactory#shutdown()
	 */
	public void shutdown() {
		Iterator<ExclusionFilterFactory> itr = factories.iterator();
		while(itr.hasNext()) {
			ExclusionFilterFactory i = itr.next();
			i.shutdown();
		}
	}
}
