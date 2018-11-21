package net.bpelunit.suitegenerator.recommendation.full;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.recommendation.Recommender;
import net.bpelunit.suitegenerator.statistics.Selection;

public class FastFullTestRecommenderCountOnly extends Recommender {

	private List<ClassificationVariable> roots = new ArrayList<>();
	private Map<ClassificationVariable, List<Selection>> leafsByRoot;
	private long testCasesToGenerate = 0;
	private Map<String,ClassificationVariable> classificationVariablesByName;
	
	@Override
	protected void createRecommendations() {
		long start = System.currentTimeMillis();
		
		super.createRecommendations();
		
		testCasesToGenerate = 0;
		
		leafsByRoot = new HashMap<>(statistic.getRootVariables());
		roots.clear();
		roots.addAll(leafsByRoot.keySet());
		
		Set<String> variableNamesInForbidden = new HashSet<>(); 
		forbidden.getVariableNames(variableNamesInForbidden);
		classificationVariablesByName = new HashMap<>();
		long variableChoicesNotInForbidden = 1;
		for(ClassificationVariable cv : statistic.getRootVariables().keySet()) {
			classificationVariablesByName.put(cv.getName(), cv);
			if(!variableNamesInForbidden.contains(cv.getName())) {
				variableChoicesNotInForbidden *= statistic.getRootVariables().get(cv).size();
			}
		}

		createRecommendationForVariablesInForbidden(new ArrayList<>(variableNamesInForbidden), variableChoicesNotInForbidden, forbidden, new ArrayList<Selection>());
		long end = System.currentTimeMillis();
		notifyNewState("Would generate " + testCasesToGenerate + " test cases [calculated in " + (end-start) + "ms]");
		if(testCasesToGenerate > 500) {
			notifyNewState("-> In order to generate these test cases use the following options: -g -recommender fastfull");
			notifyNewState("!!! BE AWARE: This can produce large test suites and consumes lots of memory!");
		}
	}
	
	private void createRecommendationForVariablesInForbidden(List<String> remainingVariablesInForbidden, long variableChoicesNotInForbidden, ICondition currentOptimizedForbidden, List<Selection> currentSelection) {
		if(remainingVariablesInForbidden.size() > 0) {
			String currentVariable = remainingVariablesInForbidden.remove(0);
			try {
				ClassificationVariable cv = classificationVariablesByName.get(currentVariable);
				List<Selection> currentValues = statistic.getRootVariables().get(cv);
				for(Selection s : currentValues) {
					ICondition newForbidden = currentOptimizedForbidden.clone().optimize(Arrays.asList(s));
					if(!newForbidden.isAlwaysTrue()) {
						List<Selection> newSelection = new ArrayList<>(currentSelection);
						newSelection.add(s);
						createRecommendationForVariablesInForbidden(remainingVariablesInForbidden, variableChoicesNotInForbidden, newForbidden, newSelection);
					}
				}
			} finally {
				remainingVariablesInForbidden.add(0, currentVariable);
			}
		} else {
			testCasesToGenerate += variableChoicesNotInForbidden;
		}
	}
}
