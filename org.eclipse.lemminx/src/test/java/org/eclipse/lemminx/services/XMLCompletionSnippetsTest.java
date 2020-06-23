/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.services;

import static java.lang.System.lineSeparator;
import static org.eclipse.lemminx.XMLAssert.COMMENT_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.DOCTYPE_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.NEW_XML_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.NEW_XSD_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.PROCESSING_INSTRUCTION_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.REGION_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.XML_DECLARATION_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.CATALOG_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.c;
import static org.eclipse.lemminx.XMLAssert.r;
import static org.eclipse.lemminx.XMLAssert.testCompletionFor;

import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

/**
 * XML completion tests with snippets
 *
 */
public class XMLCompletionSnippetsTest {

	// Tests with new XML snippets

	@Test
	public void emptyXMLContent() throws BadLocationException {
		testCompletionFor("|", //
				REGION_SNIPPETS /* #region */ + //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS /* Catalog snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 0), "<!DOCTYPE"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 0), "<?xml"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 0, 0, 0), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 0, 0, 0), "<?xml-model"),
				c("New XML bound with xsi:schemaLocation", //
						"<root-element xmlns=\"https://github.com/eclipse/lemminx\"" + lineSeparator() + //
								"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
								"	xsi:schemaLocation=\"" + lineSeparator() + //
								"		https://github.com/eclipse/lemminx file.xsd\">" + lineSeparator() + //
								"	" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 0), "schemaLocation"),
				c("New XML bound with xsi:noNamespaceSchemaLocation", //
						"<root-element" + lineSeparator() + //
								"	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
								"	xsi:noNamespaceSchemaLocation=\"file.xsd\">" + lineSeparator() + //
								"	" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 0), "noNamespaceSchemaLocation"),
				c("New catalog bound using DTD", //
						"<!DOCTYPE catalog" + lineSeparator() + //
						"\tPUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"" + lineSeparator() + //
						"\t\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">" + lineSeparator() + //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 0, 0, 0), "<catalog"),
				c("New catalog bound using XSD", //
						"<catalog" + lineSeparator() + //
						"\txmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"" + lineSeparator() + //
						"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
						"\txsi:schemaLocation=\"urn:oasis:names:tc:entity:xmlns:xml:catalog" + lineSeparator() + //
						"\t\thttp://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd\"" + lineSeparator() + //
						"\tprefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 0, 0, 0), "<catalog"),
				c("New catalog", //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>",
						r(0, 0, 0, 0), "<catalog"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 0), "<!--"));

