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
