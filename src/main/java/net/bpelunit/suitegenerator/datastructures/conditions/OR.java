package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OR extends AbstractCondition implements ICondition {

	private ICondition first, second;

	public OR(ICondition first, ICondition second) {
		this.first = first;
		this.second = second;
	}

	public OR() {

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
	 * Returns true if either of the contained conditions holds for the given Operands
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		return first.evaluate(ops) || second.evaluate(ops);
	}

	@Override
	public String toString() {
		if (first == null || second == null) {
			return "OR";
		}
		return "(" + first + " || " + second + ")";
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
		
		return
			(canEvaluateFirst && canEvaluateSecond)
			||
			(canEvaluateFirst && first.evaluate(l))
			||
			(canEvaluateSecond && second.evaluate(l))
		;
	}
	
	@Override
	public OR clone() {
		OR result = new OR();
		result.first = first.clone();
		result.second = second.clone();
		return result;
	}
	
	@Override
	public ICondition optimize(List<? extends IOperand> ops) {
		first = first.optimize(ops);
		second = second.optimize(ops);
		
		if(first.isAlwaysTrue() || second.isAlwaysTrue()) {
			return new TRUE();
		} else if(first.isAlwaysFalse()) {
			return second;
		} else if(second.isAlwaysFalse()) {
			return first;
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
}
