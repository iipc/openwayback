package org.archive.wayback.util.operator;


public abstract class BinaryBooleanOperator<E> implements BooleanOperator<E> {
	protected BooleanOperator<E> operand1 = null;
	protected BooleanOperator<E> operand2 = null;
	public BooleanOperator<E> getOperand1() {
		return operand1;
	}
	public void setOperand1(BooleanOperator<E> operand1) {
		this.operand1 = operand1;
	}
	public BooleanOperator<E> getOperand2() {
		return operand2;
	}
	public void setOperand2(BooleanOperator<E> operand2) {
		this.operand2 = operand2;
	}
}
