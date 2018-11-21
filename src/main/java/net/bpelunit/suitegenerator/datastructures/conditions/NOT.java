package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.List;
import java.util.Set;

public class NOT extends AbstractCondition implements ICondition {

	private ICondition first;

	public NOT(ICondition first) {
		this.first = first;
	}

	public NOT() {

	}

	public ICondition visit(ConditionParser parser) {
		if (first != null) {
			return this;
		}
		first = parser.consumeNext();
		return this;
	}

	/**
	 * Returns the inverted result of the contained condition
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		return !first.evaluate(ops);
	}

	@Override
	public String toString() {
		if (first == null) {
			return "NOT";
		}
		return "!(" + first + ")";
	}

	@Override
	public Set<OperandCondition> getVariables() {
		return first.getVariables();
	}
	
	@Override
	public boolean canEvaluate(List<? extends IOperand> l) {
		return first.canEvaluate(l);
	}
	
	@Override
	public NOT clone() {
		NOT result = new NOT(first.clone());
		return result;
	}
	
	@Override
	public ICondition optimize(List<? extends IOperand> ops) {
		first = first.optimize(ops);
		
		if(canEvaluate(ops)) {
			if(evaluate(ops)) {
				return new TRUE();
			} else {
				return new FALSE();
			}
		} else {
			return this;
		}
	}
	
	@Override
	public void getVariableNames(Set<String> variables) {
		first.getVariableNames(variables);
	}
	
	@Override
	public String getAnyVariable() {
		return first.getAnyVariable();
	}
}
