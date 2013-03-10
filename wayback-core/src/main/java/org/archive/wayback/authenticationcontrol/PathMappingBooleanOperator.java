package org.archive.wayback.authenticationcontrol;

import java.util.Map;

import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.operator.BooleanOperator;
import org.archive.wayback.webapp.AccessPoint;

public class PathMappingBooleanOperator implements BooleanOperator<WaybackRequest> {
	
	protected Map<String, BooleanOperator<WaybackRequest>> pathMap;
	
	protected boolean noMatchesDefault = true;

	@Override
	public boolean isTrue(WaybackRequest value) {
		AccessPoint ap = value.getAccessPoint();
		
		String path = ap.getAccessPointPath();
		
		if (path == null) {
			return noMatchesDefault;
		}
		
		BooleanOperator<WaybackRequest> op = pathMap.get(path);
		
		if (op == null) {
			return noMatchesDefault;
		}
		
		return op.isTrue(value);
	}

	public Map<String, BooleanOperator<WaybackRequest>> getPathMap() {
		return pathMap;
	}

	public void setPathMap(Map<String, BooleanOperator<WaybackRequest>> pathMap) {
		this.pathMap = pathMap;
	}

	public boolean isNoMatchesDefault() {
		return noMatchesDefault;
	}

	public void setNoMatchesDefault(boolean noMatchesDefault) {
		this.noMatchesDefault = noMatchesDefault;
	}
}
