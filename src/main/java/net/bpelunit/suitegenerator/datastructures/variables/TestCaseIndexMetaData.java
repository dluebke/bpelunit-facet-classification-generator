package net.bpelunit.suitegenerator.datastructures.variables;

import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.Parent;
import org.jdom2.Text;

import net.bpelunit.suitegenerator.datastructures.testcases.TestCase;

public class TestCaseIndexMetaData implements ITestCaseMetaData {

	private int digits = 0;
	private Element placeHolder;

	public TestCaseIndexMetaData(Element placeHolder) {
		this.placeHolder = placeHolder;
		Attribute digitsAttr = placeHolder.getAttribute("digits");
		if(digitsAttr != null) {
			try {
				digits = digitsAttr.getIntValue();
			} catch (DataConversionException e) {
				System.err.println("Could not parse value " + digitsAttr.getValue() + " of {" + placeHolder.getNamespaceURI() + "}" + placeHolder.getName() + "@name; using default");
			}
		}
	}
	
	@Override
	public String insertValue(TestCase tc) {
		StringBuilder result = new StringBuilder();
		result.append(Integer.toString(tc.getTestCaseIndex()));
		
		while(result.length() < digits) {
			result.insert(0, '0');
		}
		
		Parent parent = placeHolder.getParent();
		int indexOfMe = parent.indexOf(placeHolder);
		parent.addContent(indexOfMe, new Text(result.toString()));
		parent.removeContent(placeHolder);
		
		return result.toString();
	}

}
