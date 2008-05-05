package org.archive.wayback.authenticationcontrol;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.operator.BooleanOperator;

public class AccessControlSettingOperation implements BooleanOperator<WaybackRequest> {

	private ExclusionFilterFactory factory = null;
	private BooleanOperator<WaybackRequest> operator = null;
	
	public boolean isTrue(WaybackRequest value) {
		if(operator.isTrue(value)) {
			value.setExclusionFilter(factory.get());
		}
		return true;
	}

	public ExclusionFilterFactory getFactory() {
		return factory;
	}

	public void setFactory(ExclusionFilterFactory factory) {
		this.factory = factory;
	}

	public BooleanOperator<WaybackRequest> getOperator() {
		return operator;
	}

	public void setOperator(BooleanOperator<WaybackRequest> operator) {
		this.operator = operator;
	}
}
