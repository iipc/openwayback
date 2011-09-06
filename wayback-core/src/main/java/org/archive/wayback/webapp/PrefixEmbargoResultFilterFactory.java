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
package org.archive.wayback.webapp;

import java.util.ArrayList;
import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.resourceindex.filters.CompositeFilter;
import org.archive.wayback.resourceindex.filters.FilePrefixDateEmbargoFilter;
import org.archive.wayback.util.ObjectFilter;

public class PrefixEmbargoResultFilterFactory implements CustomResultFilterFactory {
	protected List<PrefixEmbargoPeriodTuple> tuples = null;
	public ObjectFilter<CaptureSearchResult> get(AccessPoint ap) {
		if(tuples == null) {
			return null;
		}
		CompositeFilter composite = new CompositeFilter();
		ArrayList<ObjectFilter<CaptureSearchResult>> filters = 
			new ArrayList<ObjectFilter<CaptureSearchResult>>();
		for(PrefixEmbargoPeriodTuple tuple : tuples) {
			filters.add(new FilePrefixDateEmbargoFilter(tuple.getPrefix(), 
					tuple.getEmbargoMS()));
		}
		composite.setFilters(filters);
		return composite;
	}
	/**
	 * @return the tuples
	 */
	public List<PrefixEmbargoPeriodTuple> getTuples() {
		return tuples;
	}
	/**
	 * @param tuples the tuples to set
	 */
	public void setTuples(List<PrefixEmbargoPeriodTuple> tuples) {
		this.tuples = tuples;
	}

}
