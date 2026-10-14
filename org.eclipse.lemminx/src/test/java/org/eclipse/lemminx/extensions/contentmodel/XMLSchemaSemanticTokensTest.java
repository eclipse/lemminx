/*******************************************************************************
* Copyright (c) 2026 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel;

import static org.eclipse.lemminx.XMLAssert.st;
import static org.eclipse.lemminx.XMLAssert.testSemanticTokensFor;

import org.eclipse.lemminx.settings.capabilities.ServerCapabilitiesConstants;
import org.junit.jupiter.api.Test;

/**
 * Tests for semantic tokens based on XML Schema (required attributes).
 */
public class XMLSchemaSemanticTokensTest extends BaseFileTempTest {

	private static final int PROPERTY = ServerCapabilitiesConstants.TOKEN_TYPE_PROPERTY;
	private static final int TYPE = ServerCapabilitiesConstants.TOKEN_TYPE_TYPE;
	private static final int READONLY = ServerCapabilitiesConstants.TOKEN_MODIFIER_READONLY;

	private static final String XSD_FILE_URI = "src/test/resources/team.xml";

	private static final String TEAM_HEADER = "<team xmlns=\"team_namespace\"\n" + //
			"      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
			"      xsi:schemaLocation=\"team_namespace xsd/team.xsd\">\n";

	private static final String TEAM_FOOTER = "</team>";

	// ------------------- XSD: required attributes on member -------------------

