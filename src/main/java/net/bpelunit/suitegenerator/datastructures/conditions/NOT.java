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
		ICondition newFirst = first.optimize(ops);
		
		if(newFirst.canEvaluate(ops)) {
			if(!newFirst.evaluate(ops)) {
				return TRUE.INSTANCE;
			} else {
				return FALSE.INSTANCE;
			}
		} else {
			if(newFirst != first) {
				return new NOT(newFirst);
			} else {
				return this;
			}
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
	
	@Override
	public Set<String> getClassificationVariableNames() {
		return first.getClassificationVariableNames();
	}
}
