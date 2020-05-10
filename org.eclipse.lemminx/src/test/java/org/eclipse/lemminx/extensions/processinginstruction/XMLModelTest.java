package org.eclipse.lemminx.extensions.processinginstruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.eclipse.lemminx.XMLAssert.d;
import static org.eclipse.lemminx.XMLAssert.te;
import static org.eclipse.lemminx.XMLAssert.ca;
import static org.eclipse.lemminx.XMLAssert.testCodeActionsFor;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.TextDocument;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.extensions.contentmodel.participants.XMLSchemaErrorCode;
import org.eclipse.lemminx.extensions.contentmodel.settings.ContentModelSettings;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;

/**
 * 
 */
public class XMLModelTest {
	@Test
	public void xmlModelDeclaration(){
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<?xml-model href=\"http://www.docbook.org/xml/5.0/xsd/docbook.xsd\"?>\r\n" + //
				"<book>\r\n" + //
				"  ...\r\n" + //
				"</book>\r\n";

		TextDocument textDocument = new TextDocument(xml, "test.xml");
		DOMDocument d = DOMParser.getInstance().parse(xml, textDocument.getUri(), null);
		assertNotNull(d.getXMLModel());

		assertEquals("http://www.docbook.org/xml/5.0/xsd/docbook.xsd",d.getXMLModel().getSchemaLocation());
	}

	@Test
	public void cvc_complex_type_2_3() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<?xml-model href=\"http://www.docbook.org/xml/5.0/xsd/docbook.xsd\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>\r\n" + //
				"		XXXXXXXXXXXXX\r\n" + // <-- error
				"	</bean>\r\n" + //
				"</beans>";

		TextDocument textDocument = new TextDocument(xml, "test.xml");
		DOMDocument doc = DOMParser.getInstance().parse(xml, textDocument.getUri(), null);
		assertNotNull(doc.getXMLModel());

		Diagnostic d = d(4, 2, 4, 15, XMLSchemaErrorCode.cvc_complex_type_2_3, "Element \'bean\' cannot contain text content.\nThe content type is defined as element-only.\n\nCode:");
		testDiagnosticsFor(xml, d);
		// testCodeActionsFor(xml, d, ca(d, te(3, 2, 3, 15, "")));
	}

	
	@Test
	public void cvc_complex_type_2_3_reference() throws Exception {
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<beans xmlns=\"http://www.springframework.org/schema/beans\" xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n"
				+ //
				"	<bean>\r\n" + //
				"		XXXXXXXXXXXXX\r\n" + // <-- error
				"	</bean>\r\n" + //
				"</beans>";
		Diagnostic d = d(3, 2, 3, 15, XMLSchemaErrorCode.cvc_complex_type_2_3, "Element \'bean\' cannot contain text content.\nThe content type is defined as element-only.\n\nCode:");
		testDiagnosticsFor(xml, d);
		testCodeActionsFor(xml, d, ca(d, te(3, 2, 3, 15, "")));
	}

	// @Test
	// public void cvc_complex_type_2_4_a() throws Exception {
	// 	String xml = 
	// 			"<project xmlns=\"http://maven.apache.org/POM/4.0.0\"\r\n" + //
	// 			"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
	// 			"	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\r\n"+ //
	// 			"	<XXX></XXX>\r\n" + // <- error
	// 			"</project>";

	// 	String message = "Invalid element name:\n - XXX\n\nOne of the following is expected:\n - modelVersion\n - parent\n - groupId\n - artifactId\n - version\n - packaging\n - name\n - description\n - url\n - inceptionYear\n - organization\n - licenses\n - developers\n - contributors\n - mailingLists\n - prerequisites\n - modules\n - scm\n - issueManagement\n - ciManagement\n - distributionManagement\n - properties\n - dependencyManagement\n - dependencies\n - repositories\n - pluginRepositories\n - build\n - reports\n - reporting\n - profiles\n\nError indicated by:\n {http://maven.apache.org/POM/4.0.0}\nwith code:";
	// 	testDiagnosticsFor(xml, d(3, 2, 3, 5, XMLSchemaErrorCode.cvc_complex_type_2_4_a, message));
	// }

	private static void testDiagnosticsFor(String xml, Diagnostic... expected) {
		XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", expected);
	}

	// private static void testDiagnosticsDisabledValidation(String xml) {
	// 	ContentModelSettings settings = XMLAssert.getContentModelSettings(true, false);
	// 	XMLAssert.testDiagnosticsFor(xml, "src/test/resources/catalogs/catalog.xml", null, null, true, settings);
	// }
}