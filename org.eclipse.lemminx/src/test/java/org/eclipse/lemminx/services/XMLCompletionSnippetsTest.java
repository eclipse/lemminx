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
import static org.eclipse.lemminx.XMLAssert.REGION_SNIPPETS;
import static org.eclipse.lemminx.XMLAssert.XML_DECLARATION_SNIPPETS;
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
		testCompletionFor("|", REGION_SNIPPETS /* #region */ + //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 0), "<!DOCTYPE"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 0), "<?xml"),
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
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 0), "<!--"));

		testCompletionFor("<|", NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 0, 0, 1), "<!DOCTYPE"),
				c("Insert XML Declaration", //
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>", //
						r(0, 0, 0, 1), "<?xml"),
				c("<!--", //
						"<!-- -->", //
						r(0, 0, 0, 1), "<!--"));

		testCompletionFor("<|>", NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ , //
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

		testCompletionFor("<!|", NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ , //
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
		testCompletionFor("<!-- -->|", NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
				XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
				COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 8, 0, 8), "<!DOCTYPE"),
				c("<!--", //
						"<!-- -->", //
						r(0, 8, 0, 8), "<!--"));
	}

	@Test
	public void afterProlog() throws BadLocationException {
		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?>|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 38), "<!DOCTYPE"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 38), "<!--"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?><|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 39), "<!DOCTYPE"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 39), "<!--"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?><|!", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 39), "<!DOCTYPE"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 39), "<!--"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(0, 38, 0, 40), "<!DOCTYPE"),
				c("<!--", //
						"<!-- -->", //
						r(0, 38, 0, 40), "<!--"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n|", //
				NEW_XML_SNIPPETS /* DOCTYPE snippets */ + //
						REGION_SNIPPETS /* regions snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("New XML with SYSTEM DOCTYPE", //
						"<!DOCTYPE root-element SYSTEM \"file.dtd\">" + lineSeparator() + //
								"<root-element>" + lineSeparator() + //
								"</root-element>", //
						r(1, 0, 1, 0), "<!DOCTYPE"),
				c("<!--", //
						"<!-- -->", //
						r(1, 0, 1, 0), "<!--"));
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
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("Insert SYSTEM DOCTYPE", //
						"<!DOCTYPE foo SYSTEM \"file.dtd\">", //
						r(0, 0, 0, 0), "<!DOCTYPE"));

		testCompletionFor("<!-- -->|<foo>", //
				DOCTYPE_SNIPPETS /* DOCTYPE snippets */ + //
						XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
						COMMENT_SNIPPETS /* Comment snippets */ , //
				c("Insert SYSTEM DOCTYPE", //
						"<!DOCTYPE foo SYSTEM \"file.dtd\">", //
						r(0, 8, 0, 8), "<!DOCTYPE"));

		testCompletionFor("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
				"<!-- -->\r\n" + //
				"|<foo>", //
				DOCTYPE_SNIPPETS /* DOCTYPE snippets */ + //
						XML_DECLARATION_SNIPPETS /* XML Declaration snippets */ + //
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
}
