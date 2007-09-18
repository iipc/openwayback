package org.archive.wayback.util.operator;


public abstract class UnaryBooleanOperator<E> implements BooleanOperator<E> {
	protected BooleanOperator<E> operand = null;
	public BooleanOperator<E> getOperand() {
		return operand;
	}
	public void setOperand(BooleanOperator<E> operand) {
		this.operand = operand;
	}
}
