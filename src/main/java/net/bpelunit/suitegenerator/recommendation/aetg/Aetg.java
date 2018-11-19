package net.bpelunit.suitegenerator.recommendation.aetg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariableNameComparator;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;
import net.bpelunit.suitegenerator.recommendation.IConfigurableRecommender;
import net.bpelunit.suitegenerator.recommendation.Recommendation;
import net.bpelunit.suitegenerator.recommendation.Recommender;
import net.bpelunit.suitegenerator.solver.ConditionSATSolver;
import net.bpelunit.suitegenerator.statistics.Selection;

public class Aetg extends Recommender implements IConfigurableRecommender {

	public class UsageSortComparator implements Comparator<Entry<IOperand, Integer>> {

		@Override
		public int compare(Entry<IOperand, Integer> o1, Entry<IOperand, Integer> o2) {
			return -o1.getValue().compareTo(o2.getValue());
		}

	}

	private static final boolean PRINT_CACHE_STATISTICS = true;

	private int tCoverage = 2;
	private int repetitions = 50;

	List<ClassificationVariable> roots = new ArrayList<>();
	Map<ClassificationVariable, List<?extends IOperand>> leafsByRoot;
	private Random randomGenerator;
	private int maxEmptyRounds = 50;
	private long seed = System.currentTimeMillis();
	
	ConditionSATSolver solver;
	
	@Override
	public void setConfigurationParameter(String parameter) {
		notifyNewState("Configuration passed: " + parameter);
		String[] paramPairs = parameter.split(",");
		
		for(String paramPair : paramPairs) {
			String[] paramPairTokens = paramPair.split("=");
			String param = paramPairTokens[0];
			String paramValue = "";
			if(paramPairTokens.length >= 2) {
				paramValue = paramPairTokens[1];
			}
			
			switch(param) {
				case "t": 
					tCoverage = Integer.valueOf(paramValue);
					break;
				case "M":
					repetitions = Integer.valueOf(paramValue);
					break;
				case "m":
					maxEmptyRounds = Integer.valueOf(paramValue);
					break;
				case "seed":
					seed = Long.valueOf(paramValue);
					break;
			}
		}
	}

	@Override
	protected void createRecommendations() {
		super.createRecommendations();

		notifyNewState("Efective configuration: t=" + tCoverage + ", repetitions=" + repetitions);
		
		notifyNewState("Using random seed: " + seed);
		randomGenerator = new Random(seed);
		super.createRecommendations();
		
		leafsByRoot = new HashMap<>(statistic.getRootVariables());
		
		roots.clear();
		roots.addAll(leafsByRoot.keySet());
		roots.sort(new ClassificationVariableNameComparator());
		
		List<IOperand> allParameterValues = new ArrayList<>();
		for(List<?extends IOperand> selections : leafsByRoot.values()) {
			for(IOperand s : selections) {
				allParameterValues.add(s);
			}
		}

		solver = new ConditionSATSolver(forbidden, leafsByRoot);
		
		notifyNewState("Generate all valid tupels");
		long t1 = System.currentTimeMillis();
		List<List<?extends IOperand>> uncoveredTupels = generateAllValidTupels(new ArrayList<>(roots));
		long t2 = System.currentTimeMillis();
		notifyNewState("Generated " + uncoveredTupels.size() + " tupels");
		
		createRecommendations(uncoveredTupels);
		long t3 = System.currentTimeMillis();
		notifyNewState("Generated " + recommendations.size() + " test cases in " + (t3-t1) + "ms [" + (t2-t1) + "," + (t3-t2) + "]");
		
		if(PRINT_CACHE_STATISTICS) {
			long cacheHits = solver.getCacheHits();
			long cacheMisses = solver.getCacheMisses();
			long cacheTotalAccesses = cacheHits + cacheMisses;
			int cacheEfficiency;
			if(cacheTotalAccesses != 0) {
				cacheEfficiency = (int)(cacheHits * 100 / cacheTotalAccesses);
			} else {
				cacheEfficiency = 0;
			}
			notifyNewState("Cache Size: " + solver.getCacheSize() + ", Cache Hits: " + cacheHits + ", Cache Misses: " + cacheMisses + " := " + cacheEfficiency + "%");
		}
	}
	
