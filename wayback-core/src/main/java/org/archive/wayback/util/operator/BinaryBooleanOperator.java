/* BinaryBooleanOperator
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