	@Test
	public void requiredAttributesOnMember() {
		String xml = TEAM_HEADER + //
				"  <member name=\"John\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills></skills>\n" + //
				"    <focus><server language=\"Java\" /></focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(3, 3, 6, TYPE, 0), // 'member' open tag
				st(3, 10, 4, PROPERTY, READONLY), // 'name'
				st(3, 22, 4, PROPERTY, READONLY), // 'role'
				st(3, 39, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(4, 5, 6, TYPE, 0), // 'skills' open tag
				st(4, 14, 6, TYPE, 0), // 'skills' close tag
				st(5, 5, 5, TYPE, 0), // 'focus' open tag

				st(5, 19, 8, PROPERTY, READONLY), // 'language'
				st(5, 39, 5, TYPE, 0), // 'focus' close tag
				st(6, 4, 6, TYPE, 0), // 'member' close tag
				st(7, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void noTokensWithoutSchema() {
		String xml = "<root attr1=\"value1\" attr2=\"value2\">\n" + //
				"  <child attr3=\"value3\" />\n" + //
				"</root>";
		testSemanticTokensFor(xml //
		);
	}

	@Test
	public void optionalAttributeNotHighlighted() {
		String xml = TEAM_HEADER.replace(">", "") + //
				"      name=\"myteam\">\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(4, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void clientElementRequiredIde() {
		String xml = TEAM_HEADER + //
				"  <member name=\"John\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills></skills>\n" + //
				"    <focus><client ide=\"Eclipse\" /></focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(3, 3, 6, TYPE, 0), // 'member' open tag
				st(3, 10, 4, PROPERTY, READONLY), // 'name'
				st(3, 22, 4, PROPERTY, READONLY), // 'role'
				st(3, 39, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(4, 5, 6, TYPE, 0), // 'skills' open tag
				st(4, 14, 6, TYPE, 0), // 'skills' close tag
				st(5, 5, 5, TYPE, 0), // 'focus' open tag

				st(5, 19, 3, PROPERTY, READONLY), // 'ide'
				st(5, 37, 5, TYPE, 0), // 'focus' close tag
				st(6, 4, 6, TYPE, 0), // 'member' close tag
				st(7, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void multipleMembers() {
		String xml = TEAM_HEADER + //
				"  <member name=\"John\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills></skills>\n" + //
				"    <focus><server language=\"Java\" /></focus>\n" + //
				"  </member>\n" + //
				"  <member name=\"Jane\" role=\"manager\" badgeNumber=\"2\">\n" + //
				"    <skills></skills>\n" + //
				"    <focus><server language=\"Go\" /></focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(3, 3, 6, TYPE, 0), // first 'member' open tag
				st(3, 10, 4, PROPERTY, READONLY), // first member 'name'
				st(3, 22, 4, PROPERTY, READONLY), // first member 'role'
				st(3, 39, 11, PROPERTY, READONLY), // first member 'badgeNumber'
				st(4, 5, 6, TYPE, 0), // 'skills' open tag
				st(4, 14, 6, TYPE, 0), // 'skills' close tag
				st(5, 5, 5, TYPE, 0), // 'focus' open tag

				st(5, 19, 8, PROPERTY, READONLY), // first server 'language'
				st(5, 39, 5, TYPE, 0), // 'focus' close tag
				st(6, 4, 6, TYPE, 0), // first 'member' close tag
				st(7, 3, 6, TYPE, 0), // second 'member' open tag
				st(7, 10, 4, PROPERTY, READONLY), // second member 'name'
				st(7, 22, 4, PROPERTY, READONLY), // second member 'role'
				st(7, 37, 11, PROPERTY, READONLY), // second member 'badgeNumber'
				st(8, 5, 6, TYPE, 0), // 'skills' open tag
				st(8, 14, 6, TYPE, 0), // 'skills' close tag
				st(9, 5, 5, TYPE, 0), // 'focus' open tag

				st(9, 19, 8, PROPERTY, READONLY), // second server 'language'
				st(9, 37, 5, TYPE, 0), // 'focus' close tag
				st(10, 4, 6, TYPE, 0), // second 'member' close tag
				st(11, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void selfClosingElementWithRequiredAttributes() {
		String xml = TEAM_HEADER + //
				"  <member name=\"John\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills />\n" + //
				"    <focus>\n" + //
				"      <server language=\"Java\" />\n" + //
				"    </focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(3, 3, 6, TYPE, 0), // 'member' open tag
				st(3, 10, 4, PROPERTY, READONLY), // 'name'
				st(3, 22, 4, PROPERTY, READONLY), // 'role'
				st(3, 39, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(4, 5, 6, TYPE, 0), // 'skills' open tag (self-closing)
				st(5, 5, 5, TYPE, 0), // 'focus' open tag
				st(6, 14, 8, PROPERTY, READONLY), // 'language'
				st(7, 6, 5, TYPE, 0), // 'focus' close tag
				st(8, 4, 6, TYPE, 0), // 'member' close tag
				st(9, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void elementWithNoAttributes() {
		String xml = TEAM_HEADER + //
				"  <member name=\"John\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills><skill>Java</skill></skills>\n" + //
				"    <focus><server language=\"Java\" /></focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(3, 3, 6, TYPE, 0), // 'member' open tag
				st(3, 10, 4, PROPERTY, READONLY), // 'name'
				st(3, 22, 4, PROPERTY, READONLY), // 'role'
				st(3, 39, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(4, 5, 6, TYPE, 0), // 'skills' open tag
				// 'skill' NOT highlighted (inside sequence minOccurs=0, optional)
				st(4, 33, 6, TYPE, 0), // 'skills' close tag
				st(5, 5, 5, TYPE, 0), // 'focus' open tag

				st(5, 19, 8, PROPERTY, READONLY), // 'language'
				st(5, 39, 5, TYPE, 0), // 'focus' close tag
				st(6, 4, 6, TYPE, 0), // 'member' close tag
				st(7, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void emptyDocument() {
		testSemanticTokensFor("" //
		);
	}

	@Test
	public void documentWithOnlyProlog() {
		testSemanticTokensFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" //
		);
	}

	@Test
	public void noTokensWithoutSchemaMultipleElements() {
		String xml = "<root>\n" + //
				"  <item id=\"1\" name=\"test\" />\n" + //
				"  <item id=\"2\" name=\"other\" />\n" + //
				"</root>";
		testSemanticTokensFor(xml //
		);
	}

	@Test
	public void mixOfRequiredAndOptionalOnTeam() {
		String xml = TEAM_HEADER.replace(">", "") + //
				"      name=\"myteam\" version=\"1\">\n" + //
				"  <member name=\"John\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills></skills>\n" + //
				"    <focus><server language=\"Java\" /></focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(4, 3, 6, TYPE, 0), // 'member' open tag
				st(4, 10, 4, PROPERTY, READONLY), // 'name'
				st(4, 22, 4, PROPERTY, READONLY), // 'role'
				st(4, 39, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(5, 5, 6, TYPE, 0), // 'skills' open tag
				st(5, 14, 6, TYPE, 0), // 'skills' close tag
				st(6, 5, 5, TYPE, 0), // 'focus' open tag
				st(6, 19, 8, PROPERTY, READONLY), // 'language'
				st(6, 39, 5, TYPE, 0), // 'focus' close tag
				st(7, 4, 6, TYPE, 0), // 'member' close tag
				st(8, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void requiredAttributeOnFirstLine() {
		String xml = "<team xmlns=\"team_namespace\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"team_namespace xsd/team.xsd\">\n" + //
				"  <member name=\"A\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills></skills>\n" + //
				"    <focus><server language=\"Java\" /></focus>\n" + //
				"  </member>\n" + //
				"</team>";
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(1, 3, 6, TYPE, 0), // 'member' open tag
				st(1, 10, 4, PROPERTY, READONLY), // 'name'
				st(1, 19, 4, PROPERTY, READONLY), // 'role'
				st(1, 36, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(2, 5, 6, TYPE, 0), // 'skills' open tag
				st(2, 14, 6, TYPE, 0), // 'skills' close tag
				st(3, 5, 5, TYPE, 0), // 'focus' open tag
				st(3, 19, 8, PROPERTY, READONLY), // 'language'
				st(3, 39, 5, TYPE, 0), // 'focus' close tag
				st(4, 4, 6, TYPE, 0), // 'member' close tag
				st(5, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void multipleSkills() {
		String xml = TEAM_HEADER + //
				"  <member name=\"John\" role=\"developer\" badgeNumber=\"1\">\n" + //
				"    <skills>\n" + //
				"      <skill>Java</skill>\n" + //
				"      <skill>Node</skill>\n" + //
				"      <skill>XML</skill>\n" + //
				"    </skills>\n" + //
				"    <focus><server language=\"Java\" /></focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(3, 3, 6, TYPE, 0), // 'member' open tag
				st(3, 10, 4, PROPERTY, READONLY), // 'name'
				st(3, 22, 4, PROPERTY, READONLY), // 'role'
				st(3, 39, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(4, 5, 6, TYPE, 0), // 'skills' open tag
				// 'skill' NOT highlighted (inside sequence minOccurs=0, optional)
				st(8, 6, 6, TYPE, 0), // 'skills' close tag
				st(9, 5, 5, TYPE, 0), // 'focus' open tag

				st(9, 19, 8, PROPERTY, READONLY), // 'language'
				st(9, 39, 5, TYPE, 0), // 'focus' close tag
				st(10, 4, 6, TYPE, 0), // 'member' close tag
				st(11, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void deltaLineAcrossMultipleLines() {
		String xml = TEAM_HEADER + //
				"  <member\n" + //
				"    name=\"John\"\n" + //
				"    role=\"developer\"\n" + //
				"    badgeNumber=\"1\">\n" + //
				"    <skills></skills>\n" + //
				"    <focus><server language=\"Java\" /></focus>\n" + //
				"  </member>\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(3, 3, 6, TYPE, 0), // 'member' open tag
				st(4, 4, 4, PROPERTY, READONLY), // 'name'
				st(5, 4, 4, PROPERTY, READONLY), // 'role'
				st(6, 4, 11, PROPERTY, READONLY), // 'badgeNumber'
				st(7, 5, 6, TYPE, 0), // 'skills' open tag
				st(7, 14, 6, TYPE, 0), // 'skills' close tag
				st(8, 5, 5, TYPE, 0), // 'focus' open tag
				st(8, 19, 8, PROPERTY, READONLY), // 'language'
				st(8, 39, 5, TYPE, 0), // 'focus' close tag
				st(9, 4, 6, TYPE, 0), // 'member' close tag
				st(10, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	@Test
	public void onlyRequiredNotImplied() {
		String xml = "<team xmlns=\"team_namespace\"\n" + //
				"      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
				"      xsi:schemaLocation=\"team_namespace xsd/team.xsd\"\n" + //
				"      version=\"1\"\n" + //
				"      name=\"myteam\">\n" + //
				TEAM_FOOTER;
		testSemanticTokensFor(xml, XSD_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'team' open tag
				st(5, 2, 4, TYPE, 0) // 'team' close tag
		);
	}

	// ------------------- XSD: Element optionality -------------------

	private static final String OPTIONALITY_FILE_URI = "src/test/resources/optionality.xml";

	private static final String OPTIONALITY_HEADER = "<root xmlns=\"optionality_ns\"\n" + //
			"      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + //
			"      xsi:schemaLocation=\"optionality_ns xsd/optionality.xsd\"";

	@Test
	public void requiredElementHighlighted() {
		String xml = OPTIONALITY_HEADER + " id=\"1\">\n" + //
				"  <required>value</required>\n" + //
				"  <choiceA>value</choiceA>\n" + //
				"</root>";
		testSemanticTokensFor(xml, OPTIONALITY_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'root' open tag
				st(2, 62, 2, PROPERTY, READONLY), // 'id' required attr
				st(3, 3, 8, TYPE, 0), // 'required' open tag
				st(3, 19, 8, TYPE, 0), // 'required' close tag
				st(5, 2, 4, TYPE, 0) // 'root' close tag
		);
	}

	@Test
	public void optionalElementNotHighlighted() {
		String xml = OPTIONALITY_HEADER + " id=\"1\">\n" + //
				"  <required>value</required>\n" + //
				"  <optional>value</optional>\n" + //
				"  <choiceA>value</choiceA>\n" + //
				"</root>";
		testSemanticTokensFor(xml, OPTIONALITY_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'root' open tag
				st(2, 62, 2, PROPERTY, READONLY), // 'id' required attr
				st(3, 3, 8, TYPE, 0), // 'required' open tag
				st(3, 19, 8, TYPE, 0), // 'required' close tag
				// 'optional' NOT highlighted (minOccurs=0)
				st(6, 2, 4, TYPE, 0) // 'root' close tag
		);
	}

	@Test
	public void elementInOptionalGroupNotHighlighted() {
		String xml = OPTIONALITY_HEADER + " id=\"1\">\n" + //
				"  <required>value</required>\n" + //
				"  <inOptionalGroup>value</inOptionalGroup>\n" + //
				"  <choiceA>value</choiceA>\n" + //
				"</root>";
		testSemanticTokensFor(xml, OPTIONALITY_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'root' open tag
				st(2, 62, 2, PROPERTY, READONLY), // 'id' required attr
				st(3, 3, 8, TYPE, 0), // 'required' open tag
				st(3, 19, 8, TYPE, 0), // 'required' close tag
				// 'inOptionalGroup' NOT highlighted (parent sequence has minOccurs=0)
				st(6, 2, 4, TYPE, 0) // 'root' close tag
		);
	}

	@Test
	public void choiceElementsNotHighlighted() {
		String xml = OPTIONALITY_HEADER + " id=\"1\">\n" + //
				"  <required>value</required>\n" + //
				"  <choiceA>value</choiceA>\n" + //
				"</root>";
		testSemanticTokensFor(xml, OPTIONALITY_FILE_URI, //
				st(0, 1, 4, TYPE, 0), // 'root' open tag
				st(2, 62, 2, PROPERTY, READONLY), // 'id' required attr
				st(3, 3, 8, TYPE, 0), // 'required' open tag
				st(3, 19, 8, TYPE, 0), // 'required' close tag
				// 'choiceA' NOT highlighted (inside choice group)
				st(5, 2, 4, TYPE, 0) // 'root' close tag
		);
	}

}
