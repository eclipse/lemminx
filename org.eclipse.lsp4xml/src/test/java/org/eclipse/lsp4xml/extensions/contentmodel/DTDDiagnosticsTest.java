/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel;

import static org.eclipse.lsp4xml.XMLAssert.d;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4xml.XMLAssert;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.DTDErrorCode;
import org.junit.Test;

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
		XMLAssert.testDiagnosticsFor(xml, d(10, 10, 14, DTDErrorCode.MSG_ATTRIBUTE_NOT_DECLARED));
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

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", expected);
	}

}
