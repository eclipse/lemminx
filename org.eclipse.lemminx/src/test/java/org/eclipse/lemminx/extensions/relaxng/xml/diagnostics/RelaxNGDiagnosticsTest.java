/*******************************************************************************
* Copyright (c) 2022 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.relaxng.xml.diagnostics;

import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.testDiagnosticsFor;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.extensions.relaxng.xml.validator.RelaxNGErrorCode;
import org.junit.jupiter.api.Test;

/**
 * XML Validation tests with RelaxNG by using xml-model processing instruction
 * association.
 *
 */
public class RelaxNGDiagnosticsTest extends AbstractCacheBasedTest {

	@Test
	public void out_of_context_element() throws Exception {
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI>\r\n" + // <-- ""element "TEI" not allowed here; expected element "TEI" or "teiCorpus"
								// (with xmlns="http://www.tei-c.org/ns/1.0")"
				"\r\n" + //
				"</TEI>";
		testDiagnosticsFor(xml, null, null, "src/test/resources/relaxng/test.xml", //
				d(1, 1, 4, RelaxNGErrorCode.out_of_context_element));
	}

	@Test
	public void incomplete_element_required_elements_missing_expected() throws Exception {
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" >\r\n" + //
				"   <teiHeader>\r\n" + //
				"      <fileDesc>\r\n" + //
				"         <titleStmt>\r\n" + //
				"            <title></title>\r\n" + //
				"         </titleStmt>\r\n" + //
				"         <publicationStmt></publicationStmt>\r\n" + // <-- "element "publicationStmt" incomplete;
																		// expected element "ab", "authority",
																		// "distributor", "p" or "publisher""
				"         <sourceDesc></sourceDesc>\r\n" + // <-- "element "sourceDesc" incomplete; expected element
															// "ab", "bibl", "biblFull", "biblStruct", "list",
															// "listApp", "listBibl", "listEvent", "listNym",
															// "listObject", "listOrg", "listPerson", "listPlace",
															// "listRelation", "listWit", "msDesc", "p",
															// "recordingStmt", "scriptStmt" or "table""
				"      </fileDesc>\r\n" + //
				"   </teiHeader>" + //
				"</TEI>";
		testDiagnosticsFor(xml, null, null, "src/test/resources/relaxng/test.xml", //
				d(7, 10, 25, RelaxNGErrorCode.incomplete_element_required_elements_missing_expected), //
				d(8, 10, 20, RelaxNGErrorCode.incomplete_element_required_elements_missing_expected), //
				d(1, 1, 4, RelaxNGErrorCode.incomplete_element_required_elements_missing_expected));
	}

	@Test
	public void invalid_attribute_name() throws Exception {
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" XXXX=\"\" >\r\n" + // <-- "attribute "XXXX" not allowed
																				// here; expected attribute "ana",
																				// "cert", "change", "copyOf",
																				// "corresp", "exclude", "facs", "n",
																				// "next", "prev", "rend", "rendition",
																				// "resp", "sameAs", "select", "source",
																				// "style", "subtype", "synch", "type",
																				// "version", "xml:base", "xml:id",
																				// "xml:lang" or "xml:space""
				"</TEI>";
		testDiagnosticsFor(xml, null, null, "src/test/resources/relaxng/test.xml", //
				d(1, 41, 45, RelaxNGErrorCode.invalid_attribute_name), //
				d(1, 1, 4, RelaxNGErrorCode.incomplete_element_required_element_missing));
	}

	@Test
	public void invalid_attribute_value() throws Exception {
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" next=\"\" >\r\n" + // <-- "value of attribute "next" is
																				// invalid; must be a URI matching the
																				// regular expression "\S+""
				"</TEI>";
		testDiagnosticsFor(xml, null, null, "src/test/resources/relaxng/test.xml", //
				d(1, 46, 48, RelaxNGErrorCode.invalid_attribute_value), //
				d(1, 1, 4, RelaxNGErrorCode.incomplete_element_required_element_missing));
	}

	@Test
	public void invalid_attribute_value_enum() throws Exception {
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" >\r\n" + //
				"   <teiHeader cert=\"XXXX\" >\r\n" + // <-- "value of attribute "cert" is invalid; must be a
														// floating-point number or must be equal to "high", "low",
														// "medium" or "unknown""
				"      <fileDesc>\r\n" + //
				"      </fileDesc>\r\n" + //
				"   </teiHeader>" + //
				"</TEI>";
		testDiagnosticsFor(xml, null, null, "src/test/resources/relaxng/test.xml", //
				d(2, 19, 25, RelaxNGErrorCode.invalid_attribute_value), //
				d(3, 7, 15, RelaxNGErrorCode.incomplete_element_required_element_missing), //
				d(1, 1, 4, RelaxNGErrorCode.incomplete_element_required_elements_missing_expected));
	}

	@Test
	public void multiple_invalid_attribute() throws Exception {
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" \r\n" + //
				"    bad1=\"\"\r\n" + // <-- attribute "bad1" not allowed here; expected attribute "ana", "cert"..
				"    ana=\"\" \r\n" + // <-- value of attribute "ana" is invalid;...
				"    bad2=\"\"  \r\n" + // <-- attribute "bad2" not allowed here; expected attribute "ana", "cert"..
				"    version=\"\">\r\n" + // <-- value of attribute "version" is invalid;...
				"</TEI>";
		testDiagnosticsFor(xml, null, null, "src/test/resources/relaxng/test.xml", //
				d(2, 4, 8, RelaxNGErrorCode.invalid_attribute_name), //
				d(3, 8, 10, RelaxNGErrorCode.invalid_attribute_value), //
				d(4, 4, 8, RelaxNGErrorCode.invalid_attribute_name), //
				d(5, 12, 14, RelaxNGErrorCode.invalid_attribute_value), //
				d(1, 1, 4, RelaxNGErrorCode.incomplete_element_required_element_missing));
	}

	@Test
	public void required_attributes_missing() throws Exception {
		String xml = "<?xml-model href=\"tei_all.rng\" ?>\r\n" + //
				"<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\r\n" + //
				"	<teiHeader>\r\n" + //
				"		<fileDesc>\r\n" + //
				"			<titleStmt>\r\n" + //
				"				<title>\r\n" + //
				"				</title>\r\n" + //
				"			</titleStmt>\r\n" + //
				"			<publicationStmt>\r\n" + //
				"				<ab></ab>\r\n" + //
				"			</publicationStmt>\r\n" + //
				"			<sourceDesc>\r\n" + //
				"				<ab></ab>\r\n" + //
				"			</sourceDesc>\r\n" + //
				"		</fileDesc>\r\n" + //
				"		<encodingDesc>\r\n" + //
				"			<appInfo>\r\n" + //
				"				<application bad=\"\">\r\n" + // <-- "attribute "bad" not allowed here;
																// <-- "element "application" missing required
																// attributes "ident" and "version""
				"				</application>\r\n" + //
				"			</appInfo>\r\n" + //
				"		</encodingDesc>\r\n" + //
				"	</teiHeader>\r\n" + //
				"</TEI>";
		testDiagnosticsFor(xml, null, null, "src/test/resources/relaxng/test.xml", //
				d(17, 17, 20, RelaxNGErrorCode.invalid_attribute_name), //
				d(17, 5, 16, RelaxNGErrorCode.required_attributes_missing), //
				d(17, 5, 16, RelaxNGErrorCode.incomplete_element_required_elements_missing_expected), //
				d(1, 1, 4, RelaxNGErrorCode.incomplete_element_required_elements_missing_expected));
	}

}
