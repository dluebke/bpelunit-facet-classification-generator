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
	boolean canEvaluate(List<? extends IOperand> l, IOperand op);

	ICondition clone();
	
	ICondition optimize(List<? extends IOperand> ops);

	ICondition optimize(List<? extends IOperand> ops, IOperand additionalOp);

	boolean isAlwaysTrue();
	boolean isAlwaysFalse();

	void getVariableNames(Set<String> variables);
	String getAnyVariable();
	
	Set<String> getClassificationVariableNames();
}