	private Selection getMostUnusedSelection(List<List<?extends IOperand>> uncovered) {
		if(uncovered.isEmpty()) {
			return null;
		}
		
		Map<IOperand, Integer> counters = new HashMap<>();
		for(List<?extends IOperand> l : uncovered) {
			for(IOperand s : l) {
				Integer counter = counters.get(s);
				if(counter == null) {
					counter = 1;
				} else {
					counter += 1;
				}
				counters.put(s, counter);
			}
		}

		Set<ClassificationVariable> usedRoots = new HashSet<>();
		List<Entry<IOperand, Integer>> selectionsByUse = new ArrayList<>(counters.entrySet());
		selectionsByUse.sort(new UsageSortComparator());
		int index = 0;
		List<Selection> result = new ArrayList<>();
		while (result.size() < tCoverage - 1) {
			Entry<IOperand, Integer> currentEntry = selectionsByUse.get(index);
			Selection currentSelection = (Selection)currentEntry.getKey();
			if(!usedRoots.contains(currentSelection.getRootElement()) && !solver.isAlwaysForbidden(result, currentSelection, leafsByRoot)) {
				usedRoots.add(currentSelection.getRootElement());
				result.add(currentSelection);
			}
			index++;
			
			if(index >= selectionsByUse.size()) {
				return null;
			}
		}
		
		if(result.isEmpty()) {
			return null;
		} else {
			return result.get(0);
		}
	}
	
	private void createRecommendations(List<List<?extends IOperand>> uncoveredTupels) {
		int roundsWithoutNewTestCase = 0;
		
		List<List<?extends IOperand>> possibleNextTestCases = new ArrayList<>();
		do {
			possibleNextTestCases.clear();
			for(int i = 0; i < repetitions; i++) {
				List<IOperand> nextTestCase = generateNextTestCase(uncoveredTupels);
				if(
					nextTestCase != null && 
					(forbidden == null || !forbidden.evaluate(nextTestCase.toArray(new Selection[nextTestCase.size()])))
					) {
					possibleNextTestCases.add(nextTestCase);
				}
			}
			if(possibleNextTestCases.size() > 0) {
				List<Selection> r = getBestNewTestCase(possibleNextTestCases, uncoveredTupels);
				if(removeNewlyCoveredTupels(r, uncoveredTupels)) {
					Recommendation tc = new Recommendation();
					tc.addSelections(r);
					recommendations.add(tc);
					roundsWithoutNewTestCase = 0;
				} else {
					roundsWithoutNewTestCase++;
				}
			} else {
				roundsWithoutNewTestCase++;
			}
			
			if(recommendations.size() % 20 == 0) {
				notifyNewState(recommendations.size() + " test cases generated, " + uncoveredTupels.size() + " tupels remaining...");
			}
		} while(uncoveredTupels.size() > 0 && roundsWithoutNewTestCase < maxEmptyRounds);
	}

	@SuppressWarnings("unchecked")
	private List<Selection> getBestNewTestCase(List<List<?extends IOperand>> possibleNextTestCases,
			List<List<?extends IOperand>> uncoveredTupels) {
		int max = 1;
		List<List<?extends IOperand>> bestTestCases = new ArrayList<>();
		
		for(List<?extends IOperand> possibleNextTestCase : possibleNextTestCases) {
			int newlyCoveredTupels = 0;
			for(List<?extends IOperand> uncoveredTupel : uncoveredTupels) {
				if(possibleNextTestCase.containsAll(uncoveredTupel)) {
					newlyCoveredTupels++;
				}
			}
			
			if(newlyCoveredTupels > max) {
				bestTestCases.clear();
				max = newlyCoveredTupels;
			}
			if(newlyCoveredTupels == max) {
				bestTestCases.add(possibleNextTestCase);
			}
		}
		
		if(bestTestCases.size() > 0) {
			return (List<Selection>)bestTestCases.get(randomGenerator.nextInt(bestTestCases.size()));
		} else {
			return null;
		}
	}

	private boolean removeNewlyCoveredTupels(List<?extends IOperand> newTest, List<List<?extends IOperand>> uncoveredTupels) {
		if(newTest == null) {
			return false;
		}
		
		List<List<?extends IOperand>> selectionsToRemove = new ArrayList<>();
		
		for(List<?extends IOperand> u : uncoveredTupels) {
			if(newTest.containsAll(u)) {
				selectionsToRemove.add(u);
			}
		}
		
		uncoveredTupels.removeAll(selectionsToRemove);
		
		return selectionsToRemove.size() > 0;
	}

