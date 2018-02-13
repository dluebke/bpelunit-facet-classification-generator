package net.bpelunit.suitegenerator.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.bpelunit.suitegenerator.config.Config;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;
import net.bpelunit.suitegenerator.datastructures.conditions.OperandCondition;
import net.bpelunit.suitegenerator.statistics.Selection;

public class ConditionSATSolver {

	public class IOperandComparator implements Comparator<IOperand> {

		@Override
		public int compare(IOperand o1, IOperand o2) {
			return o1.getOpName().compareTo(o2.getOpName());
		}

	}

	private int maxCacheSize = 900;
	private static final boolean ENABLE_CACHE = true;

	private boolean doLogging = false;
	private ICondition forbidden = null;
	private Set<String> influencialRoots = new HashSet<>();
	private Map<ClassificationVariable, List<?extends IOperand>> influencialVariablesWithValues;

	private List<List<?extends IOperand>> forbiddenTupelCache = new ArrayList<>();
	private Map<IOperand, List<List<?extends IOperand>>> forbiddenTupelsByOperandCache = new HashMap<>();
	private List<List<?extends IOperand>> allowedTupelCache = new ArrayList<>();
	private Map<IOperand, List<List<?extends IOperand>>> allowedTupelsByOperandCache = new HashMap<>();
	private long cacheHits;
	private long cacheMisses;
	private IOperandComparator operandComparator = new IOperandComparator();
	
	
	public ConditionSATSolver(ICondition c, Map<ClassificationVariable, List<?extends IOperand>> allVariablesWithValues) {
		if(c == null || allVariablesWithValues == null) {
			throw new NullPointerException();
		}
		forbidden = c;
		
		for(OperandCondition oc : c.getVariables()) {
			influencialRoots.add(getRootName(oc));
		}
		
		influencialVariablesWithValues = new HashMap<>();
		for(ClassificationVariable v : allVariablesWithValues.keySet()) {
			String rootName = v.getName();
			if(influencialRoots.contains(rootName)) {
				influencialVariablesWithValues.put(v, allVariablesWithValues.get(v));
			}
		}
		
	}
	
	public void enableLogging() {
		doLogging = true;
	}
	
	public boolean isAlwaysForbidden(List<Selection> chosenValues, Selection additionalChosenValue, Map<ClassificationVariable, List<?extends IOperand>> allVariablesWithValues) {
		List<Selection> variablesChosen = new ArrayList<>(chosenValues.size() + 1);
		variablesChosen.addAll(chosenValues);
		variablesChosen.add(additionalChosenValue);
		
		return isAlwaysForbidden(variablesChosen);
	}
	
	public boolean isAlwaysForbidden(List<?extends IOperand> variablesChosen) {
		if(doLogging) {
			System.out.println("Solving for " + variablesChosen);
		}
		
		if(ENABLE_CACHE && cacheReturnsAllowed(variablesChosen)) {
			return false;
		}
		if(ENABLE_CACHE && cacheReturnsForbidden(variablesChosen)) {
			return true;
		}
		
		boolean alwaysForbidden = isAlwaysForbidden(forbidden, variablesChosen);
		if(ENABLE_CACHE) {
			addToCache(variablesChosen, alwaysForbidden);
		}
		return alwaysForbidden;
	}

	private void addToCache(List<? extends IOperand> variablesChosen, boolean forbidden) {
		List<?extends IOperand> tupelInCache = new ArrayList<>(variablesChosen);

		List<List<? extends IOperand>> tupelCache;
		Map<IOperand, List<List<? extends IOperand>>> tupelsByOperandCache;
		if(forbidden) {
			tupelCache = forbiddenTupelCache;
			tupelsByOperandCache = forbiddenTupelsByOperandCache;
		} else {
			tupelCache = allowedTupelCache;
			tupelsByOperandCache = allowedTupelsByOperandCache;
		}
		
		if(tupelCache.size() >= maxCacheSize) {
			return;
		}
		
		if(!forbidden) {
			Collections.sort(tupelInCache, operandComparator);
		}
		
		tupelCache.add(tupelInCache);
		for(IOperand o : tupelInCache) {
			List<List<? extends IOperand>> tupelsForOperand = tupelsByOperandCache.get(o);
			if(tupelsForOperand == null) {
				tupelsForOperand = new ArrayList<>();
				tupelsByOperandCache.put(o, tupelsForOperand);
			}
			tupelsForOperand.add(tupelInCache);
		}
	}

