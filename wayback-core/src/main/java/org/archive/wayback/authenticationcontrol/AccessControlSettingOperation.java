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
package org.archive.wayback.authenticationcontrol;

import org.archive.wayback.accesscontrol.ExclusionFilterFactory;
import org.archive.wayback.core.WaybackRequest;
import org.archive.wayback.util.operator.BooleanOperator;

/**
 * BooleanOperator which tests a delegate operator, and sets an 
 * ExclusionFilterFactory on the WaybackRequest if the delegate is true.
 * 
 * @author brad
 *
 */
public class AccessControlSettingOperation implements BooleanOperator<WaybackRequest> {

	private ExclusionFilterFactory factory = null;
	private BooleanOperator<WaybackRequest> operator = null;
	
	public boolean isTrue(WaybackRequest value) {
		if(operator.isTrue(value)) {
			value.setExclusionFilter(factory.get());
		}
		return true;
	}

	/**
	 * @return ExclusionFilterFactory which will be set on operator matches
	 */
	public ExclusionFilterFactory getFactory() {
		return factory;
	}

	/**
	 * @param factory ExclusionFilterFactory which will be set on operator 
	 * matches
	 */
	public void setFactory(ExclusionFilterFactory factory) {
		this.factory = factory;
	}

	/**
	 * @return the BooleanOperator delegate which determines if the factory
	 * 			is applied
	 */
	public BooleanOperator<WaybackRequest> getOperator() {
		return operator;
	}

	/**
	 * @param operator the BooleanOperator delegate which determines if the 
	 * 			factory is applied
	 */
	public void setOperator(BooleanOperator<WaybackRequest> operator) {
		this.operator = operator;
	}
}
