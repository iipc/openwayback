package org.archive.wayback.webapp;

import java.util.List;

import org.archive.wayback.core.CaptureSearchResult;
import org.archive.wayback.util.ObjectFilter;
import org.archive.wayback.util.ObjectFilterChain;

public class CustomMultiFilterFactory implements CustomResultFilterFactory {
	
	List<CustomResultFilterFactory> filters;

	public List<CustomResultFilterFactory> getFilters() {
		return this.filters;
	}

	public void setFilters(List<CustomResultFilterFactory> filters) {
		this.filters = filters;
	}

	@Override
	public ObjectFilter<CaptureSearchResult> get(AccessPoint ap) {
		final ObjectFilterChain<CaptureSearchResult> chain = new ObjectFilterChain<CaptureSearchResult>();
		
		for (CustomResultFilterFactory factory : filters) {
			chain.addFilter(factory.get(ap));
		}
		
		return new ObjectFilter<CaptureSearchResult>()
		{
			@Override
			public int filterObject(CaptureSearchResult o) {
				return chain.filterObject(o);
			}
		};
	}
}
