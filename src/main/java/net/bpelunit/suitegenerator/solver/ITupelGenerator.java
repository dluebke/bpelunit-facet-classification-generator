package net.bpelunit.suitegenerator.solver;

import java.util.List;
import java.util.Map;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;

public interface ITupelGenerator {

	List<List<? extends IOperand>> generateAllValidTupels(
			Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot, ConditionBundle forbidden,
			int tupelSize);

}