	private List<IOperand> generateNextTestCase(List<List<?extends IOperand>> uncoveredTupels) {
		// from http://testingeducation.org/BBST/testdesign/Cohen_AETG_System.pdf
		// 1. Choose a parameter f and a value l for f such that that parameter value appears in the greatest number of uncovered pairs.
		// 2. Let f1 =	f. Then choose a random order for the remaining parameters. Then we	have an order for all k parameters 
		// f1, ... fk
		Selection l = (Selection)getMostUnusedSelection(uncoveredTupels);
		if(l == null) {
			return Collections.emptyList();
		}
		List<ClassificationVariable> listF = new ArrayList<>();
		List<IOperand> listV = new ArrayList<>();
		listF.add(l.getRootElement());
		listV.add(l);
		
		while(roots.size() > listF.size()) {
			int index = randomGenerator.nextInt(roots.size());
			ClassificationVariable v = roots.get(index);
			if(!listF.contains(v)) {
				listF.add(v);
			}
		}
		
		// 3. Assume that values have been selected for parameters f 1,...,fj. For 1 <=i<=j, let the selected value for f
		// i be called v i. Then choose a value	vj+1 for fj+1 as follows. For each possible value v for	fj,	find the number 
		// of new pairs in the set of pairs {fj+1 =	v and fi=vi for 1 <= i <= j}. Then let vj+1 be one of the values 
		// that appeared in the greatest number of new pairs 
		for(int i = 1; i < listF.size(); i++) {
			List<IOperand> bestV = new ArrayList<>();
			int max = 0;
			
			ClassificationVariable fi = listF.get(i);
			List<?extends IOperand> valuesForFi = getSelectableValues(listV, leafsByRoot.get(fi));
			for(IOperand v : valuesForFi) {
				int newlyCoveredTupels = calculateNewlyCoveredTupels(listV, v, uncoveredTupels);
				
				if(newlyCoveredTupels > max) {
					max = newlyCoveredTupels;
					bestV.clear();
				}
				if(newlyCoveredTupels == max) {
					bestV.add(v);
				}
			}
			
			// if there are multiple configurations with the same coverage of new tupels, choose a random one
			if(bestV.size() == 0) {
//				solver.enableLogging();
//				solver.isAlwaysForbidden(listV, leafsByRoot);
				notifyNewState("Error selecting value for " + fi + " already chosen values: " + listV + ", possible values in this step are " + valuesForFi);
				return null;
			} else {
				listV.add(bestV.get(randomGenerator.nextInt(bestV.size())));
			}
		}
		
		return listV;
	}

	private List<IOperand> getSelectableValues(List<?extends IOperand> chosenSelections, List<?extends IOperand> allValues) {
		List<IOperand> result = new ArrayList<>();
		
		List<IOperand> newSelection = new ArrayList<>();
		newSelection.addAll(chosenSelections);
		for(IOperand v : allValues) {
			newSelection.add(v);
			if(!solver.isAlwaysForbidden(newSelection)) {
				result.add(v);
			}
			
			newSelection.remove(v);
		}
		
		return result;
	}

	private int calculateNewlyCoveredTupels(List<?extends IOperand> listV, IOperand v, List<List<?extends IOperand>> uncoveredTupels) {
		List<IOperand> allSelections = new ArrayList<>(listV.size() + 1);
		allSelections.addAll(listV);
		allSelections.add(v);
		int count = 0;
		
		for(List<?extends IOperand> s : uncoveredTupels) {
			if(allSelections.containsAll(s)) {
				count++;
			}
		}
		return count;
		
	}
	
	List<List<?extends IOperand>> generateAllValidTupels(List<ClassificationVariable> remainingRoots) {
		List<List<?extends IOperand>> result = new ArrayList<>();
		
		for(List<?extends IOperand> o : generateAllTupels(new ArrayList<>(remainingRoots))) {
			if(o.size() == tCoverage && !solver.isAlwaysForbidden(o)) {
				result.add(o);
			}
		}
		
		return result;
	}
	
	List<List<?extends IOperand>> generateAllTupels(List<ClassificationVariable> remainingRoots) {
		List<List<?extends IOperand>> result = new ArrayList<>();
		if(remainingRoots.size() > 0) {
			ClassificationVariable currentRoot = remainingRoots.remove(0);
			List<List<?extends IOperand>> lowerLevelTupels = generateAllTupels(remainingRoots);
			
			for(List<?extends IOperand> tupel : lowerLevelTupels) {
				result.add(tupel);
				if(tupel.size() < tCoverage) {
					for(IOperand newValue : leafsByRoot.get(currentRoot)) {
						List<IOperand> newTupel = new ArrayList<>(tupel);
						newTupel.add(newValue);
						if(!solver.isAlwaysForbidden(newTupel)) {
							result.add(newTupel);
						}
					}
				}
			}
			for(IOperand newValue : leafsByRoot.get(currentRoot)) {
				List<IOperand> newTupel = Arrays.asList(newValue);
				if(!solver.isAlwaysForbidden(newTupel)) {
					result.add(newTupel);
				}
			}
		} else {
			return Collections.emptyList();
		}
		return result;
	}
	
}
