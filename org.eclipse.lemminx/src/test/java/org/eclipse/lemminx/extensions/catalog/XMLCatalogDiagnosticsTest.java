package org.eclipse.lemminx.extensions.catalog;

import static org.eclipse.lemminx.XMLAssert.d;

import org.eclipse.lemminx.AbstractCacheBasedTest;
import org.eclipse.lemminx.XMLAssert;
import org.junit.jupiter.api.Test;

public class XMLCatalogDiagnosticsTest extends AbstractCacheBasedTest {
	@Test
	public void testCatalogWithValidFile() {
		String xml = "<?xml version=\"1.0\"?>\n" + //
				"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"<system systemId=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\n" + //
				"uri=\"src/test/resources/xsd/spring-beans-3.0.xsd\" />" + //
				"</catalog>";
		XMLAssert.testDiagnosticsFor(xml);
	}

	@Test
	public void testCatalogWithInvalidFile() {
		String xml = "<?xml version=\"1.0\"?>\n" + //
				"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"<system systemId=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\n" + //
				"uri=\"src/test/resources/xsd/spring-beans-3.0ABCDE.xsd\" />" + //
				"</catalog>";
		XMLAssert.testDiagnosticsFor(xml, d(3, 5, 3, 53, XMLCatalogErrorCode.catalog_uri));
	}

	@Test
	public void testCatalogWithHttpLink() {
		String xml = "<?xml version=\"1.0\"?>\n" + //
				"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">\n" + //
				"<system systemId=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\n" + //
				"uri=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\" />" + //
				"</catalog>";
		XMLAssert.testDiagnosticsFor(xml);
	}

	@Test
	public void testCatalogEntryWithXMLBaseGroupValidFile() {
		String xml = "<?xml version=\"1.0\"?>\n" + //
				"  <catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" xml:base= \"src\">\n" + //
				"    <group xml:base=\"test/resources/xsd/\">\n" + //
				"      <system systemId=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\n" + //
				"      uri=\"spring-beans-3.0.xsd\" />" + //
				"    </group>" + //
				"  </catalog>";
		XMLAssert.testDiagnosticsFor(xml);
	}

	@Test
	public void testCatalogEntryWithXMLBaseGroupInvalidFile() {
		String xml = "<?xml version=\"1.0\"?>\n" + //
				"  <catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" xml:base= \"src\">\n" + //
				"    <group xml:base=\"test/resources/xsd/\">\n" + //
				"      <system systemId=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\n" + //
				"      uri=\"spring-beans-3.0ABCDE.xsd\" />" + //
				"    </group>" + //
				"  </catalog>";
		XMLAssert.testDiagnosticsFor(xml, d(4, 11, 4, 36, XMLCatalogErrorCode.catalog_uri));
	}

	@Test
	public void testCatalogEntryWithXMLBaseGroupInvalidBase() {
		String xml = "<?xml version=\"1.0\"?>\n" + //
				"  <catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" xml:base= \"srcABCDE\">\n" + //
				"    <group xml:base=\"test/resources/xsd/\">\n" + //
				"      <system systemId=\"http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\"\n" + //
				"      uri=\"spring-beans-3.0.xsd\" />" + //
				"    </group>" + //
				"  </catalog>";
		XMLAssert.testDiagnosticsFor(xml, d(4, 11, 4, 31, XMLCatalogErrorCode.catalog_uri));
	}
}