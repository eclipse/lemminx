/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
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

import static org.eclipse.lemminx.XMLAssert.c;

import java.io.File;
import java.io.IOException;

import org.eclipse.lemminx.XMLAssert;
import org.eclipse.lemminx.commons.BadLocationException;
import org.junit.jupiter.api.Test;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

/**
 * Content model manager cache test
 * 
 * @author Angelo ZERR
 *
 */
public class ContentModelManagerCacheTest extends BaseFileTempTest {

	@Test
	public void testXSDCache() throws IOException, BadLocationException {
		String xsdPath = tempDirUri.getPath() + "/tag.xsd";
		String xmlPath = tempDirUri.toString() + "/tag.xml";

		// Create a XSD file in the temp directory
		String xsd = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema\r\n" + //
				"    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"    elementFormDefault=\"qualified\">\r\n" + //
				"  <xs:element name=\"root\">\r\n" + //
				"    <xs:complexType>\r\n" + //
				"      <xs:sequence>\r\n" + //
				"        <xs:element name=\"tag\"/>\r\n" + //
				"      </xs:sequence>\r\n" + //
				"    </xs:complexType>\r\n" + //
				"  </xs:element>\r\n" + //
				"</xs:schema>";
		createFile(xsdPath, xsd);

		// Open completion in a XML which is bound to the XML Schema
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\r\n" + //
				"<root\r\n" + //
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\r\n" + //
				"    xsi:noNamespaceSchemaLocation=\"tag.xsd\">\r\n" + //
				"  | " + // <-- completion must provide tag
				"</root>";
		XMLAssert.testCompletionFor(xml, null, xmlPath, 5 /* region, endregion, cdata, comment, tag */,
				c("tag", "<tag></tag>"));
		// Open again the completion to use the cache
		XMLAssert.testCompletionFor(xml, null, xmlPath, null, c("tag", "<tag></tag>"));

		// Change the content of the XSD on the file system
		xsd = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\r\n" + //
				"<xs:schema\r\n" + //
				"    xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\r\n" + //
				"    elementFormDefault=\"qualified\">\r\n" + //
				"  <xs:element name=\"root\">\r\n" + //
				"    <xs:complexType>\r\n" + //
				"      <xs:sequence>\r\n" + //
				"        <xs:element name=\"label\"/>\r\n" + // <-- change tag by label
				"      </xs:sequence>\r\n" + //
				"    </xs:complexType>\r\n" + //
				"  </xs:element>\r\n" + //
				"</xs:schema>";
		updateFile(xsdPath, xsd);

		XMLAssert.testCompletionFor(xml, null, xmlPath, 5 /* region, endregion, cdata, comment, label */,
				c("label", "<label></label>"));
		// Open again the completion to use the cache
		XMLAssert.testCompletionFor(xml, null, xmlPath, 5, c("label", "<label></label>"));

		// delete the XSD file
		MoreFiles.deleteRecursively(new File(xsdPath).toPath(), RecursiveDeleteOption.ALLOW_INSECURE);
		// Completion must be empty
		XMLAssert.testCompletionFor(xml, null, xmlPath, 4 /* region, endregion, cdata, comment */);
		
		// recreate the XSD file
		createFile(xsdPath, xsd);
		XMLAssert.testCompletionFor(xml, null, xmlPath, 5 /* region, endregion, cdata, comment, label */,
				c("label", "<label></label>"));
		XMLAssert.testCompletionFor(xml, null, xmlPath, 5 /* region, endregion, cdata, comment, label */,
				c("label", "<label></label>"));
		
	}
}
