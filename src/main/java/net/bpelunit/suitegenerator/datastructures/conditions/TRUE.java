package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TRUE implements ICondition {

	public static final TRUE INSTANCE = new TRUE();
	
	private TRUE() {
	}
	
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		return true;
	}

	@Override
	public boolean evaluate(List<? extends IOperand> ops, IOperand additional) {
		return true;
	}

	@Override
	public boolean evaluate(IOperand... ops) {
		return true;
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
	public TRUE clone() {
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
		return false;
	}
	
	@Override
	public boolean isAlwaysTrue() {
		return true;
	}
	
	@Override
	public String toString() {
		return "TRUE";
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
