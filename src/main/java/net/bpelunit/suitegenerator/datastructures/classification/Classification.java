package net.bpelunit.suitegenerator.datastructures.classification;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.testcases.TestCase;

public class Classification {

	private List<TestCase> testCases = new LinkedList<>();
	private ClassificationTree tree;
	private ConditionBundle forbidden;
	private Collection<ClassificationVariableSelection> leaves;

	public List<TestCase> getTestCases() {
		return testCases;
	}

	public void addTestCase(TestCase testCase) {
		this.testCases.add(testCase);
	}

	public ClassificationTree getTree() {
		return tree;
	}

	public void setTree(ClassificationTree tree) {
		this.tree = tree;
	}

	public ConditionBundle getForbidden() {
		return forbidden;
	}

	public void setForbidden(ConditionBundle forbidden) {
		this.forbidden = forbidden;
	}

	/**
	 * @return Everything that maps to a Mapping
	 */
	public Collection<ClassificationVariableSelection> getAllClassificationTreeLeaves() {
		return leaves;
	}

	public void setLeaves(Collection<ClassificationVariableSelection> leaves) {
		this.leaves = leaves;
	}

}
