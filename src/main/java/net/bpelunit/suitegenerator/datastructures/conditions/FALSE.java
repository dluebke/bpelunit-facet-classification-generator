package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FALSE implements ICondition {

	public static final FALSE INSTANCE = new FALSE();
	
	private FALSE() {
	}
	
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		return false;
	}

	@Override
	public boolean evaluate(List<? extends IOperand> ops, IOperand additional) {
		return false;
	}

	@Override
	public boolean evaluate(IOperand... ops) {
		return false;
	}

	@Override
	public ICondition visit(ConditionParser parser) {
		return null;
	}

	@Override
	public Set<OperandCondition> getVariables() {
		return Collections.emptySet();
	}

	@Override
	public boolean canEvaluate(List<? extends IOperand> l) {
		return true;
	}

	@Override
	public boolean canEvaluate(List<? extends IOperand> l, IOperand op) {
		return true;
	}
	
	@Override
	public FALSE clone() {
		return INSTANCE;
	}

	@Override
	public ICondition optimize(List<? extends IOperand> ops) {
		return this;
	}

	@Override
	public ICondition optimize(List<? extends IOperand> ops, IOperand additionalOp) {
		return this;
	}

	@Override
	public boolean isAlwaysFalse() {
		return true;
	}
	
	@Override
	public boolean isAlwaysTrue() {
		return false;
	}
	
	@Override
	public String toString() {
		return "FALSE";
	}
	
	@Override
	public void getVariableNames(Set<String> variables) {
	}
	
	@Override
	public String getAnyVariable() {
		return null;
	}
	
	@Override
	public Set<String> getClassificationVariableNames() {
		return Collections.emptySet();
	}
}
