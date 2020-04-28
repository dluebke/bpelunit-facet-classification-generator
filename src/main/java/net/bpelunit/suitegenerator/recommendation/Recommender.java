package net.bpelunit.suitegenerator.recommendation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.bpelunit.suitegenerator.datastructures.classification.Classification;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariableSelection;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.OperandCondition;
import net.bpelunit.suitegenerator.datastructures.variables.VariableLibrary;
import net.bpelunit.suitegenerator.statistics.IStatistics;
import net.bpelunit.suitegenerator.statistics.Selection;
import net.bpelunit.suitegenerator.util.Copy;

public abstract class Recommender implements IRecommender {

	protected IStatistics statistic;
	protected VariableLibrary variables;
	protected Classification classification;
	protected ConditionBundle forbidden;

	protected List<Recommendation> recommendations = null;
	protected List<IRecommenderStatusListener> listeners = new ArrayList<>();
	
	public Recommender() {
	}

	@Override
	public void addRecommenderStatusListener(IRecommenderStatusListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeRecommenderStatusListener(IRecommenderStatusListener listener) {
		listeners.remove(listener);
	}
	
	protected void notifyNewState(String description) {
		for(IRecommenderStatusListener l : listeners) {
			l.newState(description);
		}
	}
	
	protected void createRecommendations() {
		recommendations = new LinkedList<>();
		if(forbidden != null) {
			List<String> leafNames = new ArrayList<>();
			Collection<ClassificationVariableSelection> allLeaves = classification.getAllClassificationTreeLeaves();
			for(ClassificationVariableSelection c : allLeaves) {
				leafNames.add(c.getCompleteName());
			}
			
			List<String> varNames = new ArrayList<>();
			Set<OperandCondition> vars = forbidden.getVariables();
			for(OperandCondition o : vars) {
				varNames.add(o.getTag());
			}
			
			List<String> unknownVariables = new ArrayList<>();
			for(String varName : varNames) {
				if(!leafNames.contains(varName)) {
					unknownVariables.add(varName);
				}
			}
			
			if(unknownVariables.size() > 0) {
				throw new RuntimeException("Following variables in conditions are not in the classification tree: " + unknownVariables);
			}
		}
	}

	@Override
	public List<Recommendation> getRecommendations() {
		if (recommendations == null) {
			createRecommendations();
		}
		return recommendations;
	}

	protected Map<ClassificationVariable, List<Selection>> copyHashMap(
			HashMap<ClassificationVariable, List<Selection>> dest, Map<ClassificationVariable, List<Selection>> src) {
		for (ClassificationVariable cv : src.keySet()) {
			List<Selection> l = src.get(cv);
			List<Selection> neu = Copy.deepCopy(new LinkedList<Selection>(), l);
			dest.put(cv, neu);
		}
		return dest;
	}

	protected Selection findLeastUsed(List<Selection> list, boolean mayFindFault) {
		Selection least = list.get(0);
		for (Selection neu : list) {
			if (least.getNumUsages() > neu.getNumUsages() && !neu.getSelection().isFault()) {
				least = neu;
			}
		}
		return least;
	}

	@Override
	public void setClassificationData(IStatistics stat, VariableLibrary variables, Classification classification) {
		this.statistic = stat;
		this.variables = variables;
		this.classification = classification;
		this.forbidden = classification.getForbidden();
	}

}
