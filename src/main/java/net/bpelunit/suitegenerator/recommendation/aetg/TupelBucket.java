package net.bpelunit.suitegenerator.recommendation.aetg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;

public class TupelBucket {

	private List<List<?extends IOperand>> allTupels = new ArrayList<>();

	private Map<IOperand, List<List<?extends IOperand>>> buckets = new HashMap<>();

	public TupelBucket() {
	}
	
	public TupelBucket(List<List<? extends IOperand>> tupels) {
		addAll(tupels);
	}

	private void addAll(List<List<? extends IOperand>> tupels) {
		for(List<?extends IOperand> l : tupels) {
			add(l);
		}
	}

	public List<List<?extends IOperand>> getTupelsWithOperand(IOperand o) {
		return buckets.get(o);
	}
	
	public void add(List<?extends IOperand> tupel) {
		for(IOperand o : tupel) {
			List<List<? extends IOperand>> bucket = buckets.get(o);
			if(bucket == null) {
				bucket = new ArrayList<>();
				buckets.put(o, bucket);
			}
			bucket.add(tupel);
		}
		allTupels.add(tupel);
	}
	
	public void remove(List<?extends IOperand> tupel) {
		if(allTupels.remove(tupel)) {
			for(IOperand o : tupel) {
				List<List<? extends IOperand>> bucket = buckets.get(o);
				if(bucket != null) {
					bucket.remove(tupel);
				}
			}
		}
	}
	
	public int size() {
		return allTupels.size();
	}

	public boolean isEmpty() {
		return allTupels.isEmpty();
	}

	public Set<?extends IOperand> getAllOperands() {
		return buckets.keySet();
	}

	public void removeAll(List<List<? extends IOperand>> tupelsToRemove) {
		for(List<? extends IOperand> tupelToRemove : tupelsToRemove) {
			remove(tupelToRemove);
		}
	}
}
