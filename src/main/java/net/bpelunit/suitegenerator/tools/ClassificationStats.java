package net.bpelunit.suitegenerator.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jdom2.Element;

import net.bpelunit.suitegenerator.datastructures.classification.Classification;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationTree;
import net.bpelunit.suitegenerator.datastructures.classification.IClassificationElement;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.ICondition;
import net.bpelunit.suitegenerator.datastructures.variables.VariableLibrary;
import net.bpelunit.suitegenerator.reader.IClassificationReader;
import net.bpelunit.suitegenerator.reader.ICodeFragmentReader;
import net.bpelunit.suitegenerator.reader.ReaderFactory;
import net.bpelunit.suitegenerator.recommendation.full.FastFullTestRecommenderCountOnly;
import net.bpelunit.suitegenerator.recommendation.full.FullTestRecommenderCountOnly;
import net.bpelunit.suitegenerator.recommendation.full.FullTestRecommenderCountOnly.Mode;
import net.bpelunit.suitegenerator.statistics.Statistics;
import net.bpelunit.suitegenerator.util.CounterMap;

public class ClassificationStats {

	public static void main(String[] args) {
		ClassificationStats classificationStats = new ClassificationStats();
		
		classificationStats.printHeader();
		for(String fileName : args) {
			File classificationFile = new File(fileName);
			
			classificationStats.generate(classificationFile);
		}
		classificationStats.printFooter();
	}

	private void printFooter() {
		
	}

	private void printHeader() {
		
	}

	private void generate(File classificationFile) {
		IClassificationReader classificationReader = ReaderFactory.findReader(classificationFile);
		final VariableLibrary variableLibrary = new VariableLibrary();
		
		classificationReader.readAndEnrich(new ICodeFragmentReader() {
			@Override
			public VariableLibrary getVariables() {
				return variableLibrary;
			}
			
			@Override
			public Element getSkeletalStructure() {
				return null;
			}
		}, null);
		
		Statistics stat = new Statistics();
		stat.update(classificationReader.getClassification().getAllClassificationTreeLeaves(), variableLibrary);
		
		Classification classification = classificationReader.getClassification();
		String classificationStats = generateClassificationStats(classification.getTree());
		String forbiddenStats = generateForbiddenStats(classification.getForbidden());
		FastFullTestRecommenderCountOnly r = new FastFullTestRecommenderCountOnly();
		r.setClassificationData(stat, variableLibrary, classification);
		r.getRecommendations();
		long validTestCases = r.getValidTestCases();
		long forbiddenTestCases = r.getForbiddenTestCases();
		
		System.out.println(
				classificationFile.getName() + " & " + 
				classificationStats + " & " +
				forbiddenStats + " & " + 
				validTestCases + " & " +
				forbiddenTestCases + " & " +
			" \\\\");
	}

	private String generateClassificationStats(ClassificationTree tree) {
		List<IClassificationElement> classificationVariables = tree.getChildren();
		CounterMap<Integer> parameterValueCounts = new CounterMap<Integer>();
		
		for(IClassificationElement e : classificationVariables) {
			List<IClassificationElement> childrenToProcess = new ArrayList<>(e.getChildren());
			int leafCount = 0;
			
			while(childrenToProcess.size() > 0) {
				IClassificationElement child = childrenToProcess.remove(0);
				if(child.getChildren().size() == 0) {
					leafCount++;
				} else {
					childrenToProcess.addAll(child.getChildren());
				}
			}
			
			parameterValueCounts.inc(leafCount);
		}
		
		return printLatexCounts(parameterValueCounts);
	}

	private String generateForbiddenStats(ICondition forbidden) {
		List<ICondition> conditions = new ArrayList<>();
		if(forbidden instanceof ConditionBundle) {
			ConditionBundle c = (ConditionBundle)forbidden;
			conditions.addAll(c.getConditions());
		} else {
			conditions.add(forbidden);
		}
		
		CounterMap<Integer> varsInConstraintCounts = new CounterMap<Integer>();
		for(ICondition c : conditions) {
			int varCount = c.getVariables().size();
			varsInConstraintCounts.inc(varCount);
		}
		
		return printLatexCounts(varsInConstraintCounts);
	}

	private String printLatexCounts(CounterMap<Integer> elementCounts) {
		List<Integer> sortedSizes = new ArrayList<>(elementCounts.keySet());		
		Collections.sort(sortedSizes);
		List<String> forbiddenStatementStats = new ArrayList<>();
		for(int size : sortedSizes) {
			forbiddenStatementStats.add(size + "^" + elementCounts.get(size));
		}
		
		return "$" + String.join(" \\cdot ", forbiddenStatementStats) + "$";
	}
	
}
