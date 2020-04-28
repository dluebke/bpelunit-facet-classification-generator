package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AND extends AbstractCondition implements ICondition {

	private ICondition first, second;

	public AND(ICondition first, ICondition second) {
		this.first = first;
		this.second = second;
	}

	public AND() {

	}

	public ICondition visit(ConditionParser parser) {
		if (first != null && second != null) {
			return this;
		}
		first = parser.consumeBefore();
		second = parser.consumeNext();
		return this;
	}

	/**
	 * Returns true if both contained conditions hold for the given Operands
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		boolean canEvaluateFirst = first.canEvaluate(ops);
		boolean canEvaluateSecond = second.canEvaluate(ops);
		if(canEvaluateFirst && first.evaluate(ops) == false) {
			return false;
		}
		if(canEvaluateSecond && second.evaluate(ops) == false) {
			return false;
		}
		return first.evaluate(ops) && second.evaluate(ops);
	}

	@Override
	public String toString() {
		if (first == null || second == null) {
			return "AND";
		}
		return "(" + first + " && " + second + ")";
	}

	@Override
	public Set<OperandCondition> getVariables() {
		Set<OperandCondition> result = new HashSet<>();
		result.addAll(first.getVariables());
		result.addAll(second.getVariables());
		return result;
	}
	
	@Override
	public boolean canEvaluate(List<? extends IOperand> l) {
		boolean canEvaluateFirst = first.canEvaluate(l);
		boolean canEvaluateSecond = second.canEvaluate(l);
		
		if(canEvaluateFirst && canEvaluateSecond) {
			return true;
		} else if(canEvaluateFirst && !first.evaluate(l)) {
			return true;
		} else if(canEvaluateSecond && !second.evaluate(l)) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public AND clone() {
		AND result = new AND();
		result.first = first.clone();
		result.second = second.clone();
		return result;
	}
	
	@Override
	public ICondition optimize(List<? extends IOperand> ops) {
		
		ICondition newFirst = first.optimize(ops);
		if(newFirst.isAlwaysFalse()) {
			return FALSE.INSTANCE;
		}
		
		ICondition newSecond = second.optimize(ops);
		if(newSecond.isAlwaysFalse()) {
			return FALSE.INSTANCE;
		}
		
		if(newFirst.isAlwaysTrue() && newSecond.isAlwaysTrue()) {
			return TRUE.INSTANCE;
		}
		
		if(newFirst.isAlwaysTrue()) {
			return newSecond;
		}
		if(newSecond.isAlwaysTrue()) {
			return newFirst;
		}
		if(newFirst != first || newSecond != second) {
			return new AND(newFirst, newSecond);
		} else {
			return this;
		}
	}
	
	@Override
	public void getVariableNames(Set<String> variables) {
		first.getVariableNames(variables);
		second.getVariableNames(variables);
	}
	
	@Override
	public String getAnyVariable() {
		String result = first.getAnyVariable();
		if(result != null) {
			return result;
		} else {
			return second.getAnyVariable();
		}
	}
	
	@Override
	public Set<String> getClassificationVariableNames() {
		Set<String> result = new HashSet<>();
		
		result.addAll(first.getClassificationVariableNames());
		result.addAll(second.getClassificationVariableNames());
		
		return result;
	}
}
