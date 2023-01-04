/*******************************************************************************
* Copyright (c) 2023 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.references;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.lemminx.extensions.references.settings.XMLReferenceExpression;
import org.eclipse.lemminx.extensions.references.settings.XMLReferences;
import org.eclipse.lemminx.extensions.references.settings.XMLReferencesSettings;

/**
 * {@link XMLReferencesSettings} for tests.
 * 
 */
public class XMLReferencesSettingsForTest {

	public static XMLReferencesSettings createXMLReferencesSettings() {
		XMLReferencesSettings referencesSettings = new XMLReferencesSettings();
		referencesSettings.setReferences(createReferences());
		return referencesSettings;
	}

	private static List<XMLReferences> createReferences() {
		List<XMLReferences> references = new ArrayList<>();

		XMLReferences tei = new XMLReferences();
		tei.setPattern("**/*tei.xml");
		references.add(tei);
		/*
		 * {
		 * "prefix": "#",
		 * "from": "@corresp",
		 * "to": "@xml:id"
		 * }
		 */
		XMLReferenceExpression corresp = new XMLReferenceExpression();
		corresp.setPrefix("#");
		corresp.setFrom("@corresp");
		corresp.setTo("@xml:id");

		/*
		 * {
		 * "prefix": "#",
		 * "from": "@target",
		 * "to": "@xml:id",
		 * "multiple": true
		 * }
		 */
		XMLReferenceExpression target = new XMLReferenceExpression();
		target.setPrefix("#");
		target.setFrom("@target");
		target.setTo("@xml:id");
		target.setMultiple(true);

		tei.setExpressions(Arrays.asList(corresp, target));

		/*
		 * {
		 * "from": "xref/@linkend",
		 * "to": "@id"
		 * }
		 */
		XMLReferences docbook = new XMLReferences();
		docbook.setPattern("**/*docbook.xml");
		XMLReferenceExpression linkend = new XMLReferenceExpression();
		linkend.setFrom("xref/@linkend");
		linkend.setTo("@id");
		docbook.setExpressions(Arrays.asList(linkend));
		references.add(docbook);

		/*
		 * {
		 * "from": "servlet-mapping/servlet-name/text()",
		 * "to": "servlet/servlet-name/text()"
		 * }
		 */
		XMLReferences web = new XMLReferences();
		web.setPattern("**/web.xml");
		XMLReferenceExpression servletName = new XMLReferenceExpression();
		servletName.setFrom("servlet-mapping/servlet-name/text()");
		servletName.setTo("servlet/servlet-name/text()");
		web.setExpressions(Arrays.asList(servletName));
		references.add(web);

		/*
		 * {
		 * "from": "@ref",
		 * "to": "bbb/text()",
		 * "multiple": true
		 * }
		 */
		XMLReferences attrToText = new XMLReferences();
		attrToText.setPattern("**/attr-to-text.xml");
		XMLReferenceExpression attrToTextExpr = new XMLReferenceExpression();
		attrToTextExpr.setFrom("@ref");
		attrToTextExpr.setTo("bbb/text()");
		attrToTextExpr.setMultiple(true);
		attrToText.setExpressions(Arrays.asList(attrToTextExpr));
		references.add(attrToText);

		/*
		 * {
		 * "from": "from/text",
		 * "to": "to/text()",
		 * "multiple": true
		 * }
		 */
		XMLReferences textToText = new XMLReferences();
		textToText.setPattern("**/text-to-text.xml");
		XMLReferenceExpression textToTextExpr = new XMLReferenceExpression();
		textToTextExpr.setFrom("from/text()");
		textToTextExpr.setTo("to/text()");
		textToTextExpr.setMultiple(true);
		textToText.setExpressions(Arrays.asList(textToTextExpr));
		references.add(textToText);

		
		return references;
	}

}
