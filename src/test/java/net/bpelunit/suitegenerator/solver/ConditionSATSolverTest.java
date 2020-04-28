package net.bpelunit.suitegenerator.solver;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import net.bpelunit.suitegenerator.datastructures.classification.ClassificationTree;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariable;
import net.bpelunit.suitegenerator.datastructures.classification.ClassificationVariableSelection;
import net.bpelunit.suitegenerator.datastructures.classification.IClassificationElement;
import net.bpelunit.suitegenerator.datastructures.conditions.AND;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionBundle;
import net.bpelunit.suitegenerator.datastructures.conditions.ConditionParser;
import net.bpelunit.suitegenerator.datastructures.conditions.IOperand;
import net.bpelunit.suitegenerator.datastructures.conditions.OperandCondition;

public class ConditionSATSolverTest {

	@Test
	public void testOptimize() throws Exception {
		
		/*
		 * Error selecting value for PaperDescriptions already chosen values: [TargetAddress:NotSpecified: 1, CaseDescription:CaseDescription2: 0, CaseHandlingComment:NotSpecified: 18, AffectedMortgage:AssetId2: 0, CreationDate:NotSpecified: 17, UserId:User2: 0, Outcome:Success: 1], possible values in this step are []
[(((((((((((((((!(RelatedMessageId:NotSpecified) || !(CaseId:NotSpecified)) || !(AffectedMunicipality:NotSpecified)) || NomineeDepotId:Specified) || NomineeCertificateId:Specified) || BankCertificateId:Specified) || EvidenceNr:Specified) || Errichtungsdatum:Specified) || PaperDescriptionsType:MortgageRef) || SixVaultAssetId:Set) || SixVaultAccountId:Set) || !(Document:Valid)) || (ProcessFlow:SendClose || ProcessFlow:BookSendClose)) || ProcessFlow:BookClose) || (ProcessFlow:Notary && (((Address:NotSpecified && NotaryBpId:NotSpecified) || LRID:NotSpecified) || !(NotaryBpId:NotSpecified)))) || (ProcessFlow:LandRegistry && (LRID:NotSpecified || !(Address:NotSpecified)))), (PaperDescriptions:None && SixVaultAssetId:Set), (PaperDescriptions:None && SixVaultAccountId:Set), ((!(NotaryBpId:NotSpecified) || !(LRID:NotSpecified)) || !(Address:NotSpecified)), (PaperDescriptions:None ^ PaperDescriptionsType:NA), (AffectedMortgageDetail:NotSpecified && ((((!(NomineeDepotId:NotSpecified) || !(NomineeCertificateId:NotSpecified)) || !(BankCertificateId:NotSpecified)) || !(EvidenceNr:NotSpecified)) || !(Errichtungsdatum:NotSpecified))), (!(NotaryBpId:NotSpecified) && LRID:NotSpecified)]
N
		 */
		ClassificationTree t = new ClassificationTree();
		ClassificationVariable a = new ClassificationVariable("PaperDescriptions");
		t.addChild(a);
		a.addChild(new ClassificationVariableSelection("PaperDescriptions:None"));
		a.addChild(new ClassificationVariableSelection("PaperDescriptions:PaperDescriptions1"));
		ClassificationVariable b = new ClassificationVariable("SixVaultAssetId");
		t.addChild(b);
		b.addChild(new ClassificationVariableSelection("SixVaultAssetId:Set"));
		b.addChild(new ClassificationVariableSelection("SixVaultAssetId:NotSpecified"));
		ClassificationVariable c = new ClassificationVariable("SixVaultAssetId2");
		t.addChild(c);
		c.addChild(new ClassificationVariableSelection("SixVaultAssetId2:Set"));
		c.addChild(new ClassificationVariableSelection("SixVaultAssetId2:NotSpecified"));
		
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(new ConditionParser().parse("PaperDescriptions:None && SixVaultAssetId:Set"));
		ConditionSATSolver solver = new ConditionSATSolver();
		Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot = createLeafsByRoot(t.getChildren());
		assertTrue(solver.canBe(true, forbidden, leafsByRoot));
		assertTrue(solver.canBe(false, forbidden, leafsByRoot));
//		
		System.out.println(solver.generateAllValidTupels(leafsByRoot, forbidden, 3));
	}
	
