package net.bpelunit.suitegenerator.recommendation.full;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariableNameComparator;
import net.bpelunit.suitegenerator.recommendation.Recommender;
import net.bpelunit.suitegenerator.solver.ConditionSATSolver;
import net.bpelunit.suitegenerator.statistics.Selection;

public class FastFullTestRecommenderCountOnly extends Recommender {

	private List<ClassificationVariable> roots = new ArrayList<>();
	private Map<ClassificationVariable, List<Selection>> leafsByRoot;
	private long testCasesToGenerate = 0;
	private final Mode mode;
	private ConditionSATSolver solver;
	
	public enum Mode {
		SILENT,
		NORMAL
	}
	
	public FastFullTestRecommenderCountOnly() {
		this(Mode.NORMAL);
	}
	
	public FastFullTestRecommenderCountOnly(Mode mode) {
		this.mode = mode;
	}

	@Override
	protected void createRecommendations() {
		super.createRecommendations();
		
		testCasesToGenerate = 0;
		
		leafsByRoot = new HashMap<>(statistic.getRootVariables());
		
		roots.clear();
		roots.addAll(leafsByRoot.keySet());
		roots.sort(new ClassificationVariableNameComparator());
		
		solver = new ConditionSATSolver(forbidden, new HashMap<>(statistic.getRootVariables()));
		
		long start = System.currentTimeMillis();
		createRecommendation(new ArrayList<>());
		long end = System.currentTimeMillis();
		if(mode == Mode.NORMAL) {
			System.out.println("Would generate " + testCasesToGenerate + " test cases [calculated in " + (end-start) + "ms]");
			System.out.println("-> In order to generate these test cases use the following options: -g -recommender full");
			System.out.println("!!! BE AWARE: This can produce large test suites and consumes lots of memory!");
		}
	}
	
	private void createRecommendation(List<Selection> chosenValuesForRoots) {
		int rootToHandle = chosenValuesForRoots.size();
		
		if(rootToHandle == roots.size()) {
			if(!forbidden.evaluate(chosenValuesForRoots.toArray(new Selection[rootToHandle]))) {
				testCasesToGenerate++;
			}
			
			return;
		}
		
		List<Selection> leafsForRoot = leafsByRoot.get(roots.get(rootToHandle));
		for(Selection s : leafsForRoot) {
			List<Selection> newSelection = new ArrayList<>(chosenValuesForRoots.size() + 1);
			newSelection.addAll(chosenValuesForRoots);
			newSelection.add(s);
			
			if(!solver.isAlwaysForbidden(newSelection)) {
				createRecommendation(newSelection);
			}
		}
	}
}
