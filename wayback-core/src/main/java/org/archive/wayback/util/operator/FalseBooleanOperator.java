package org.archive.wayback.util.operator;


public class FalseBooleanOperator<E> implements BooleanOperator<E> {
	public boolean isTrue(E value) {
		return false;
	}
}
