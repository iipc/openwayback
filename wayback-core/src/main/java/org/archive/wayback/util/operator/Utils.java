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
package org.archive.wayback.util.operator;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	@SuppressWarnings("unchecked")
	public static <T> List<BooleanOperator<T>> getOperators(BooleanOperator<T> top) {
		ArrayList<BooleanOperator<T>> operators = new ArrayList<BooleanOperator<T>>();
		ArrayList<BooleanOperator<T>> toInspect = new ArrayList<BooleanOperator<T>>();
		toInspect.add(top);
		while(toInspect.size() > 0) {
			BooleanOperator<T> current = toInspect.remove(0);
			operators.add(current);
			if(current instanceof UnaryBooleanOperator) {
				toInspect.add(((UnaryBooleanOperator<T>)current).getOperand());
			} else if(current instanceof BinaryBooleanOperator) {
				toInspect.add(((BinaryBooleanOperator<T>)current).getOperand1());				
				toInspect.add(((BinaryBooleanOperator<T>)current).getOperand2());				
			}
		}
		return operators;
	}
}
