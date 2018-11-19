package net.bpelunit.suitegenerator.recommendation.random;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.bpelunit.suitegenerator.datastructures.classification.Classification;
import net.bpelunit.suitegenerator.datastructures.variables.VariableLibrary;
import net.bpelunit.suitegenerator.recommendation.IRecommender;
import net.bpelunit.suitegenerator.recommendation.IRecommenderStatusListener;
import net.bpelunit.suitegenerator.recommendation.Recommendation;
import net.bpelunit.suitegenerator.recommendation.full.FullTestRecommender;
import net.bpelunit.suitegenerator.statistics.IStatistics;

public abstract class AbstractRandomRecommender implements IRecommender {

	private FullTestRecommender fullTestRecommender = new FullTestRecommender();
	private List<IRecommenderStatusListener> listeners = new ArrayList<>(); 
	
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
	
	@Override
	public Collection<Recommendation> getRecommendations() {
		notifyNewState("Generating all test cases...");
		List<Recommendation> allRecommendations = fullTestRecommender.getRecommendations();
		
		int recommendationCount = getMaximumRecommendations();
		
		
		notifyNewState("Choosing " + recommendationCount + " from " + allRecommendations.size() + " possible combinations");
		if(allRecommendations.size() <= recommendationCount) {
			notifyNewState("Only " + allRecommendations.size() + " possible recommendations!");
			notifyNewState("Returning " + allRecommendations.size() + " random recommendations");
			return allRecommendations;
		}
		
		long seed = System.currentTimeMillis();
		notifyNewState("Using random seed: " + seed);
		Random random = new Random(seed);
		if(recommendationCount > 100) {
			notifyNewState("!!! ATTENTION: When using the " + getClass().getSimpleName() + ", please make sure that BPELUnit has enough memory to execute the test suite successfully!");
		}
		
		List<Recommendation> randomRecommendations = new ArrayList<>(recommendationCount);
		while(randomRecommendations.size() < recommendationCount) {
			int i = random.nextInt(allRecommendations.size());
			Recommendation r = allRecommendations.remove(i);
			randomRecommendations.add(r);
		}
		
		notifyNewState("Returning " + randomRecommendations.size() + " random recommendations");
		
		return randomRecommendations;
	}

	protected abstract int getMaximumRecommendations(); 

	@Override
	public void setClassificationData(IStatistics stat, VariableLibrary variables, Classification classification) {
		fullTestRecommender.setClassificationData(stat, variables, classification);
	}

}
