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
