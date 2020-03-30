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

import static org.eclipse.lemminx.XMLAssert.d;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.extensions.contentmodel.participants.DTDErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * DTD diagnostics services tests
 *
 */
public class DTDDiagnosticsTest {
	
	@Test
	public void MSG_ELEMENT_NOT_DECLARED() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \r\n" + //
				"<!DOCTYPE web-app\r\n" + //
				"   PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\"\r\n" + //
				"   \"http://java.sun.com/dtd/web-app_2_3.dtd\">\r\n" + //
				"\r\n" + //
				"<web-app>\r\n" + //
				"	<XXX></XXX>\r\n" + //
				"</web-app>";
		testDiagnosticsFor(xml, d(6, 2, 5, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED),
				d(5, 1, 8, DTDErrorCode.MSG_CONTENT_INVALID));
	}
	
	@Test
	public void MSG_CONTENT_INVALID() throws Exception {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"<!ELEMENT note (to,from,heading,body)>\r\n" + // 
				"    <!ELEMENT to (#PCDATA)>\r\n" + //
				"        <!ELEMENT from (#PCDATA)>\r\n" + // 
				"                <!ELEMENT heading (#PCDATA)>\r\n" + // 
				"            <!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + // 
				"	<from>Jani</from>\r\n" + // 
				"	<heading>Reminder</heading>\r\n" + // 
				"	<body>Don't forget me this weekend</body>\r\n" + // 
				"</note>";
		XMLAssert.testDiagnosticsFor(xml, d(8, 1, 5, DTDErrorCode.MSG_CONTENT_INVALID));
	}

	@Test
	public void MSG_ATTRIBUTE_NOT_DECLARED() throws Exception {
		String xml = "<?xml version=\"1.0\"?>\r\n" + //
				"<!DOCTYPE note [\r\n" + //
				"<!ELEMENT note (to,from,heading,body)>\r\n" + //
				"<!ELEMENT to (#PCDATA)>\r\n" + //
				"<!ELEMENT from (#PCDATA)>\r\n" + //
				"<!ELEMENT heading (#PCDATA)>\r\n" + //
				"<!ELEMENT body (#PCDATA)>\r\n" + //
				"]>\r\n" + //
				"<note>\r\n" + //
				"    <to></to>\r\n" + //
				"    <from XXXX=\"\" >Jani</from>\r\n" + // <- error 
				"    <heading>Reminder</heading>\r\n" + //
				"    <body>Don't forget me this weekend</body>\r\n" + //
				"</note> ";
		XMLAssert.testDiagnosticsFor(xml, d(10, 15, 17, DTDErrorCode.MSG_ATTRIBUTE_NOT_DECLARED));
	}

	@Test
	public void MSG_FIXED_ATTVALUE_INVALID() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE address [\r\n" + //
				"  <!ELEMENT address (company)*>\r\n" + //
				"  <!ELEMENT company (#PCDATA)>\r\n" + //
				"  <!ATTLIST company name NMTOKEN #FIXED \"tutorialspoint\">\r\n" + //
				"]>\r\n" + //
				"<address>\r\n" + //
				"  <company name=\"etutorialspoint\"" // <- error
				+ ">we are a free online teaching faculty</company>\r\n" + //
				"</address>";
		XMLAssert.testDiagnosticsFor(xml, d(7, 16, 33, DTDErrorCode.MSG_FIXED_ATTVALUE_INVALID));
	}

	@Test
	public void MSG_ATTRIBUTE_VALUE_NOT_IN_LIST() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE foo [\r\n" + //
				"  <!ELEMENT foo (bar)*>\r\n" + // 
				"  <!ELEMENT bar (#PCDATA)>\r\n" + // 
				"  <!ATTLIST bar fruit (one | two | three) #REQUIRED>\r\n" + // 
				"]>\r\n" + // 
				"<foo>\r\n" + // 
				"    <bar fruit=\"four\"" + // <- error
				">toto</bar>\r\n" + // 
				"</foo>";
		XMLAssert.testDiagnosticsFor(xml, d(7, 15, 21, DTDErrorCode.MSG_ATTRIBUTE_VALUE_NOT_IN_LIST));
	}

	@Test
	public void MSG_CONTENT_INCOMPLETE() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE foo [\r\n" + //
				"  <!ELEMENT foo (bar)>\r\n" + //
				"  <!ELEMENT bar (#PCDATA)>\r\n" + //
				"  <!ATTLIST bar fruit (one | two | three) #REQUIRED>\r\n" + //
				"]>\r\n" + //
				"<foo>\r\n" + //
				"    \r\n" + // <- error
				"</foo>";
		XMLAssert.testDiagnosticsFor(xml, d(6, 1, 4, DTDErrorCode.MSG_CONTENT_INCOMPLETE));
	}

	@Test
	public void MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE foo [\r\n" + //
				"  <!ELEMENT foo (bar)>\r\n" + // 
				"  <!ELEMENT bar (#PCDATA)>\r\n" + // 
				"  <!ATTLIST bar fruit (one | two | three) #REQUIRED>\r\n" + // 
				"]>\r\n" + //
				"<foo>\r\n" + // 
				"    <bar />\r\n" + // <- error ("fruit" attribute is missing)
				"</foo>";
		XMLAssert.testDiagnosticsFor(xml, d(7, 5, 8, DTDErrorCode.MSG_REQUIRED_ATTRIBUTE_NOT_SPECIFIED));
	}

	@Test
	public void MSG_ELEMENT_WITH_ID_REQUIRED() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ELEMENT Folks (Person*)>\r\n" + // 
				"	<!ELEMENT Person (Name,Email?)>\r\n" + // 
				"	<!ATTLIST Person Pin ID #REQUIRED>\r\n" + // 
				"	<!ATTLIST Person Friend IDREF #IMPLIED>\r\n" + // 
				"	<!ATTLIST Person Likes IDREFS #IMPLIED>\r\n" + //
				"	<!ELEMENT Name (#PCDATA)>\r\n" + //
				"	<!ELEMENT Email (#PCDATA)>\r\n" + //
				"	]>\r\n" + //
				"<Folks>\r\n" + //
				"    <Person Pin=\"id2\" Likes=\"vfg\"> \r\n" + // 
				"        <Name>Bob</Name>\r\n" + //
				"    </Person>\r\n" + //
				"</Folks>";
		XMLAssert.testDiagnosticsFor(xml, d(10, 1, 6, DTDErrorCode.MSG_ELEMENT_WITH_ID_REQUIRED));
	}
	
	@Test
	public void IDInvalidWithNamespaces() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ELEMENT Person EMPTY>\r\n" + //
				"	<!ATTLIST Person Pin ID #REQUIRED>\r\n" + //
				"	]>\r\n" + //
				"<Person Pin=\"7\" />"; // <- error on @Pin value
		XMLAssert.testDiagnosticsFor(xml, d(5, 12, 15, DTDErrorCode.IDInvalidWithNamespaces));
	}

	@Test
	public void IDREFInvalidWithNamespaces() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ELEMENT Person EMPTY>\r\n" + // 
				"	<!ATTLIST Person Friend IDREF #IMPLIED>\r\n" + // 
				"]>\r\n" + //
				"<Person Friend=\"\" />"; // <- error on @Friend value
		XMLAssert.testDiagnosticsFor(xml, d(5, 15, 17, DTDErrorCode.IDREFInvalidWithNamespaces));
	}
	
	@Test
	public void IDREFSInvalid() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ELEMENT Person EMPTY>\r\n" + //
				"	<!ATTLIST Person Likes IDREFS #IMPLIED>\r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(5, 14, 16, DTDErrorCode.IDREFSInvalid));
	}

	@Test
	public void MSG_MARKUP_NOT_RECOGNIZED_IN_DTD() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ELEMENT Person EMPTY>\r\n" + //
				"   Bad Value   " +
				"	<!ATTLIST Person Likes IDREFS #IMPLIED>\r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 24, 3, 16, DTDErrorCode.MSG_MARKUP_NOT_RECOGNIZED_IN_DTD));
	}

	@Test
	public void QuoteRequiredInPublicID() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!NOTATION    name PUBLIC asd > \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 27, 30, DTDErrorCode.QuoteRequiredInPublicID));
	}

	@Test
	public void QuoteRequiredInPublicID2() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!NOTATION name PUBLIC    >  \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 23, 24, DTDErrorCode.QuoteRequiredInPublicID));
	}

	@Test
	public void QuoteRequiredInSystemID() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!NOTATION name SYSTEM    >  \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 23, 24, DTDErrorCode.QuoteRequiredInSystemID));
	}

	@Test
	public void OpenQuoteMissingInDecl() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ENTITY asd >   \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 13, 14, DTDErrorCode.OpenQuoteMissingInDecl));
	}

	@Test
	public void SpaceRequiredAfterSYSTEM() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!NOTATION name SYSTEM>  \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 23, 24, DTDErrorCode.SpaceRequiredAfterSYSTEM));
	}

	@Test
	public void MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!NOTATION name>  \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 16, 17, DTDErrorCode.MSG_SPACE_REQUIRED_AFTER_NOTATION_NAME_IN_NOTATIONDECL));
	}

	@Test
	public void AttTypeRequiredInAttDef() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ATTLIST payment name BadType >  \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 24, 31, DTDErrorCode.AttTypeRequiredInAttDef));
	}

	@Test
	public void EntityDeclUnterminated() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ENTITY copyright \"Copyright W3Schools.\"  \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 42, 43, DTDErrorCode.EntityDeclUnterminated));
	}

	@Test
	public void NotationDeclUnterminated() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!NOTATION png PUBLIC \"PNG 1.0\" \"image/png\"  \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 44, 45, DTDErrorCode.NotationDeclUnterminated));
	}

	@Test
	public void EntityNotDeclared() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<!DOCTYPE article [\r\n" +
				"	<!ELEMENT article (#PCDATA)>\r\n" +
				"]>\r\n" +
				"<article>\r\n" +
				"	&nbsp;\r\n" +
				"</article>";

		XMLAssert.testDiagnosticsFor(xml, d(5, 1, 7, DTDErrorCode.EntityNotDeclared));
	}

	@Test
	public void ElementDeclUnterminated() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Person [\r\n" + //
				"	<!ELEMENT element-name (element-content) \r\n" + //
				"]>\r\n" + //
				"<Person Likes=\"\" />"; // <- error on @Likes value
		XMLAssert.testDiagnosticsFor(xml, d(2, 41, 42, DTDErrorCode.ElementDeclUnterminated));
	}

	@Test
	public void PEReferenceWithinMarkup() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Folks [\r\n" + //
				"	<!ENTITY % Folks \"(%bar;)*\"> \r\n" + // <- error on "(%bar;)*"
				"]>\r\n" + //
				"<Folks></Folks>"; // 
		XMLAssert.testDiagnosticsFor(xml, d(2, 18, 28, DTDErrorCode.PEReferenceWithinMarkup));
	}

	@Test
	public void MSG_ELEMENT_ALREADY_DECLARED() throws Exception {
		String xml = "<?xml version = \"1.0\"?>\r\n" + //
				"<!DOCTYPE Email [\r\n" + //
				"	<!ELEMENT Email (#PCDATA)> \r\n" + //
				"	<!ELEMENT Email (#PCDATA)> \r\n" + // <- error on 'ELEMENT'
				"]>\r\n" + //
				"<Email></Email>"; // 
		XMLAssert.testDiagnosticsFor(xml, d(3, 3, 10, DTDErrorCode.MSG_ELEMENT_ALREADY_DECLARED));
	}

	@Test
	public void testDoctypeDiagnosticsRefresh() throws Exception {
		//@formatter:off
		String xml = "<?xml version=\"1.0\"?>\n" + 
					"<!DOCTYPE student [\n" + 
					"  <!ELEMENT student (surname,id)>\n" + 
					"  <!ELEMENT surname (#PCDATA)>\n" + 
					"]>\n" + 
					"<student>\n" + 
					"  <surname>Smith</surname>\n" + 
					"  <id>567896</id>\n" + 
					"</student>";
		//@formatter:on
		XMLAssert.testDiagnosticsFor(xml, d(7, 3, 5, DTDErrorCode.MSG_ELEMENT_NOT_DECLARED));

		//@formatter:off
		xml = "<?xml version=\"1.0\"?>\n" + 
			"<!DOCTYPE student [\n" + 
			"  <!ELEMENT student (surname,id)>\n" + 
			"  <!ELEMENT surname (#PCDATA)>\n" + 
			"  <!ELEMENT id (#PCDATA)>\n" + 
			"]>\n" + 
			"<student>\n" + 
			"  <surname>Smith</surname>\n" + 
			"  <id>567896</id>\n" + 
			"</student>";
		//@formatter:on
		XMLAssert.testDiagnosticsFor(xml, new Diagnostic[0]);

	}

	@Test
	public void testDTDNotFoundWithSYSTEM() throws Exception {
		String xml = "<?xml version=\"1.0\" standalone=\"no\" ?>\r\n" + //
				"<!DOCTYPE inEQUAL_PMT SYSTEM \"inEQUAL_PMT.dtd\">\r\n" + // <- error DTD not found
				"<inEQUAL_PMT>\r\n" + //
				"  \r\n" + //
				"    <!-- The Proceeds and Term -->\r\n" + //
				"   <Proceeds>10000.00</Proceed>\r\n" + // <- error, it misses 's' for </Proceed>
				"   <Term>36</Term>\r\n" + //
				"   \r\n" + //
				"</inEQUAL_PMT>";
		XMLAssert.testDiagnosticsFor(xml, d(1, 29, 1, 46, DTDErrorCode.dtd_not_found),
				d(5, 23, 5, 30, XMLSyntaxErrorCode.ETagRequired));
	}

	@Test
	public void testDTDNotFoundWithPUBLIC() throws Exception {
		String xml = "<?xml version=\"1.0\" standalone=\"no\" ?>\r\n" + //
				"<!DOCTYPE inEQUAL_PMT PUBLIC 'X' \"inEQUAL_PMT.dtd\">\r\n" + // <- error DTD not found
				"<inEQUAL_PMT>\r\n" + //
				"  \r\n" + //
				"    <!-- The Proceeds and Term -->\r\n" + //
				"   <Proceeds>10000.00</Proceed>\r\n" + // <- error, it misses 's' for </Proceed>
				"   <Term>36</Term>\r\n" + //
				"   \r\n" + //
				"</inEQUAL_PMT>";
		XMLAssert.testDiagnosticsFor(xml, d(1, 33, 1, 50, DTDErrorCode.dtd_not_found),
				d(5, 23, 5, 30, XMLSyntaxErrorCode.ETagRequired));
	}
	
	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", expected);
	}

}
