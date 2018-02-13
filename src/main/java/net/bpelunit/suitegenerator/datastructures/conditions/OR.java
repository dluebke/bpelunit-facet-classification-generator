package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OR implements ICondition {

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

	/**
	 * Returns true if either of the contained conditions holds for the given Operands
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops, IOperand additional) {
		List<IOperand> neu = new ArrayList<>(ops);
		neu.add(additional);
		return evaluate(neu);
	}

	/**
	 * Returns true if either of the contained conditions holds for the given Operands
	 */
	@Override
	public boolean evaluate(IOperand... ops) {
		List<IOperand> neu = new ArrayList<>();
		for (IOperand op : ops) {
			neu.add(op);
		}
		return evaluate(neu);
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
		return first.canEvaluate(l) && second.canEvaluate(l);
	}
}
