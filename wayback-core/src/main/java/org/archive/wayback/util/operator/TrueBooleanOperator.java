package org.archive.wayback.util.operator;


public class TrueBooleanOperator<E> implements BooleanOperator<E> {
	public boolean isTrue(E value) {
		return true;
	}
}
