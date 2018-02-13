package net.bpelunit.suitegenerator.solver;

import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;

public class Literal implements IOperand {

	private String name;
	
	public Literal(String name) {
		this.name = name;
	}
	
	@Override
	public String getOpName() {
		return name;
	}

	@Override
	public String toString() {
		return getOpName();
	}
}