	@Test
	public void testOptimize2() throws Exception {
		
		/*
		 * Error selecting value for PaperDescriptions already chosen values: [TargetAddress:NotSpecified: 1, CaseDescription:CaseDescription2: 0, CaseHandlingComment:NotSpecified: 18, AffectedMortgage:AssetId2: 0, CreationDate:NotSpecified: 17, UserId:User2: 0, Outcome:Success: 1], possible values in this step are []
Error selecting value for AffectedMunicipality already chosen values: [SixVaultAccountId:None: 17, PartnerCaseId:PartnerCaseId1: 8, AffectedMortgageDetail:NotSpecified: 6, NomineeDepotId:NotSpecified: 17, EvidenceNr:NotSpecified: 17, TargetAddress:NotSpecified: 1, PaperDescriptions:None: 15, CreationDate:NotSpecified: 17, CaseHandlingComment:CaseHandlingComment1: 0, UserId:User2: 0, SisHandlingInstruction:SisHandlingInstruction2: 5, NomineeCertificateId:NotSpecified: 17, BankCertificateId:NotSpecified: 17, Outcome:Success: 1], possible values in this step are []
[(((((((((((!(RelatedMessageId:NotSpecified) || !(CaseId:NotSpecified)) || AffectedMortgage:NotSpecified) || !(AffectedMunicipality:NotSpecified)) || Errichtungsdatum:Specified) || PaperDescriptionsType:MortgageRef) || SixVaultAssetId:Set) || !(Document:Valid)) || (ProcessFlow:SendClose || ProcessFlow:BookSendClose)) || ProcessFlow:BookClose) || (ProcessFlow:Notary && (((Address:NotSpecified && NotaryBpId:NotSpecified) || LRID:NotSpecified) || (!(NotaryBpId:NotSpecified) && CaseDescription:NotSpecified)))) || (ProcessFlow:LandRegistry && (LRID:NotSpecified || (!(Address:NotSpecified) && CaseDescription:NotSpecified)))), SixVaultAssetId:Set, ((!(NotaryBpId:NotSpecified) || !(LRID:NotSpecified)) || !(Address:NotSpecified)), !(PaperDescriptionsType:NA), !(Errichtungsdatum:NotSpecified), (!(NotaryBpId:NotSpecified) && LRID:NotSpecified)]

		 */
		ClassificationTree t = new ClassificationTree();
		
		addClassificationVariable(t, "Address", "Specified", "NotSpecified");
		addClassificationVariable(t, "AffectedMortgage", "Specified", "NotSpecified");
		addClassificationVariable(t, "AffectedMunicipality", "Specified", "NotSpecified");
		addClassificationVariable(t, "CaseDescription", "None", "PaperDescriptions1");
		addClassificationVariable(t, "CaseId", "Specified", "NotSpecified");
		addClassificationVariable(t, "Document", "Valid", "Invalid");
		addClassificationVariable(t, "Errichtungsdatum", "Specified", "NotSpecified");
		addClassificationVariable(t, "LRID", "Specified", "NotSpecified");
		addClassificationVariable(t, "NotaryBpId", "Specified", "NotSpecified");
		addClassificationVariable(t, "PaperDescriptions", "None", "PaperDescriptions1");
		addClassificationVariable(t, "PaperDescriptionsType", "MortgageRef", "OtherSecurities");
		addClassificationVariable(t, "ProcessFlow", "SendClose", "BookSendClose", "BookClose", "Notary", "LandRegistry");
		addClassificationVariable(t, "RelatedMessageId", "Specified", "NotSpecified");
		addClassificationVariable(t, "SixVaultAssetId", "Specified", "NotSpecified");
		addClassificationVariable(t, "SixVaultAssetId2", "Specified", "NotSpecified");
		
		
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(new ConditionParser().parse("(((((((((((!(RelatedMessageId:NotSpecified) || !(CaseId:NotSpecified)) || AffectedMortgage:NotSpecified) || !(AffectedMunicipality:NotSpecified)) || Errichtungsdatum:Specified) || PaperDescriptionsType:MortgageRef) || SixVaultAssetId:Set) || !(Document:Valid)) || (ProcessFlow:SendClose || ProcessFlow:BookSendClose)) || ProcessFlow:BookClose) || (ProcessFlow:Notary && (((Address:NotSpecified && NotaryBpId:NotSpecified) || LRID:NotSpecified) || (!(NotaryBpId:NotSpecified) && CaseDescription:NotSpecified)))) || (ProcessFlow:LandRegistry && (LRID:NotSpecified || (!(Address:NotSpecified) && CaseDescription:NotSpecified))))"));
		ConditionSATSolver solver = new ConditionSATSolver();
		Map<ClassificationVariable, List<? extends IOperand>> leafsByRoot = createLeafsByRoot(t.getChildren());
		assertTrue(solver.canBe(true, forbidden, leafsByRoot));
		assertTrue(solver.canBe(false, forbidden, leafsByRoot));
		System.out.println(solver.generateAllValidTupels(leafsByRoot, forbidden, 3));
	}
	
