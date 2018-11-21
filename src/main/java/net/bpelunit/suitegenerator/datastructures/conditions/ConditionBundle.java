package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ConditionBundle implements ICondition {

	private List<ICondition> conditions = new LinkedList<>();

	/**
	 * Returns true if any of the contained conditions returns true
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops) {
		for (ICondition cond : conditions) {
			if (cond.evaluate(ops)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if any of the contained conditions returns true
	 */
	@Override
	public boolean evaluate(List<? extends IOperand> ops, IOperand additional) {
		List<IOperand> neu = new ArrayList<>(ops);
		neu.add(additional);
		return evaluate(neu);
	}

	/**
	 * Returns true if any of the contained conditions returns true
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
	public ICondition visit(ConditionParser parser) {
		return null;
	}

	public void addCondition(ICondition add) {
		this.conditions.add(add);
	}
	
	public List<ICondition> getConditions() {
		return conditions;
	}

	@Override
	public String toString() {
		return conditions.toString();
	}
	
	@Override
	public Set<OperandCondition> getVariables() {
		Set<OperandCondition> result = new HashSet<>();
		
		for(ICondition c : conditions) {
			result.addAll(c.getVariables());
		}
		return result;
	}

	@Override
	public boolean canEvaluate(List<? extends IOperand> l) {
		for(ICondition c : conditions) {
			if(!c.canEvaluate(l)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean canEvaluate(List<? extends IOperand> l, IOperand op) {
		for(ICondition c : conditions) {
			if(!c.canEvaluate(l, op)) {
				return false;
			}
		}
		return true;
	}
	
	public ConditionBundle clone() {
		ConditionBundle result = new ConditionBundle();
		for(ICondition c : conditions) {
			result.conditions.add(c.clone());
		}
		
		return result;
	}
	
	@Override
	public ICondition optimize(List<? extends IOperand> ops) {
		List<ICondition> newConditions = new ArrayList<>();
		for(ICondition c : conditions) {
			ICondition c2 = c.clone().optimize(ops);
			if(c2.canEvaluate(ops)) {
				if(c2.evaluate(ops)) {
					return new TRUE();
				} else {
					// skip this condition because it is always false
				}
			} else {
				newConditions.add(c2);
			}
		}
		
		if(newConditions.size() == 0) {
			return FALSE.INSTANCE;
		} else {
			ConditionBundle result = new ConditionBundle();
			result.conditions = newConditions;
			return result;
		}
	}
	
	@Override
	public ICondition optimize(List<? extends IOperand> ops, IOperand additionalOp) {
		List<ICondition> newConditions = new ArrayList<>();
		for(ICondition c : conditions) {
			ICondition c2 = c.optimize(ops, additionalOp);
			if(c2.canEvaluate(ops, additionalOp)) {
				if(c2.evaluate(ops, additionalOp)) {
					return new TRUE();
				} else {
					// skip this condition because it is always false
				}
			} else {
				newConditions.add(c2);
			}
		}
		
		ConditionBundle result = new ConditionBundle();
		result.conditions = newConditions;
		return result;
	}
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
	public void getVariableNames(Set<String> variables) {
		for(ICondition c : conditions) {
			c.getVariableNames(variables);
		}
	}
	
	@Override
	public String getAnyVariable() {
		for(ICondition c : conditions) {
			String result = c.getAnyVariable();
			if(result != null) {
				return result;
			}
		}
		return null;
	}
}
