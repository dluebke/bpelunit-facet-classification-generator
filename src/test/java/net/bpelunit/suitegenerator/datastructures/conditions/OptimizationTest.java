package net.bpelunit.suitegenerator.datastructures.conditions;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import net.bpelunit.suitegenerator.datastructures.conditions.OptimizationTest.DummyOperand;

public class OptimizationTest {

	public static class DummyOperand implements IOperand {

		private String name;
		
		public DummyOperand(String name) {
			this.name = name;
		}
		
		@Override
		public String getOpName() {
			return name;
		}

		@Override
		public String getVariableName() {
			return name.split(":")[0];
		}
	}
	
	@Test
	public void testLiteral() {
		OperandCondition opA = new OperandCondition("A:A");
		assertSame(opA, opA.optimize(Arrays.asList(new DummyOperand("B:A"))));
		assertTrue(opA.optimize(Arrays.asList(new DummyOperand("A:A"))) instanceof TRUE);
		
		OperandCondition opB = new OperandCondition("A:B");
		assertSame(opB, opB.optimize(Arrays.asList(new DummyOperand("B:A"))));
		assertTrue(opB.optimize(Arrays.asList(new DummyOperand("A:A"))) instanceof FALSE);
	}

	@Test
	public void testOptimizeAnd() {
		OperandCondition op1 = new OperandCondition("A:A");
		OperandCondition op2 = new OperandCondition("B:A");
		AND andOriginal = new AND(op1, op2);
		AND and;
		
		and = andOriginal.clone();
		assertSame(and, and.optimize(Arrays.asList(new DummyOperand("A:A"))));
		and = andOriginal.clone();
		assertSame(and, and.optimize(Arrays.asList(new DummyOperand("B:A"))));
		and = andOriginal.clone();
		and = andOriginal.clone();
		assertSame(and, and.optimize(Arrays.asList(new DummyOperand("A:A"), new DummyOperand("C"))));
		and = andOriginal.clone();
		assertTrue(and.optimize(Arrays.asList(new DummyOperand("A:A"), new DummyOperand("B:A"))) instanceof TRUE);
	}
	
	@Test
	public void testOptimizeOr() {
		OperandCondition op1 = new OperandCondition("A:A");
		OperandCondition op2 = new OperandCondition("B:A");
		OR orOriginal = new OR(op1, op2);
		OR or;
		
		or = orOriginal.clone();
		assertTrue(or.optimize(Arrays.asList(new DummyOperand("A:A"))) instanceof TRUE);
		or = orOriginal.clone();
		assertTrue(or.optimize(Arrays.asList(new DummyOperand("B:A"))) instanceof TRUE);
		or = orOriginal.clone();
		or = orOriginal.clone();
		assertSame(or, or.optimize(Arrays.asList(new DummyOperand("C:A"), new DummyOperand("D"))));
		or = orOriginal.clone();
		assertTrue(or.optimize(Arrays.asList(new DummyOperand("A:B"), new DummyOperand("B:B"))) instanceof FALSE);
	}
	
	@Test
	public void testIsAlwaysTrueFalse() {
		assertTrue(new TRUE().isAlwaysTrue());
		assertFalse(new TRUE().isAlwaysFalse());
		assertTrue(new FALSE().isAlwaysFalse());
		assertFalse(new FALSE().isAlwaysTrue());
		
		ICondition op1 = new TRUE();
		OperandCondition op2 = new OperandCondition("B:A");
		OR orOriginal = new OR(op1, op2);
		assertTrue(orOriginal.isAlwaysTrue());
		assertFalse(orOriginal.isAlwaysFalse());
	}
}
