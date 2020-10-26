/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lemminx.extensions.contentmodel;

import static java.lang.System.lineSeparator;
import static org.eclipse.lemminx.XMLAssert.ca;
import static org.eclipse.lemminx.XMLAssert.createFile;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.teOp;
import static org.eclipse.lemminx.XMLAssert.testCodeActionsFor;
import static org.eclipse.lemminx.XMLAssert.testDiagnosticsFor;

import java.util.Arrays;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lemminx.extensions.contentmodel.settings.XMLValidationSettings;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.ResourceOperationKind;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.WorkspaceEditCapabilities;
import org.junit.jupiter.api.Test;

/**
 * XML problems like noGrammar.
 *
 */
public class XMLProblemsTest {

	@Test
	public void noGrammarIgnore() throws BadLocationException {
		String xml = "<root /> ";
		testDiagnosticsFor(xml);
	}

	@Test
	public void noGrammarHint() throws BadLocationException {
		noGrammarHint(false);
	}

	@Test
	public void noGrammarHintSelfClose() throws BadLocationException {
		noGrammarHint(true);
	}

	private static void noGrammarHint(boolean selfClose) throws BadLocationException {
		String xml = selfClose ? "<root/>" : "<root></root>";
		Diagnostic d = new Diagnostic(r(0, 1, 0, 5), "No grammar constraints (DTD or XML Schema).",
				DiagnosticSeverity.Hint, "test.xml", XMLSyntaxErrorCode.NoGrammarConstraints.name());
		// Set noGrammar has 'hint'
		ContentModelSettings contentModelSettings = new ContentModelSettings();
		XMLValidationSettings problems = new XMLValidationSettings();
		problems.setNoGrammar("hint");
		contentModelSettings.setValidation(problems);

		testDiagnosticsFor(xml, null, null, null, false, contentModelSettings, d);

		SharedSettings settings = new SharedSettings();
		WorkspaceClientCapabilities workspace = new WorkspaceClientCapabilities();
		WorkspaceEditCapabilities workspaceEdit = new WorkspaceEditCapabilities();
		workspaceEdit.setResourceOperations(Arrays.asList(ResourceOperationKind.Create));
		workspace.setWorkspaceEdit(workspaceEdit);
		settings.getWorkspaceSettings().setCapabilities(workspace);

		// Code action to generate DTD, XSD
		String schemaTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + lineSeparator() + //
				"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
				"  <xs:element name=\"root\" />" + lineSeparator() + //
				"</xs:schema>";
		String dtdTemplate = "<!ELEMENT root EMPTY>";
		testCodeActionsFor(xml, d, null, settings,
				// XSD with xsi:noNamespaceSchemaLocation
				ca(d, //
						createFile("test.xsd", false), //
						teOp("test.xsd", 0, 0, 0, 0, //
								schemaTemplate), //
						teOp("test.xml", 0, 5, 0, 5, //
								" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
										" xsi:noNamespaceSchemaLocation=\"test.xsd\"")),
				// XSD with xml-model
				ca(d, //
						createFile("test.xsd", false), //
						teOp("test.xsd", 0, 0, 0, 0, //
								schemaTemplate), //
						teOp("test.xml", 0, 0, 0, 0, //
								"<?xml-model href=\"test.xsd\"?>" + lineSeparator())),
				// DTD with DOCTYPE
				ca(d, //
						createFile("test.dtd", false), //
						teOp("test.dtd", 0, 0, 0, 0, //
								dtdTemplate), //
						teOp("test.xml", 0, 0, 0, 0, //
								"<!DOCTYPE root SYSTEM \"test.dtd\">" + lineSeparator())),
				// DTD with xml-model
				ca(d, //
						createFile("test.dtd", false), //
						teOp("test.dtd", 0, 0, 0, 0, //
								dtdTemplate), //
						teOp("test.xml", 0, 0, 0, 0, //
								"<?xml-model href=\"test.dtd\"?>" + lineSeparator() //
						)));

	}
}
