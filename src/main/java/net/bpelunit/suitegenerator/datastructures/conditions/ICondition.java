package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.List;
import java.util.Set;

public interface ICondition {

	/**
	 * Returns false when the condition does not hold. This includes when there are not enough arguments given
	 * 
	 * @param ops
	 * @return
	 */
	boolean evaluate(List<? extends IOperand> ops);

	boolean evaluate(List<? extends IOperand> ops, IOperand additional);

	boolean evaluate(IOperand... ops);

	ICondition visit(ConditionParser parser);

	Set<OperandCondition> getVariables();

	boolean canEvaluate(List<? extends IOperand> l);

}