	private boolean cacheReturnsAllowed(List<? extends IOperand> variablesChosen) {
		if(variablesChosen.size() == 0) {
			return false;
		}
		
		List<?extends IOperand> sortedTupel = new ArrayList<>(variablesChosen);
		Collections.sort(sortedTupel, operandComparator);
		
		IOperand lastAddedOperand = variablesChosen.get(variablesChosen.size() - 1);
		List<List<? extends IOperand>> allowedTupels = allowedTupelsByOperandCache.get(lastAddedOperand);
		if(allowedTupels == null) {
			cacheMisses++;
			return false;
		}
		
		for(List<? extends IOperand> allowedTupel : allowedTupels) {
			if(allowedTupel.equals(sortedTupel)) {
				cacheHits++;
				return true;
			}
		}
		
		cacheMisses++;
		return false;
	}
	
	private boolean cacheReturnsForbidden(List<? extends IOperand> variablesChosen) {
		if(variablesChosen.size() == 0) {
			return false;
		}
		
		IOperand lastAddedOperand = variablesChosen.get(variablesChosen.size() - 1);
		List<List<? extends IOperand>> forbiddenTupels = forbiddenTupelsByOperandCache.get(lastAddedOperand);
		if(forbiddenTupels == null) {
			cacheMisses++;
			return false;
		}
		
		for(List<? extends IOperand> forbiddenTupel : forbiddenTupels) {
			if(forbiddenTupel.size() <= variablesChosen.size() && variablesChosen.containsAll(forbiddenTupel)) {
				cacheHits++;
				return true;
			}
		}
		
		cacheMisses++;
		return false;
	}

	private boolean isAlwaysForbidden(ICondition c, List<?extends IOperand> variablesChosen) {
		List<String> usedRoots = new ArrayList<>();
		List<List<? extends IOperand>> openVariables = new ArrayList<>();
		
		List<IOperand> influencialVariablesChosen = new ArrayList<>();  
		for(IOperand s : variablesChosen) {
			String rootName = getRootName(s);
			usedRoots.add(rootName);
			
			if(influencialRoots.contains(rootName)) {
				Literal l = new Literal(s.getOpName());
				influencialVariablesChosen.add(l);
			}
		}
		
		for(ClassificationVariable v : influencialVariablesWithValues.keySet()) {
			String rootName = v.getName();
			if(!usedRoots.contains(rootName) && influencialRoots.contains(rootName)) {
				openVariables.add(influencialVariablesWithValues.get(v));
			}
		}

		return isAlwaysForbiddenInternal(c, influencialVariablesChosen, openVariables);
	}

	private boolean isAlwaysForbiddenInternal(ICondition c, List<IOperand> influencialVariablesChosen, List<List<?extends IOperand>> openVariables) {
		if(openVariables.isEmpty()) {
			boolean isAlwaysForbidden = c.evaluate(influencialVariablesChosen);
			if(doLogging) {
				if(!isAlwaysForbidden) {
					System.out.println("Can solve " + " with " + influencialVariablesChosen);
					System.out.println(c.evaluate(influencialVariablesChosen));
				} else {
					System.out.println("CANNOT solve " + c + "  with " + influencialVariablesChosen);
					System.out.println();
				}
			}
			return isAlwaysForbidden;
		}
		
		boolean isAlwaysForbidden = true;
		List<IOperand> newVariablesChosen = influencialVariablesChosen;
		List<List<?extends IOperand>> newOpenVariables = new ArrayList<>(openVariables);
		List<? extends IOperand> v = newOpenVariables.remove(0);
		for(IOperand s : v) {
			newVariablesChosen.add(s);
			isAlwaysForbidden &= isAlwaysForbiddenInternal(c, newVariablesChosen, newOpenVariables);
			if(!isAlwaysForbidden) { // shortcut to improve performance
				return isAlwaysForbidden;
			}
			newVariablesChosen.remove(s);
		}
		return isAlwaysForbidden;
	}

	private String getRootName(IOperand o) {
		return o.getOpName().split(Config.get().getSelectionDelimeter())[0];
	}
	
	private String getRootName(OperandCondition o) {
		return o.getTag().split(Config.get().getSelectionDelimeter())[0];
	}

	public boolean isLoggingEnabled() {
		return doLogging;
	}

	public long getCacheHits() {
		return cacheHits;
	}
	
	public long getCacheMisses() {
		return cacheMisses;
	}

	public int getCacheSize() {
		return forbiddenTupelCache.size() + allowedTupelCache.size();
	}
}
