package net.bpelunit.suitegenerator.solver;

import java.util.Comparator;
import java.util.Map.Entry;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;

class ClassificationVariableEntryComparator
		implements Comparator<Entry<ClassificationVariable, ?>> {

	@Override
	public int compare(Entry<ClassificationVariable, ?> o1, Entry<ClassificationVariable, ?> o2) {
		return o1.getKey().getName().compareTo(o2.getKey().getName());
	}

	
	
}
