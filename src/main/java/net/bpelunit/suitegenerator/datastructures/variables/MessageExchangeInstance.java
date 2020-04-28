package net.bpelunit.suitegenerator.datastructures.variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.filter.ElementFilter;

import net.bpelunit.suitegenerator.config.Config;
import net.bpelunit.suitegenerator.datastructures.testcases.TestCase;

/**
 * MessageExchanges that can receive VariableInstances to replace variable-tags
 *
 */
public class MessageExchangeInstance extends BaseInstance {

	private Map<String, List<VariableSlot>> slots = new HashMap<>();
	private List<ITestCaseMetaData> testCaseMetaData = new ArrayList<>();

	MessageExchangeInstance(String name, Element content) {
		super(name, "No need for an instanceName for MessageExchanges", content);
		parseContent(content);
	}

	private void parseContent(Element content) {
		List<Element> toDetach = new LinkedList<>();
		for (Element var : content.getDescendants(new ElementFilter(Config.get().getVariableSlotTag(), Config.get().getGeneratorSpace()))) {
			Attribute nameAtt = var.getAttribute("name");
			if (nameAtt == null || nameAtt.getValue().isEmpty()) {
				Config.get().out().varSlotWithoutName(this.name);
				continue;
			}
			String name = nameAtt.getValue();
			if (name != null && !name.isEmpty()) {
				Element parent = var.getParentElement();
				if (parent != null) {
					toDetach.add(var);
					addSlot(new VariableSlot(var, name));
				}
			}
		}
	}

	private void addSlot(VariableSlot variableSlot) {
		List<VariableSlot> slotList = slots.get(variableSlot.getName());
		if (slotList == null) {
			slotList = new LinkedList<>();
			slots.put(variableSlot.getName(), slotList);
		}
		slotList.add(variableSlot);
	}

	public void insertVariables(Map<String, IVariableInstance> instanceForVarName, TestCase testCase) {
		for (String slotName : instanceForVarName.keySet()) {
			List<VariableSlot> slotList = slots.get(slotName);
			if (slotList != null) {
				IVariableInstance inst = instanceForVarName.get(slotName);
				for (VariableSlot vs : slotList) {
					IInsertedInstance ii = inst.replaceWithVariable(vs);
					if(ii != null) {
						ii.insertVariables(instanceForVarName);
					}
				}
			}
		}
	}

	public boolean hasSlot(String slotName) {
		return numberOfSlotsFor(slotName) != 0;
	}

	public int numberOfSlotsFor(String slotName) {
		List<VariableSlot> slotList = slots.get(slotName);
		return slotList != null ? slotList.size() : 0;
	}

	public void replaceTestCaseMetaData(TestCase testCase) {
		for (Element var : content.getDescendants(new ElementFilter(Config.get().getPlaceholderTestCaseIndex(), Config.get().getGeneratorSpace()))) {
			TestCaseIndexMetaData tciv = new TestCaseIndexMetaData(var);
			testCaseMetaData.add(tciv);
		}
		
		for(ITestCaseMetaData tcmd : testCaseMetaData) {
			tcmd.insertValue(testCase);
		}
	}

}
