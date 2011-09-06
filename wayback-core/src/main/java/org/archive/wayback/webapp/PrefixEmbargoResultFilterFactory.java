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