	private void addClassificationVariable(ClassificationTree t, String name, String... values) {
		ClassificationVariable a = new ClassificationVariable(name);
		t.addChild(a);
		for(String value : values) {
			a.addChild(new ClassificationVariableSelection(name + ":" + value));
		}
	}

	@Test
	public void testGenerate2Tupels() {
		ClassificationTree tree = new ClassificationTree();
		ClassificationVariable a = new ClassificationVariable("A");
		tree.addChild(a);
		a.addChild(new ClassificationVariableSelection("A:A1"));
		a.addChild(new ClassificationVariableSelection("A:A2"));
		ClassificationVariable b = new ClassificationVariable("B");
		tree.addChild(b);
		b.addChild(new ClassificationVariableSelection("B:B1"));
		b.addChild(new ClassificationVariableSelection("B:B2"));
		ClassificationVariable c = new ClassificationVariable("C");
		tree.addChild(c);
		c.addChild(new ClassificationVariableSelection("C:C1"));
		c.addChild(new ClassificationVariableSelection("C:C2"));
		
		
		ConditionSATSolver solver = new ConditionSATSolver();
		List<List<?extends IOperand>> pairs = solver.generateAllValidTupels(createLeafsByRoot(Arrays.asList(a, b, c)), new ConditionBundle(), 2);
		System.out.println(pairs);
	}

	@Test
	public void testGeneratePairsWithConstraints() {
		ClassificationTree tree = new ClassificationTree();
		ClassificationVariable a = new ClassificationVariable("A");
		tree.addChild(a);
		a.addChild(new ClassificationVariableSelection("A:A1"));
		a.addChild(new ClassificationVariableSelection("A:A2"));
		ClassificationVariable b = new ClassificationVariable("B");
		tree.addChild(b);
		b.addChild(new ClassificationVariableSelection("B:B1"));
		b.addChild(new ClassificationVariableSelection("B:B2"));
		ClassificationVariable c = new ClassificationVariable("C");
		tree.addChild(c);
		c.addChild(new ClassificationVariableSelection("C:C1"));
		c.addChild(new ClassificationVariableSelection("C:C2"));
		
		
		ConditionSATSolver solver = new ConditionSATSolver();
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(new OperandCondition("C:C2"));
		List<List<?extends IOperand>> pairs = solver.generateAllValidTupels(createLeafsByRoot(Arrays.asList(a, b, c)), forbidden, 2);
		System.out.println(pairs);
	}
	
	@Test
	public void testGenerate2TupelsWithConstraints() {
		List<ClassificationVariableSelection> variableSelections = new ArrayList<>();
		ClassificationTree tree = new ClassificationTree();
		for(String name : Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J")) {
			ClassificationVariable classificationVariable = new ClassificationVariable(name, true);
			tree.addChild(classificationVariable);
			for(String value : Arrays.asList("1", "2", "3", "4", "5", "6")) {
				ClassificationVariableSelection s = new ClassificationVariableSelection(name + ":" + name + value);
				classificationVariable.addChild(s);
				variableSelections.add(s);
			}
		}
		
		ConditionSATSolver solver = new ConditionSATSolver();
		
		ConditionBundle forbidden = new ConditionBundle();
		forbidden.addCondition(new OperandCondition("B:B2"));
		forbidden.addCondition(new AND(new OperandCondition("A:A1"), new OperandCondition("C:C2")));
		
		long start = System.currentTimeMillis();
		List<List<?extends IOperand>> pairs = solver.generateAllValidTupels(createLeafsByRoot(tree.getChildren()), forbidden, 2);
		long end = System.currentTimeMillis();
		System.out.println(pairs.size() + " tupels in "+ (end - start) + "ms");
	}
	
	private Map<ClassificationVariable, List<? extends IOperand>> createLeafsByRoot(List<IClassificationElement> classificationElements) {
		Map<ClassificationVariable, List<? extends IOperand>> result = new HashMap<>();
		
		for(IClassificationElement cv : classificationElements) {
			List<IOperand> list = new ArrayList<>();
			for (IClassificationElement ce : cv.getChildren()) {
				list.add(new Literal(ce.getName()));
			}
			result.put((ClassificationVariable) cv, list);
		}
		
		return result;
	}

}
