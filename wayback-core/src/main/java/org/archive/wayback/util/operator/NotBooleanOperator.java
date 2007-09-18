package org.archive.wayback.util.operator;


public class NotBooleanOperator<E> extends UnaryBooleanOperator<E> {
	public boolean isTrue(E value) {
		return !operand.isTrue(value);
	}
}
