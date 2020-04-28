package net.bpelunit.suitegenerator.solver;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationTree;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariableSelection;
import net.bpelunit.suitegenerator.datastructures.classification.IClassificationElement;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.FALSE;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;
import net.bpelunit.suitegenerator.datastructures.conditions.NOT;
import net.bpelunit.suitegenerator.datastructures.conditions.OperandCondition;
import net.bpelunit.suitegenerator.datastructures.conditions.TRUE;
import net.bpelunit.suitegenerator.datastructures.conditions.XOR;

public class PairGeneratorTest {

	@Test(expected=IllegalArgumentException.class)
	public void testPairsOnly() throws Exception {
		new PairGenerator().generateAllValidTupels(null, null, 3);
	}
	
	@Test
	public void testPairsNoForbidden() {
		ClassificationTree t = new ClassificationTree();
		addClassificationVariable(t, "A", "A1", "A2");
		addClassificationVariable(t, "B", "B1", "B2");
		addClassificationVariable(t, "C", "C1", "C2");
		
		PairGenerator pg = new PairGenerator();
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(FALSE.INSTANCE);
		List<List<?extends IOperand>> pairs = pg.generateAllValidTupels(createLeafsByRoot(t.getChildren()), forbidden, 2);
		assertEquals(pairs.toString(), 2*(2+2)+2*2, pairs.size());
	}
	
	@Test
	public void testPairsOneValueForbidden() {
		ClassificationTree t = new ClassificationTree();
		addClassificationVariable(t, "A", "A1", "A2");
		addClassificationVariable(t, "B", "B1", "B2");
		addClassificationVariable(t, "C", "C1", "C2");
		
		PairGenerator pg = new PairGenerator();
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(new NOT(new OperandCondition("A:A1")));
		List<List<?extends IOperand>> pairs = pg.generateAllValidTupels(createLeafsByRoot(t.getChildren()), forbidden, 2);
		assertEquals(pairs.toString(), 1*(2+2)+2*2, pairs.size());
	}
	
	@Test
	public void testPairsTwoVariablesForbidden() {
		ClassificationTree t = new ClassificationTree();
		addClassificationVariable(t, "A", "A1", "A2");
		addClassificationVariable(t, "B", "B1", "B2");
		addClassificationVariable(t, "C", "C1", "C2");
		
		PairGenerator pg = new PairGenerator();
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(new XOR(new OperandCondition("A:A1"), new OperandCondition("B:B1")));
		List<List<?extends IOperand>> pairs = pg.generateAllValidTupels(createLeafsByRoot(t.getChildren()), forbidden, 2);
		System.out.println(pairs.toString());
		assertEquals(pairs.toString(), 2*(2+2)+2*2 - 2, pairs.size());
	}
	
	@Test
	public void testPairsThreeVariablesForbidden() {
		ClassificationTree t = new ClassificationTree();
		addClassificationVariable(t, "A", "A1", "A2");
		addClassificationVariable(t, "B", "B1", "B2");
		addClassificationVariable(t, "C", "C1", "C2");
		
		PairGenerator pg = new PairGenerator();
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(new XOR(new OperandCondition("A:A1"), new OperandCondition("B:B1")));
		forbidden.addCondition(new XOR(new OperandCondition("A:A1"), new OperandCondition("C:C1")));

		List<List<?extends IOperand>> pairs = pg.generateAllValidTupels(createLeafsByRoot(t.getChildren()), forbidden, 2);
		System.out.println(pairs.toString());
		assertEquals(pairs.toString(), 8, pairs.size());
	}
	
	@Test
	public void testPairsAllForbidden() {
		ClassificationTree t = new ClassificationTree();
		addClassificationVariable(t, "A", "A1", "A2");
		addClassificationVariable(t, "B", "B1", "B2");
		addClassificationVariable(t, "C", "C1", "C2");
		
		PairGenerator pg = new PairGenerator();
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(TRUE.INSTANCE);
		List<List<?extends IOperand>> pairs = pg.generateAllValidTupels(createLeafsByRoot(t.getChildren()), forbidden, 2);
		assertEquals(pairs.toString(), 0, pairs.size());
	}

	@Test
	public void testLargePairwiseConnectedClassification() throws Exception {
		ClassificationTree t = new ClassificationTree();
		ConditionBundle forbidden = new ConditionBundle();
		
		for(char varName = 'A'; varName < 'Z'; varName++) {
			addClassificationVariable(t, varName + "", "NotSpecified", "A", "B", "C", "D", "E", "F");
			addClassificationVariable(t, varName + "" + varName, "NA", "AA", "BB", "CC", "DD", "EE", "FF");
			forbidden.addCondition(new XOR(
				new OperandCondition(varName + ":NotSpecified"),
				new OperandCondition(varName + "" + varName + ":NA")
			));
		}
		
		PairGenerator pg = new PairGenerator();
		pg.generateAllValidTupels(createLeafsByRoot(t.getChildren()), forbidden, 2);
		
		// assertion is timely completion
	}
	
	private void addClassificationVariable(ClassificationTree t, String name, String... values) {
		ClassificationVariable a = new ClassificationVariable(name);
		t.addChild(a);
		for(String value : values) {
			a.addChild(new ClassificationVariableSelection(name + ":" + value));
		}
	}
	
	private Map<ClassificationVariable, List<? extends IOperand>> createLeafsByRoot(List<IClassificationElement> classificationElements) {
		Map<ClassificationVariable, List<? extends IOperand>> result = new HashMap<>();
		
		for(IClassificationElement cv : classificationElements) {
			List<IOperand> list = new ArrayList<>();
			for (IClassificationElement ce : cv.getChildren()) {
				list.add(new Literal(ce.getName()));
			}
			result.put((ClassificationVariable) cv, list);
		}
		
		return result;
	}
}