		testCompletionFor("<|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS /* Catalog snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 1), "<!DOCTYPE"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 1), "<?xml"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 0, 0, 1), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 0, 0, 1), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 1), "<!--"),
				c("New catalog bound using DTD", //
						"<!DOCTYPE catalog" + lineSeparator() + //
						"\tPUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"" + lineSeparator() + //
						"\t\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">" + lineSeparator() + //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 0, 0, 1), "<catalog"),
				c("New catalog bound using XSD", //
						"<catalog" + lineSeparator() + //
						"\txmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"" + lineSeparator() + //
						"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
						"\txsi:schemaLocation=\"urn:oasis:names:tc:entity:xmlns:xml:catalog" + lineSeparator() + //
						"\t\thttp://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd\"" + lineSeparator() + //
						"\tprefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 0, 0, 1), "<catalog"), //
				c("New catalog", //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>",
						r(0, 0, 0, 1), "<catalog"));

		testCompletionFor("<|>", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS /* Catalog snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 2), "<!DOCTYPE"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 2), "<?xml"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 0, 0, 2), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 0, 0, 2), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 2), "<!--"),
				c("New catalog bound using DTD", //
						"<!DOCTYPE catalog" + lineSeparator() + //
						"\tPUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"" + lineSeparator() + //
						"\t\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">" + lineSeparator() + //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 0, 0, 2), "<catalog"),
				c("New catalog bound using XSD", //
						"<catalog" + lineSeparator() + //
						"\txmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"" + lineSeparator() + //
						"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
						"\txsi:schemaLocation=\"urn:oasis:names:tc:entity:xmlns:xml:catalog" + lineSeparator() + //
						"\t\thttp://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd\"" + lineSeparator() + //
						"\tprefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 0, 0, 2), "<catalog"), //
				c("New catalog", //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>",
						r(0, 0, 0, 2), "<catalog"));

		testCompletionFor("<!|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS /* Catalog snippets (are counted here but get filtered by the prefix in the editor ) */, // TODO
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 2), "<!DOCTYPE"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 2), "<?xml"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 2), "<!--"));

	}

	@Test
	public void afterComment() throws BadLocationException {
		testCompletionFor("<!-- -->|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS, //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 8, 0, 8), "<!DOCTYPE"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 8, 0, 8), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 8, 0, 8), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(0, 8, 0, 8), "<!--"),
				c("New catalog bound using DTD", //
						"<!DOCTYPE catalog" + lineSeparator() + //
						"\tPUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"" + lineSeparator() + //
						"\t\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">" + lineSeparator() + //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 8, 0, 8), "<catalog"),
				c("New catalog bound using XSD", //
						"<catalog" + lineSeparator() + //
						"\txmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"" + lineSeparator() + //
						"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
						"\txsi:schemaLocation=\"urn:oasis:names:tc:entity:xmlns:xml:catalog" + lineSeparator() + //
						"\t\thttp://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd\"" + lineSeparator() + //
						"\tprefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 8, 0, 8), "<catalog"), //
				c("New catalog", //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>",
						r(0, 8, 0, 8), "<catalog"));
	}

	@Test
	public void afterProlog() throws BadLocationException {
		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?>|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS, //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 38), "<!DOCTYPE"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 38, 0, 38), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 38, 0, 38), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 38), "<!--"),
				c("New catalog bound using DTD", //
						"<!DOCTYPE catalog" + lineSeparator() + //
						"\tPUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"" + lineSeparator() + //
						"\t\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">" + lineSeparator() + //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 38, 0, 38), "<catalog"),
				c("New catalog bound using XSD", //
						"<catalog" + lineSeparator() + //
						"\txmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"" + lineSeparator() + //
						"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
						"\txsi:schemaLocation=\"urn:oasis:names:tc:entity:xmlns:xml:catalog" + lineSeparator() + //
						"\t\thttp://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd\"" + lineSeparator() + //
						"\tprefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 38, 0, 38), "<catalog"), //
				c("New catalog", //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>",
						r(0, 38, 0, 38), "<catalog"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?><|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS, //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 39), "<!DOCTYPE"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 38, 0, 39), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 38, 0, 39), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 39), "<!--"),
				c("New catalog bound using DTD", //
						"<!DOCTYPE catalog" + lineSeparator() + //
						"\tPUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"" + lineSeparator() + //
						"\t\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">" + lineSeparator() + //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 38, 0, 39), "<catalog"),
				c("New catalog bound using XSD", //
						"<catalog" + lineSeparator() + //
						"\txmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"" + lineSeparator() + //
						"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
						"\txsi:schemaLocation=\"urn:oasis:names:tc:entity:xmlns:xml:catalog" + lineSeparator() + //
						"\t\thttp://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd\"" + lineSeparator() + //
						"\tprefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(0, 38, 0, 39), "<catalog"), //
				c("New catalog", //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>",
						r(0, 38, 0, 39), "<catalog"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?><|!", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 39), "<!DOCTYPE"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 38, 0, 39), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 38, 0, 39), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 39), "<!--"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 40), "<!DOCTYPE"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 38, 0, 40), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 38, 0, 40), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 40), "<!--"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
				REGION_SNIPPETS /* regions snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ + //
				CATALOG_SNIPPETS, //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(1, 0, 1, 0), "<!DOCTYPE"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(1, 0, 1, 0), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(1, 0, 1, 0), "<?xml-model"),
				c("<!--", //
						"<!-- -->", //
						r(1, 0, 1, 0), "<!--"), //
				c("New catalog bound using DTD", //
						"<!DOCTYPE catalog" + lineSeparator() + //
						"\tPUBLIC \"-//OASIS//DTD Entity Resolution XML Catalog V1.0//EN\"" + lineSeparator() + //
						"\t\"http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd\">" + lineSeparator() + //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\" prefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(1, 0, 1, 0), "<catalog"),
				c("New catalog bound using XSD", //
						"<catalog" + lineSeparator() + //
						"\txmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\"" + lineSeparator() + //
						"\txmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + lineSeparator() + //
						"\txsi:schemaLocation=\"urn:oasis:names:tc:entity:xmlns:xml:catalog" + lineSeparator() + //
						"\t\thttp://www.oasis-open.org/committees/entity/release/1.1/catalog.xsd\"" + lineSeparator() + //
						"\tprefer=\"public\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>", //
						r(1, 0, 1, 0), "<catalog"), //
				c("New catalog", //
						"<catalog xmlns=\"urn:oasis:names:tc:entity:xmlns:xml:catalog\">" + lineSeparator() + //
						"\t<public publicId=\"\" uri=\"\" />" + lineSeparator() + //
						"</catalog>",
						r(1, 0, 1, 0), "<catalog"));
	}

	// Tests with new XSD snippets

	@Test
	public void emptyXSDContent() throws BadLocationException {
		testCompletionFor("|", null, //
				"test.xsd", //
				REGION_SNIPPETS /* #region */ + //
					NEW_XSD_SNIPPETS /* schema snippets */ + //
					XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
					COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML Schema", //
						"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
								"	<xs:element name=\"root-element\">" + lineSeparator() + //
								"		" + lineSeparator() + //
								"	</xs:element>" + lineSeparator() + //
								"</xs:schema>", //
						r(0, 0, 0, 0), "<schema"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 0), "<?xml"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 0), "<!--"));

		testCompletionFor("<|", null, //
				"test.xsd", //
				NEW_XSD_SNIPPETS /* schema snippets */ + //
						XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML Schema", //
						"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
								"	<xs:element name=\"root-element\">" + lineSeparator() + //
								"		" + lineSeparator() + //
								"	</xs:element>" + lineSeparator() + //
								"</xs:schema>", //
						r(0, 0, 0, 1), "<schema"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 1), "<?xml"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 1), "<!--"));

		testCompletionFor("<|>", null, //
				"test.xsd", //
				NEW_XSD_SNIPPETS /* schema snippets */ + //
						XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML Schema", //
						"<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">" + lineSeparator() + //
								"	<xs:element name=\"root-element\">" + lineSeparator() + //
								"		" + lineSeparator() + //
								"	</xs:element>" + lineSeparator() + //
								"</xs:schema>", //
						r(0, 0, 0, 2), "<schema"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 2), "<?xml"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 2), "<!--"));

	}

	// Tests with comments

	@Test
	public void commentsOnEmptyContent() throws BadLocationException {
		testCompletionFor("|", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 0), "<!--"));
		testCompletionFor("<|", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 1), "<!--"));
		testCompletionFor("<!|", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 2), "<!--"));
		testCompletionFor("<!-|", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 3), "<!--"));
		testCompletionFor("<!--|", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 4), "<!--"));
		testCompletionFor("<!--| ", c("<!--", //
				"<!-- -->",  //
				r(0, 0, 0, 4), "<!--"));
	}

	@Test
	public void commentsBeforeTag() throws BadLocationException {
		testCompletionFor("<a|", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 2), "<!--"));
		testCompletionFor("<a>|", c("<!--", //
				"<!-- -->", //
				r(0, 3, 0, 3), "<!--"));
		testCompletionFor("<a><|", c("<!--", //
				"<!-- -->", //
				r(0, 3, 0, 4), "<!--"));
		testCompletionFor("<a><!|", c("<!--", //
				"<!-- -->", //
				r(0, 3, 0, 5), "<!--"));

		testCompletionFor("<a> |", c("<!--", //
				"<!-- -->", //
				r(0, 4, 0, 4), "<!--"));
		testCompletionFor("<a> <|", c("<!--", //
				"<!-- -->", //
				r(0, 4, 0, 5), "<!--"));
		testCompletionFor("<a> <!|", c("<!--", //
				"<!-- -->", //
				r(0, 4, 0, 6), "<!--"));
	}

	@Test
	public void commentsInsideTag() throws BadLocationException {
		testCompletionFor("<a>|</a>", c("<!--", //
				"<!-- -->", //
				r(0, 3, 0, 3), "<!--"));
		testCompletionFor("<a> |</a>", c("<!--", //
				"<!-- -->", //
				r(0, 4, 0, 4), "<!--"));
		testCompletionFor("<a> | </a>", c("<!--", //
				"<!-- -->", //
				r(0, 4, 0, 4), "<!--"));

		testCompletionFor("<a><|/a>", 0);
		testCompletionFor("<a></|a>", 0);
		testCompletionFor("<a></a|>", 0);
	}

	@Test
	public void commentsInStartTag() throws BadLocationException {
		testCompletionFor("<a|></a>", 0);
	}

	@Test
	public void commentsAfterTag() throws BadLocationException {
		testCompletionFor("<a>|", c("<!--", //
				"<!-- -->", //
				r(0, 3, 0, 3), "<!--"));
		testCompletionFor("<a></a>|", c("<!--", //
				"<!-- -->", //
				r(0, 7, 0, 7), "<!--"));
	}

	@Test
	public void commentsFollowedBy() throws BadLocationException {
		testCompletionFor("<!|abcd", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 2), "<!--"));
		testCompletionFor("<|-abcd", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 2), "<!--"));
		testCompletionFor("<|--abcd", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 3), "<!--"));
		testCompletionFor("<|-->abcd", c("<!--", //
				"<!-- -->", //
				r(0, 0, 0, 4), "<!--"));
	}

	// Tests with CDATA

	@Test
	public void cdataBeforeTag() throws BadLocationException {
		testCompletionFor("<a|", COMMENT_SNIPPETS);
		testCompletionFor("<a>|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 3, 0, 3), "<![CDATA["));
		testCompletionFor("<a><|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 3, 0, 4), "<![CDATA["));
		testCompletionFor("<a><!|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 3, 0, 5), "<![CDATA["));

		testCompletionFor("<a> |", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 4), "<![CDATA["));
		testCompletionFor("<a> <|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 5), "<![CDATA["));
		testCompletionFor("<a> <!|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 6), "<![CDATA["));
		testCompletionFor("<a> <![CDATA|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 12), "<![CDATA["));
		testCompletionFor("<a> <![CDATA[|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 13), "<![CDATA["));
		testCompletionFor("<a> <![CDATA[| ", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 13), "<![CDATA["));		
	}

	@Test
	public void cdataInsideTag() throws BadLocationException {
		testCompletionFor("<a>|</a>", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 3, 0, 3), "<![CDATA["));
		testCompletionFor("<a> |</a>", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 4), "<![CDATA["));
		testCompletionFor("<a> | </a>", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 4, 0, 4), "<![CDATA["));
	}

	@Test
	public void cdataAfterTag() throws BadLocationException {
		testCompletionFor("<a>|", c("<![CDATA[", //
				"<![CDATA[ ]]>", //
				r(0, 3, 0, 3), "<![CDATA["));
		testCompletionFor("<a></a>|", COMMENT_SNIPPETS);
	}

	// Tests with doctype snippets

	@Test
	public void doctype() throws BadLocationException {
		testCompletionFor("|<foo>", //
				DOCTYPE_SNIPPETS /* DOCTYPE snippets */ + //
						XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
						PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("Insert SYSTEM DOCTYPE", //
						"<!DOCTYPE foo SYSTEM \"file.dtd\">", //
						r(0, 0, 0, 0), "<!DOCTYPE"));

		testCompletionFor("<!-- -->|<foo>", //
				DOCTYPE_SNIPPETS /* DOCTYPE snippets */ + //
						XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
						PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("Insert SYSTEM DOCTYPE", //
						"<!DOCTYPE foo SYSTEM \"file.dtd\">", //
						r(0, 8, 0, 8), "<!DOCTYPE"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!-- -->\r\n" + //
				"|<foo>", //
				DOCTYPE_SNIPPETS /* DOCTYPE snippets */ + //
						XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
						PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("Insert SYSTEM DOCTYPE", //
						"<!DOCTYPE foo SYSTEM \"file.dtd\">", //
						r(2, 0, 2, 0), "<!DOCTYPE"));
	}

	// Tests with prolog snippets

	@Test
	public void prolog() throws BadLocationException {
		testCompletionFor("<?|", //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
						PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction Snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 2), "<?xml"));

	}

	// Empty completion

	@Test
	public void emptyCompletionInsideComment() throws BadLocationException {
		testCompletionFor("<!-- |-->", 0);
		testCompletionFor("<!-- |--><foo>", 0);
	}

	@Test
	public void emptyCompletionInsideDOCTYPE() throws BadLocationException {
		testCompletionFor("<!DOCTYPE |root-element SYSTEM \"file.dtd\">", 0);
		testCompletionFor("<!DOCTYPE root-element |SYSTEM \"file.dtd\">", 0);
		testCompletionFor("<!DOCTYPE root-element SYSTEM |\"file.dtd\">", 0);
	}

	// Tests triggering completion at the very beginning of the file.
	// Should be possible if not in front of the xml declaration.
	@Test
	public void atBeginningOfFile() throws BadLocationException{
		// No Completion when triggered in front of xml declaration.
		// Only XML declaration
		String xml = "|<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
		testCompletionFor(xml, 0);
		// Space before declaration; triggered before space
		xml = "| <?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
		testCompletionFor(xml, REGION_SNIPPETS); // FIXME: region snippets should not trigger here.
		// Space before declaration; triggered after space
		xml = " |<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
		testCompletionFor(xml, REGION_SNIPPETS); // FIXME: region snippets should not trigger here.
		// Declaration and a root element
		xml = "|<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<root/>";
		testCompletionFor(xml, 0);
		// Space before declaration; triggered before space
		xml = "| <?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<root/>";
		testCompletionFor(xml, REGION_SNIPPETS); // FIXME: region snippets should not trigger here.
		// Space before declaration; triggered after space
		xml = " |<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<root/>";
		testCompletionFor(xml, REGION_SNIPPETS); // FIXME: region snippets should not trigger here.

		// Multiple Snippets can be triggered at start of file, if it doesn't have an xml declaration
		xml = "|\r\n" + //
				"<root/>";
		testCompletionFor(xml, //
				COMMENT_SNIPPETS /* Comment Snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration Snippets */ + //
				PROCESSING_INSTRUCTION_SNIPPETS /* Processing Instruction Snippets */ + //
				DOCTYPE_SNIPPETS /* Doctype Snippets */ + //
				REGION_SNIPPETS /* Region Snippets */, //
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 0), "<?xml"),
				c("Insert XML Schema association", //
						"<?xml-model href=\"file.xsd\" type=\"application/xml\" schematypens=\"http://www.w3.org/2001/XMLSchema\"?>", //
						r(0, 0, 0, 0), "<?xml-model"),
				c("Insert DTD association", //
						"<?xml-model href=\"file.dtd\" type=\"application/xml-dtd\"?>", //
						r(0, 0, 0, 0), "<?xml-model"),
				c("Insert SYSTEM DOCTYPE", //
						"<!DOCTYPE root SYSTEM \"file.dtd\">", //
						r(0, 0, 0, 0), "<!DOCTYPE"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 0), "<!--"));
	}
}
