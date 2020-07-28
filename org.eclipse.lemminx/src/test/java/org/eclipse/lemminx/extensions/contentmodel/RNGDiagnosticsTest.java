/*******************************************************************************
* Copyright (c) 2020 Balduin Landolt and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Balduin Landolt - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.d;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

public class RNGDiagnosticsTest {
	
	@Test
	public void simpleValidRNGFile() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<?xml-model href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_all.rng\"" +
				"            type=\"application/xml\"\r\n" +
				"            schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\r\n" +
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" +
				"  <teiHeader>\r\n" +
				"      <fileDesc>\r\n" +
				"         <titleStmt>\r\n" +
				"            <title>Title</title>\r\n" +
				"         </titleStmt>\r\n" +
				"         <publicationStmt>\r\n" +
				"            <p>Publication Information</p>\r\n" +
				"         </publicationStmt>\r\n" +
				"         <sourceDesc>\r\n" +
				"            <p>Information about the source</p>\r\n" +
				"         </sourceDesc>\r\n" +
				"      </fileDesc>\r\n" +
				"  </teiHeader>\r\n" +
				"  <text>\r\n" +
				"      <body>\r\n" +
				"         <p n=\"1\">Some text here.</p>\r\n" +
				"      </body>\r\n" +
				"  </text>\r\n" +
				"</TEI>";
		testDiagnosticsDisabledValidation(xml);
	}
	
	@Test
	public void unexpectedElement() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<?xml-model href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_all.rng\"" +
				"            type=\"application/xml\"\r\n" +
				"            schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\r\n" +
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" +
				"  <teiHeader>\r\n" +
				"      <fileDesc><am/>\r\n" + //  <-- Element "am" not allowed here
				"         <titleStmt>\r\n" +
				"            <title>Title</title>\r\n" +
				"         </titleStmt>\r\n" +
				"         <publicationStmt>\r\n" +
				"            <p>Publication Information</p>\r\n" +
				"         </publicationStmt>\r\n" +
				"         <sourceDesc>\r\n" +
				"            <p>Information about the source</p>\r\n" +
				"         </sourceDesc>\r\n" +
				"      </fileDesc>\r\n" +
				"  </teiHeader>\r\n" +
				"  <text>\r\n" +
				"      <body>\r\n" +
				"         <p n=\"1\">Some text here.</p>\r\n" +
				"      </body>\r\n" +
				"  </text>\r\n" +
				"</TEI>";
		testDiagnosticsFor(xml, d(6, 17, 6, 22, XMLSchemaErrorCode.cvc_complex_type_2_4_a, 
						"element \"am\" not allowed here; expected element \"titleStmt\""));
	}
	
	@Test
	public void unexpectedAttribute() {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<?xml-model href=\"http://www.tei-c.org/release/xml/tei/custom/schema/relaxng/tei_all.rng\"" +
				"            type=\"application/xml\"\r\n" +
				"            schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\r\n" +
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" +
				"  <teiHeader foo=\"bar\">\r\n" +  // <-- attribute not allowed here
				"      <fileDesc>\r\n" +
				"         <titleStmt>\r\n" +
				"            <title>Title</title>\r\n" +
				"         </titleStmt>\r\n" +
				"         <publicationStmt>\r\n" +
				"            <p>Publication Information</p>\r\n" +
				"         </publicationStmt>\r\n" +
				"         <sourceDesc>\r\n" +
				"            <p>Information about the source</p>\r\n" +
				"         </sourceDesc>\r\n" +
				"      </fileDesc>\r\n" +
				"  </teiHeader>\r\n" +
				"  <text>\r\n" +
				"      <body>\r\n" +
				"         <p n=\"1\">Some text here.</p>\r\n" +
				"      </body>\r\n" +
				"  </text>\r\n" +
				"</TEI>";
		testDiagnosticsFor(xml, d(5, 19, 5, 24, XMLSchemaErrorCode.cvc_complex_type_3_2_2));
	}
	// TODO: get Range right

	// TODO: more tests for more potential errors

	

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", expected);
	}

	private static void testDiagnosticsDisabledValidation(String xml) {
		ContentModelSettings settings = XMLAssert.getContentModelSettings(true, false);
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", null, null, true, settings);
	}

}