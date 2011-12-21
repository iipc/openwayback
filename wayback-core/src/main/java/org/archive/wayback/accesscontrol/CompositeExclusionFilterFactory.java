/*
 *  This file is part of the Wayback archival access software
 *   (http://archive-access.sourceforge.net/projects/wayback/).
 *
 *  Licensed to the Internet Archive (IA) by one or more individual 
 *  contributors. 
 *
 *  The IA licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.archive.wayback.accesscontrol;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import org.archive.wayback.accesscontrol.staticmap.StaticMapExclusionFilterFactory;
import org.archive.wayback.resourceindex.filters.CompositeExclusionFilter;
import org.archive.wayback.resourceindex.filters.ExclusionFilter;

/**
 * Class that provides SearchResult Filtering based on multiple
 * ExclusionFilterFactory instances by returning a single composite
 * SearchResultFilter based on the results of each ExclusionFilter.
 *
 * @author brad
 * @version $Date$, $Revision$
 */
public class CompositeExclusionFilterFactory implements ExclusionFilterFactory {

	private static final Logger LOGGER =
        Logger.getLogger(CompositeExclusionFilterFactory.class.getName());
	
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
	public ExclusionFilter get() {
		Iterator<ExclusionFilterFactory> itr = factories.iterator();
		CompositeExclusionFilter filter = new CompositeExclusionFilter();
		while(itr.hasNext()) {
			ExclusionFilterFactory factory = itr.next();
			ExclusionFilter filterEntry = factory.get();
			if (filterEntry != null) {
				filter.addComponent(filterEntry);
			} else {
				LOGGER.warning("Skipping null filter returned from factory: " + factory.getClass().toString());
			}
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
