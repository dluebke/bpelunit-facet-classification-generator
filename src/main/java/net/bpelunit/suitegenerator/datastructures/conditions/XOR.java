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
		ICondition newFirst = first.optimize(ops);
		ICondition newSecond = second.optimize(ops);
		
		
		if(
			(newFirst.isAlwaysTrue() && newSecond.isAlwaysFalse())
			||
			(newFirst.isAlwaysFalse() && newSecond.isAlwaysTrue())
			) {
				return TRUE.INSTANCE;
		}
		if(
			(newFirst.isAlwaysTrue() && newSecond.isAlwaysTrue())
			||
			(newFirst.isAlwaysFalse() && newSecond.isAlwaysFalse()) 
			) {
				return FALSE.INSTANCE;
		}
		if(newFirst.isAlwaysFalse()) {
			return newSecond;
		}
		if(newFirst.isAlwaysTrue()) {
			return new NOT(newSecond);
		}
		if(newSecond.isAlwaysFalse()) {
			return newFirst;
		}
		if(newSecond.isAlwaysTrue()) {
			return new NOT(newFirst);
		}
		if(first != newFirst || second != newSecond) {
			return new XOR(first, second);
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
