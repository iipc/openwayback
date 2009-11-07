/* Utils
 *
 * $Id$
 *
 * Created on Nov 7, 2009.
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
