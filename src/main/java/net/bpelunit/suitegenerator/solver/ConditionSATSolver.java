package net.bpelunit.suitegenerator.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;

public class ConditionSATSolver implements ITupelGenerator {

	public boolean canBeAllowed(List<? extends ICondition> conditions, Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot) {
		for(ICondition c : conditions) {
			if(c.isAlwaysTrue()) {
				return false;
			}
			if(!c.isAlwaysFalse() && !canBe(false, c, leafsByRoot)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean canBe(boolean value, ICondition c,
			Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot) {
		Map<String, List<? extends IOperand>> leafsByRootAsString = new HashMap<>();
		for (ClassificationVariable v : leafsByRoot.keySet()) {
			leafsByRootAsString.put(v.getName(), leafsByRoot.get(v));
		}

		ICondition x = c.clone().optimize(Collections.emptyList());

		return canBeWithString(value, x, leafsByRootAsString);
	}

	public boolean canBeWithString(boolean value, ICondition c, Map<String, List<? extends IOperand>> leafsByRoot) {
		String varName = c.getAnyVariable();
		if (varName != null) {
			for (IOperand o : leafsByRoot.get(varName)) {
				ICondition optimized = c.optimize(Arrays.asList(o));
				if (canBeWithString(value, optimized, leafsByRoot)) {
					return true;
				}
			}
		} else {
			if(value) {
				return c.isAlwaysTrue();
			} else {
				return c.isAlwaysFalse();
			}
//			return c.evaluate() == value;
		}

		return false;
	}

	@Override
	public List<List<?extends IOperand>> generateAllValidTupels(Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot,
			ConditionBundle forbidden, int tupelSize) {
		List<List<?extends IOperand>> result = new ArrayList<>();

		generateAllTupels(new ArrayList<>(leafsByRoot.keySet()), leafsByRoot,
				forbidden.optimize(Collections.emptyList()), result, new ArrayList<>(), tupelSize);

		return result;
	}

	private void generateAllTupels(List<ClassificationVariable> remainingRoots,
			Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot, ConditionBundle forbidden,
			List<List<?extends IOperand>> result, List<List<?extends IOperand>> intermediateTupels, int tupelSize) {
		if (remainingRoots.size() > 0) {
			remainingRoots = new ArrayList<>(remainingRoots);
			ClassificationVariable currentRoot = remainingRoots.remove(0);
			List<List<IOperand>> newIntermediateTupels = new ArrayList<>();
			for (IOperand newValue : leafsByRoot.get(currentRoot)) {
				ConditionBundle f = forbidden.optimize(Arrays.asList(newValue));
				for (List<?extends IOperand> intermediateTupel : intermediateTupels) {
					ConditionBundle f2 = f.optimize(intermediateTupel);
					if (canBeAllowed(f2.getConditionsForClassificationVariable(newValue.getVariableName()), leafsByRoot)) {
						List<IOperand> tNew = new ArrayList<>(intermediateTupel);
						tNew.add(newValue);
						if (tNew.size() == tupelSize) {
							result.add(tNew);
						} else {
							newIntermediateTupels.add(tNew);
						}
					}
				}
				newIntermediateTupels.add(Arrays.asList(newValue));
			}
			intermediateTupels.addAll(newIntermediateTupels);
			generateAllTupels(remainingRoots, leafsByRoot, forbidden, result, intermediateTupels, tupelSize);
		}
	}
}
