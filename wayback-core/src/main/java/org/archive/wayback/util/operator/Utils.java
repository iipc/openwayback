package org.archive.wayback.util.operator;

import java.util.ArrayList;
import java.util.List;

public class Utils {
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
