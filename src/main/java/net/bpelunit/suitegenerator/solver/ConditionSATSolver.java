package net.bpelunit.suitegenerator.solver;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;

public class ConditionSATSolver {

	public boolean canBe(boolean value, ICondition c, Map<ClassificationVariable, List<?extends IOperand>> leafsByRoot) {
		Map<String, List<?extends IOperand>> leafsByRootAsString = new HashMap<>();
		for(ClassificationVariable v : leafsByRoot.keySet()) {
			leafsByRootAsString.put(v.getName(), leafsByRoot.get(v));
		}
		
		ICondition x = c.clone().optimize(Collections.emptyList());
		
		return canBeWithString(value, x, leafsByRootAsString);
	}
	
	public boolean canBeWithString(boolean value, ICondition c, Map<String, List<?extends IOperand>> leafsByRoot) {
		String varName = c.getAnyVariable();
		if(varName != null) {
			for(IOperand o : leafsByRoot.get(varName)) {
				if(canBeWithString(value, c.clone().optimize(Arrays.asList(o)), leafsByRoot)) {
					return true;
				}
			}
		} else {
			return c.evaluate() == value;
		}
		
		return false;
	}
	
}
