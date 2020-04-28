package net.bpelunit.suitegenerator.datastructures.conditions;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * TODO Improve in order to make an object reusable for multiple parsings
 *
 */
public class ConditionParser {

	private int position = 0;
	private List<ICondition> conds = new LinkedList<>();

	public ICondition parse(String cond) {
		position = 0;
		conds.clear();
		StringBuilder effectiveCond = new StringBuilder();
		cond = cond.replaceAll("\r", "\n");
		String[] lines = cond.split("\n");
		for(String line : lines) {
			int posOfComment = line.indexOf('#');
			if(posOfComment >= 0) {
				line = line.substring(0, posOfComment);
			}
			line = line.trim();
			if(!("".equals(line))) {
				effectiveCond.append(line).append(" ");
			}
		}
		cond = effectiveCond.toString();
		
		cond = cond.replaceAll("\t", " ");
		cond = "(" + cond + ")";
		cond = cond.replace("(", "( ");
		cond = cond.replace(")", " )");
		cond = cond.replaceAll("\\s+", " ");
		String[] parts = cond.split(" ");
		parseConds(parts);
		return handleCondition(conds.get(0));
		// parseBracket(0, cond.length() - 1);
	}

	private void parseConds(String[] parts) {
		for (String part : parts) {
			if (part.equals("(")) {
				conds.add(new OpenBracket());
			} else if (part.equals(")")) {
				conds.add(new CloseBracket());
			} else if (part.equals("AND") || part.equals("&&")) {
				conds.add(new AND());
			} else if (part.equals("OR") || part.equals("||")) {
				conds.add(new OR());
			} else if (part.equals("XOR") || part.equals("^")) {
				conds.add(new XOR());
			} else if (part.equals("NOT") || part.equals("!")) {
				conds.add(new NOT());
			} else {
				conds.add(new OperandCondition(part));
			}
		}
	}

	public ICondition handleCondition(ICondition cond) {
		return cond.visit(this);
	}

	public ICondition consumeBefore() {
		return handleCondition(conds.remove(--position));
	}

	public ICondition consumeNext() {
		ICondition cond = conds.get(++position);
		ICondition result = handleCondition(cond);
		conds.remove(position);
		position--;
		return result;
	}

	public ICondition parseBrackets() {
		int backTo = position;
		conds.remove(position);
		for (; conds.size() > position; position++) {
			ICondition c = conds.get(position).visit(this);
			if (c == null) {
				ICondition ret = conds.get(position);
				position = backTo;
				return ret;
			}
		}
		// Just for the compiler
		return conds.get(0);
	}

	public void moveForward() {
		position++;
	}

	public void moveBackward() {
		position--;
	}

	public ICondition endParseBracket() {
		conds.remove(position--);
		return null;
	}

}

class OpenBracket implements ICondition {

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
		return parser.parseBrackets();
	}

	@Override
	public String toString() {
		return "{ ";
	}

	@Override
	public Set<OperandCondition> getVariables() {
		return new HashSet<>();
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
	public OpenBracket clone() {
		return new OpenBracket();
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
		return false;
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

class CloseBracket implements ICondition {

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
		return parser.endParseBracket();
	}

	@Override
	public String toString() {
		return " }";
	}
	
	@Override
	public Set<OperandCondition> getVariables() {
		return new HashSet<>();
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
	public CloseBracket clone() {
		return new CloseBracket();
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
		return false;
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