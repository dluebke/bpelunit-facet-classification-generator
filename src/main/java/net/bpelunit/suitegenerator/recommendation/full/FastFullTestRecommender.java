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
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;
import net.bpelunit.suitegenerator.recommendation.Recommendation;
import net.bpelunit.suitegenerator.recommendation.Recommender;
import net.bpelunit.suitegenerator.solver.ConditionSATSolver;
import net.bpelunit.suitegenerator.statistics.Selection;

public class FastFullTestRecommender extends Recommender {

	private List<ClassificationVariable> roots = new ArrayList<>();
	private Map<ClassificationVariable, List<?extends IOperand>> leafsByRoot;
	private Map<String,ClassificationVariable> classificationVariablesByName;
	private ConditionSATSolver solver = new ConditionSATSolver();
	
	@Override
	protected void createRecommendations() {
		long start = System.currentTimeMillis();
		
		super.createRecommendations();
		
		leafsByRoot = new HashMap<>(statistic.getRootVariables());
		roots.clear();
		roots.addAll(leafsByRoot.keySet());
		
		Set<String> variableNamesInForbidden = new HashSet<>(); 
		forbidden.getVariableNames(variableNamesInForbidden);
		List<String> variableNamesNotInForbidden = new ArrayList<>();
		classificationVariablesByName = new HashMap<>();
		for(ClassificationVariable cv : statistic.getRootVariables().keySet()) {
			classificationVariablesByName.put(cv.getName(), cv);
			if(!variableNamesInForbidden.contains(cv.getName())) {
				variableNamesNotInForbidden.add(cv.getName());
			}
		}

		ArrayList<String> forbiddenVariableNames = new ArrayList<String>(variableNamesNotInForbidden);
		createRecommendationForVariablesInForbidden(forbiddenVariableNames, variableNamesNotInForbidden, forbidden, new ArrayList<Selection>());
		long end = System.currentTimeMillis();
		notifyNewState("Created "  + recommendations.size() + " test cases in " + (end-start) + "ms");
	}
	
	private void createRecommendationForVariablesInForbidden(List<String> remainingVariablesInForbidden, List<String> remainingVariablesNotInForbidden, ICondition currentOptimizedForbidden, List<Selection> currentSelection) {
		if(remainingVariablesInForbidden.size() > 0) {
			String currentVariable = remainingVariablesInForbidden.remove(0);
			try {
				ClassificationVariable cv = classificationVariablesByName.get(currentVariable);
				List<Selection> currentValues = statistic.getRootVariables().get(cv);
				for(Selection s : currentValues) {
					ICondition newForbidden = currentOptimizedForbidden.optimize(Arrays.asList(s));
					if(solver.canBe(false, newForbidden, leafsByRoot)) {
						List<Selection> newSelection = new ArrayList<>(currentSelection);
						newSelection.add(s);
						createRecommendationForVariablesInForbidden(remainingVariablesInForbidden, remainingVariablesNotInForbidden, newForbidden, newSelection);
					}
				}
			} finally {
				remainingVariablesInForbidden.add(0, currentVariable);
			}
		} else {
			createRecommendationForVariablesNotInForbidden(remainingVariablesNotInForbidden, currentSelection);
		}
	}
	
	private void createRecommendationForVariablesNotInForbidden(List<String> remainingVariablesNotInForbidden, List<Selection> currentSelection) {
		if(remainingVariablesNotInForbidden.size() == 0) {
			addRecommendation(currentSelection);
		} else {
			String currentVariable = remainingVariablesNotInForbidden.get(0);
			ClassificationVariable cv = classificationVariablesByName.get(currentVariable);
			List<Selection> currentValues = statistic.getRootVariables().get(cv);
			for(Selection s : currentValues) {
				List<Selection> newSelection = new ArrayList<>(currentSelection);
				newSelection.add(s);
				createRecommendationForVariablesNotInForbidden(remainingVariablesNotInForbidden, newSelection);
			}
			remainingVariablesNotInForbidden.add(0, currentVariable);
		}
	}

	private void addRecommendation(List<Selection> chosenValues) {
		Recommendation e = new Recommendation();
		e.addSelections(chosenValues);
		recommendations.add(e);
	}
}
