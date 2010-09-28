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

import junit.framework.TestCase;

public class BooleanOperatorTest extends TestCase {

	public void testAll() {
		FalseBooleanOperator<Object> fbo = new FalseBooleanOperator<Object>();
		TrueBooleanOperator<Object> tbo = new TrueBooleanOperator<Object>();
		NotBooleanOperator<Object> nbo = new NotBooleanOperator<Object>();
		OrBooleanOperator<Object> obo = new OrBooleanOperator<Object>();
		AndBooleanOperator<Object> abo = new AndBooleanOperator<Object>();

		assertFalse(fbo.isTrue(null));
		assertTrue(tbo.isTrue(null));
		nbo.setOperand(fbo);
		assertTrue(nbo.isTrue(null));
		nbo.setOperand(tbo);
		assertFalse(nbo.isTrue(null));
		obo.setOperand1(fbo);
		obo.setOperand2(tbo);
		assertTrue(obo.isTrue(null));

		abo.setOperand1(fbo);
		abo.setOperand2(tbo);
		assertFalse(abo.isTrue(null));
		
		abo.setOperand1(tbo);
		assertTrue(abo.isTrue(null));
		
		obo.setOperand2(fbo);
		assertFalse(obo.isTrue(null));
		
		abo.setOperand1(nbo);
		nbo.setOperand(fbo);
		assertTrue(abo.isTrue(null));
		
		
	}

}
