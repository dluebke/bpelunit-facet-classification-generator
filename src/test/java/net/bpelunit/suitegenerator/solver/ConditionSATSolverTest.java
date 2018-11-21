package net.bpelunit.suitegenerator.solver;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionParser;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;
import net.bpelunit.suitegenerator.reader.ICodeFragmentReader;
import net.bpelunit.suitegenerator.reader.ReaderFactory;
import net.bpelunit.suitegenerator.reader.XLSReader;
import net.bpelunit.suitegenerator.statistics.Statistics;

public class ConditionSATSolverTest {

	@Test
	public void testForbidden1() {
		ConditionBundle c = new ConditionBundle();
		ConditionParser conditionParser = new ConditionParser();
		c.addCondition(conditionParser.parse("ApprovalConfig:4Eye AND 2ndApprover:None"));
		c.addCondition(conditionParser.parse("NOT ApprovalConfig:4Eye AND NOT 2ndApprover:None"));
		c.addCondition(conditionParser.parse("ProcessStart:NoSuccess:NoApprovalConfigForDepot XOR ApprovalConfig:NoApproval"));
		Map<ClassificationVariable, List<?extends IOperand>> allVariablesWithValues = createClassificationTree();
		ConditionSATSolver solver = new ConditionSATSolver(c, allVariablesWithValues);
		
		List<Literal> variablesChosen = Arrays.asList(new Literal("ProcessStart:NoSuccess:NoApprovalConfigForDepot"));
		
		assertFalse(solver.isAlwaysForbidden(variablesChosen));
	}
	
	@Test
	public void testForbidden2() {
		ConditionBundle c = new ConditionBundle();
		c.addCondition(new ConditionParser().parse("ApprovalConfig:4Eye AND 2ndApprover:None"));
		c.addCondition(new ConditionParser().parse("NOT ApprovalConfig:4Eye AND NOT 2ndApprover:None"));
		c.addCondition(new ConditionParser().parse("ProcessStart:NoSuccess:NoApprovalConfigForDepot XOR ApprovalConfig:NoApproval"));
		Map<ClassificationVariable, List<?extends IOperand>> allVariablesWithValues = createClassificationTree();
		ConditionSATSolver solver = new ConditionSATSolver(c, allVariablesWithValues);
		solver.enableLogging();
		
		List<Literal> variablesChosen = Arrays.asList(new Literal("ProcessStart:NoSuccess:NoApprovalConfigForDepot"), new Literal("2ndApprover:Rejects"));
		
		assertTrue(solver.isAlwaysForbidden(variablesChosen));
	}
	
	@Test
	public void testForbidden3() {
		ConditionBundle c = new ConditionBundle();
		c.addCondition(new ConditionParser().parse("ApprovalConfig:4Eye AND 2ndApprover:None"));
		c.addCondition(new ConditionParser().parse("NOT ApprovalConfig:4Eye AND NOT 2ndApprover:None"));
		c.addCondition(new ConditionParser().parse("ProcessStart:NoSuccess:NoApprovalConfigForDepot XOR ApprovalConfig:NoApproval"));
		Map<ClassificationVariable, List<?extends IOperand>> allVariablesWithValues = createClassificationTree();
		ConditionSATSolver solver = new ConditionSATSolver(c, allVariablesWithValues);
		solver.enableLogging();
		
		List<Literal> variablesChosen = Arrays.asList(new Literal("ProcessStart:NoSuccess:NoApprovalConfigForDepot"), new Literal("2ndApprover:Rejects"), new Literal("ApprovalConfig:NoApproval"));
		
		assertTrue(solver.isAlwaysForbidden(variablesChosen));
	}
	
	@Test
	public void testForbiddenNotOneSpecificVariable() {
		ConditionBundle c = new ConditionBundle();
		ConditionParser conditionParser = new ConditionParser();
		c.addCondition(conditionParser.parse("ApprovalConfig:4Eye"));
		Map<ClassificationVariable, List<?extends IOperand>> allVariablesWithValues = createClassificationTree();
		ConditionSATSolver solver = new ConditionSATSolver(c, allVariablesWithValues);
		solver.enableLogging();
		
		List<Literal> variablesChosen = Arrays.asList(new Literal("ApprovalConfig:4Eye"));
		
		assertTrue(solver.isAlwaysForbidden(variablesChosen));
	}

	
	@Test
	public void testLargeProblem() throws Exception {
		
		ICondition c = new ConditionParser().parse("(V1:A OR V2:B) AND (V2:B OR V3:C) AND (V3:C OR V4:D) AND (V4:D OR V5:E) AND (V5:E OR V6:F) AND (V6:F OR V7:G) AND (V7:G OR V8:H) AND (V8:H OR V9:I) AND (V9:I OR V10:J)");
		
		Map<ClassificationVariable, List<?extends IOperand>> tree = new HashMap<>();
		ClassificationVariable property;
		List<Literal> values;

		for(int i = 1; i <= 10; i++) {		
			property = new ClassificationVariable("V" + i, true);
			values = new ArrayList<>();
			tree.put(property, values);
			
			for(String value : new String[]{ "A", "B", "C", "D", "E", "F", "G", "H", "I", "J"}) {
				values.add(new Literal("V" + i + ":" + value));
			}
		}
		
		Random random = new Random(1234567);
		List<List<?extends IOperand>> chosenValuesList = new ArrayList<>();
		List<ClassificationVariable> classificationVariables = new ArrayList<>(tree.keySet());
		for(int i = 0; i < 100000; i++) {
			int x = random.nextInt(classificationVariables.size());
			List<IOperand> chosenValues = new ArrayList<>(x);
			chosenValuesList.add(chosenValues);
			
			for(int j = 0; j < x; j++) {
				ClassificationVariable var = classificationVariables.get(j);
				List<?extends IOperand> selections = tree.get(var);
				int k = random.nextInt(selections.size());
				chosenValues.add(selections.get(k));
			}
		}
		
		ConditionSATSolver solver = new ConditionSATSolver(c, tree);
		long start = System.currentTimeMillis();
		for(List<?extends IOperand> chosenValues : chosenValuesList) {
			solver.isAlwaysForbidden(chosenValues);
		}
		long end = System.currentTimeMillis();
		
		System.out.println((end-start) + "ms");
	}
	
	private Map<ClassificationVariable, List<?extends IOperand>> createClassificationTree() {
		Map<ClassificationVariable, List<?extends IOperand>> result = new HashMap<>();
		
		ClassificationVariable property;
		List<Literal> values;
		
		property = new ClassificationVariable("ProcessStart", true);
		values = new ArrayList<>();
		result.put(property, values);
		values.add(new Literal("ProcessStart:NoSuccess:NotAllowed"));
		values.add(new Literal("ProcessStart:NoSuccess:NoApprovalConfigForDepot"));
		values.add(new Literal("ProcessStart:NoSuccess:NoAssetsToApprove"));
		values.add(new Literal("ProcessStart:Success"));
		
		property = new ClassificationVariable("ApprovalConfig", true);
		values = new ArrayList<>();
		result.put(property, values);
		values.add(new Literal("ApprovalConfig:NoApproval"));
		values.add(new Literal("ApprovalConfig:2Eye"));
		values.add(new Literal("ApprovalConfig:4Eye"));
		
		property = new ClassificationVariable("2ndApprover", true);
		values = new ArrayList<>();
		result.put(property, values);
		values.add(new Literal("2ndApprover:None"));
		values.add(new Literal("2ndApprover:Accepts"));
		values.add(new Literal("2ndApprover:Rejects"));
		
		return result;
	}

}
