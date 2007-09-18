package org.archive.wayback.util.operator;

public class OrBooleanOperator<E> extends BinaryBooleanOperator<E> {

	public boolean isTrue(E value) {
		if(operand1 == null) return false;
		if(operand2 == null) return false;
		return operand1.isTrue(value) || operand2.isTrue(value);
	}
}
