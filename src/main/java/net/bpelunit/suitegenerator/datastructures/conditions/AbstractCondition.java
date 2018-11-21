package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCondition implements ICondition {

	@Override
	public final boolean canEvaluate(List<? extends IOperand> l, IOperand op) {
		List<IOperand> neu = new ArrayList<>(l);
		neu.add(op);
		return canEvaluate(neu);
	}
	
	/**
	 * Returns true if both contained conditions hold for the given Operands
	 */
	@Override
	public final boolean evaluate(List<? extends IOperand> ops, IOperand additional) {
		List<IOperand> neu = new ArrayList<>(ops);
		neu.add(additional);
		return evaluate(neu);
	}

	/**
	 * Returns true if both contained conditions hold for the given Operands
	 */
	@Override
	public final boolean evaluate(IOperand... ops) {
		return evaluate(Arrays.asList(ops));
	}
	
	@Override
	public abstract AbstractCondition clone();
	
	@Override
	public boolean isAlwaysFalse() {
		List<IOperand> empty = Collections.emptyList();
		return canEvaluate(empty) && !evaluate(empty);
	}
	
	@Override
	public boolean isAlwaysTrue() {
		List<IOperand> empty = Collections.emptyList();
		return canEvaluate(empty) && evaluate(empty);
	}
	
	@Override
	public final ICondition optimize(List<? extends IOperand> ops, IOperand additionalOp) {
		List<IOperand> neu = new ArrayList<>(ops);
		neu.add(additionalOp);
		return optimize(neu);
	}
	
}
