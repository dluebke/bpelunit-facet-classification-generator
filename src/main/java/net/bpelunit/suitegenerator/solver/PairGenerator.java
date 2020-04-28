package net.bpelunit.suitegenerator.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;

public class PairGenerator implements ITupelGenerator {

	private ConditionSATSolver satSolver = new ConditionSATSolver();
	
	@Override
	public List<List<? extends IOperand>> generateAllValidTupels(Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot, ConditionBundle forbidden, int tupelSize) {
		
		List<List<? extends IOperand>> result = new ArrayList<>();
		
		if(tupelSize != 2) {
			throw new IllegalArgumentException("Tupel size can only be 2 for TupelGenerator class");
		}
		
		// check conditions with no variables
		// XXX for checking all cases, getConditions() would be better. Check performance penalty
		if(!satSolver.canBeAllowed(forbidden.getConditionsForClassificationVariable(""), leafsByRoot)) {
			return result;
		}
		
		List<Entry<ClassificationVariable, List<? extends IOperand>>> cvs = new ArrayList<>(leafsByRoot.entrySet());
		cvs.sort(new ClassificationVariableEntryComparator());
		for(int cv1Index = 0; cv1Index < cvs.size(); cv1Index++) {
			Entry<ClassificationVariable, List<? extends IOperand>> cv1 = cvs.get(cv1Index);
			for(IOperand op1 : cv1.getValue()) {
				List<? extends ICondition> conditionsForCv1 = forbidden.getConditionsForClassificationVariable(cv1.getKey().getName());
				List<ICondition> optimizedForbiddenForCv1 = optimizeConditions(conditionsForCv1, Arrays.asList(op1));
				
				if(satSolver.canBeAllowed(optimizedForbiddenForCv1, leafsByRoot)) {
					for(int cv2Index = cv1Index + 1; cv2Index < cvs.size(); cv2Index++) {
						Entry<ClassificationVariable, List<? extends IOperand>> cv2 = cvs.get(cv2Index);
						
						for(IOperand op2 : cv2.getValue()) {
							List<IOperand> pair = Arrays.asList(op1, op2);
							
							List<? extends ICondition> conditionsForCv2 = forbidden.getConditionsForClassificationVariable(cv2.getKey().getName());
							List<ICondition> optimizedForbiddenForCv1Cv2 = optimizeConditions(conditionsForCv2, Arrays.asList(op1, op2));
							
							if(satSolver.canBeAllowed(optimizedForbiddenForCv1Cv2, leafsByRoot)) {
								result.add(pair);
							}
						}
					}
				}
			}
		}
		
		return result;
	}

	private List<ICondition> optimizeConditions(List<? extends ICondition> conditions, List<?extends IOperand> ops) {
		List<ICondition> optimizedForbiddenForCv1 = new ArrayList<>(conditions.size());
		for(ICondition c : conditions) {
			optimizedForbiddenForCv1.add(c.optimize(ops));
		}
		return optimizedForbiddenForCv1;
	}

}
