package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OperandCondition extends AbstractCondition implements ICondition {

	private String tag;
	private String variableName;

	private OperandCondition() {
	}
	
	public OperandCondition(String tag) {
		this.tag = tag.trim();
		this.variableName = this.tag.split(":")[0];
	}

	public ICondition visit(ConditionParser parser) {
		return this;
	}

	/**
	 * Returns true if any of the given Operands has the same opName
	 * 
	 * @param ops
	 * @return
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		for (IOperand op : ops) {
			if (op.getOpName().equalsIgnoreCase(tag)) {
				return true;
			}
			if(belongsToSameVariable(op)) {
				return false;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return tag;
	}
	
	@Override
	public Set<OperandCondition> getVariables() {
		Set<OperandCondition> result = new HashSet<>();
		result.add(this);
		return result;
	}
	
	public String getTag() {
		return tag;
	}
	
	@Override
	public boolean canEvaluate(List<? extends IOperand> l) {
		for(IOperand i : l) {
			if(i.getOpName().equalsIgnoreCase(tag) || belongsToSameVariable(i)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public OperandCondition clone() {
		OperandCondition result = new OperandCondition();
		result.tag = tag;
		result.variableName = variableName;
		return result;
	}
	
	@Override
	public ICondition optimize(List<? extends IOperand> ops) {
		if(canEvaluate(ops)) {
			if(evaluate(ops)) {
				return TRUE.INSTANCE;
			} else {
				return FALSE.INSTANCE;
			}
		} else {
			return this;
		}
	}
	
	private boolean belongsToSameVariable(IOperand i) {
		return this.variableName.equalsIgnoreCase(i.getVariableName());
	}

	public String getVariableName() {
		return variableName;
	}
	
	@Override
	public void getVariableNames(Set<String> variables) {
		variables.add(variableName);
	}
	
	@Override
	public String getAnyVariable() {
		return variableName;
	}
}
