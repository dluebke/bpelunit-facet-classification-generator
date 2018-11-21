package net.bpelunit.suitegenerator.solver;

import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;

public class Literal implements IOperand {

	private String name;
	private String variableName;
	
	public Literal(String name) {
		this.name = name;
		this.variableName = name.split(":")[0];
	}
	
	public Literal(String name, String variableName) {
		this.name = name;
		this.variableName = variableName;
	}
	
	@Override
	public String getOpName() {
		return name;
	}

	@Override
	public String toString() {
		return getOpName();
	}
	
	@Override
	public String getVariableName() {
		return variableName;
	}
}
