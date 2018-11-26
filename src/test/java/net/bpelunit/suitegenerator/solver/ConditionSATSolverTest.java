package net.bpelunit.suitegenerator.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.bpelunit.suitegenerator.datastructures.classification.Classification;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationTree;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariableSelection;
import net.bpelunit.suitegenerator.datastructures.classification.IClassificationElement;
import net.bpelunit.suitegenerator.datastructures.conditions.AND;
import net.bpelunit.suitegenerator.datastructures.conditions.FALSE;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;
import net.bpelunit.suitegenerator.datastructures.conditions.OR;
import net.bpelunit.suitegenerator.datastructures.conditions.OperandCondition;
import net.bpelunit.suitegenerator.recommendation.IRecommenderStatusListener;
import net.bpelunit.suitegenerator.recommendation.aetg.Aetg;
import net.bpelunit.suitegenerator.statistics.IStatistics;
import net.bpelunit.suitegenerator.statistics.Statistics;

public class ConditionSATSolverTest {

	@Test
	public void testGenerate2Tupels() {
		ClassificationTree tree = new ClassificationTree();
		ClassificationVariable a = new ClassificationVariable("A");
		tree.addChild(a);
		a.addChild(new ClassificationVariableSelection("A:A1"));
		a.addChild(new ClassificationVariableSelection("A:A2"));
		ClassificationVariable b = new ClassificationVariable("B");
		tree.addChild(b);
		b.addChild(new ClassificationVariableSelection("B:B1"));
		b.addChild(new ClassificationVariableSelection("B:B2"));
		ClassificationVariable c = new ClassificationVariable("C");
		tree.addChild(c);
		c.addChild(new ClassificationVariableSelection("C:C1"));
		c.addChild(new ClassificationVariableSelection("C:C2"));
		
		
		ConditionSATSolver solver = new ConditionSATSolver();
		List<List<?extends IOperand>> pairs = solver.generateAllValidTupels(createLeafsByRoot(Arrays.asList(a, b, c)), FALSE.INSTANCE, 2);
		System.out.println(pairs);
	}
	
	@Test
	public void testGenerate2TupelsWithConstraints() {
		List<ClassificationVariableSelection> variableSelections = new ArrayList<>();
		ClassificationTree tree = new ClassificationTree();
		for(String name : Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")) {
			ClassificationVariable classificationVariable = new ClassificationVariable(name, true);
			tree.addChild(classificationVariable);
			for(String value : Arrays.asList("1", "2", "3", "4", "5", "6")) {
				ClassificationVariableSelection s = new ClassificationVariableSelection(name + ":" + name + value);
				classificationVariable.addChild(s);
				variableSelections.add(s);
			}
		}
		
		ConditionSATSolver solver = new ConditionSATSolver();
		
		ICondition forbidden = new OR(new OperandCondition("B:B2"), new AND(new OperandCondition("A:A1"), new OperandCondition("C:C2")));
		
		long start = System.currentTimeMillis();
		List<List<?extends IOperand>> pairs = solver.generateAllValidTupels(createLeafsByRoot(tree.getChildren()), forbidden, 2);
		long end = System.currentTimeMillis();
		System.out.println(pairs.size() + " tupels in "+ (end - start) + "ms");
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
