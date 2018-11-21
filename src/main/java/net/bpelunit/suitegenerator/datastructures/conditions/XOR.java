package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class XOR extends AbstractCondition implements ICondition {

	private ICondition first, second;

	public XOR(ICondition first, ICondition second) {
		this.first = first;
		this.second = second;
	}

	public XOR() {

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
	 * Returns true if exactly one of the contained conditions holds for the given Operands
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		return first.evaluate(ops) ^ second.evaluate(ops);
	}

	@Override
	public String toString() {
		if (first == null || second == null) {
			return "XOR";
		}
		
		// Required for IPOG-solver...
//		return "(" + "(" + first + " && " + "!(" + second + ")" + ")" + " || " + "(" + "!(" + first + ")" + " && " + second + ")" + ")";
		return "(" + first + " ^ " + second + ")";
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
		return first.canEvaluate(l) && second.canEvaluate(l);
	}
	
	@Override
	public XOR clone() {
		XOR result = new XOR();
		result.first = first.clone();
		result.second = second.clone();
		return result;
	}
	
	@Override
	public ICondition optimize(List<? extends IOperand> ops) {
		first = first.optimize(ops);
		second = second.optimize(ops);
		
		if(canEvaluate(ops)) {
			if(evaluate(ops)) {
				return TRUE.INSTANCE;
			} else {
				return FALSE.INSTANCE;
			}
		} else {
			if(first.isAlwaysFalse()) {
				return second;
			}
			if(first.isAlwaysTrue()) {
				return new NOT(second);
			}
			if(second.isAlwaysFalse()) {
				return first;
			}
			if(second.isAlwaysTrue()) {
				return new NOT(first);
			}
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
