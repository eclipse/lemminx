/**
 *  Copyright (c) 2018 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * This class holds values that represent the XSI xsd. Can be seen at
 * https://www.w3.org/2001/XMLSchema-instance
 */
public class XSISchemaModel {

	public static void computeCompletionResponses(ICompletionRequest request, ICompletionResponse response,
			boolean generateValue, Range editRange, DOMDocument document) {
		boolean isSnippetsSupported = request.getCompletionSettings().isCompletionSnippetsSupported();
		String actualPrefix = document.getSchemaInstancePrefix();
		String name;
		String documentation;
		DOMElement root = document.getDocumentElement();
		boolean schemaLocationExists = document.hasSchemaLocation();
		boolean noNamespaceSchemaLocationExists = document.hasNoNamespaceSchemaLocation();
		// Indicates that no values are allowed inside an XML element
		if (!attributeAlreadyExists(root, actualPrefix, "nil")) {
			documentation = "Indicates if an element should contain content. Valid values are true/false";
			name = actualPrefix + ":nil";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, StringUtils.TRUE,
					StringUtils.TRUE_FALSE_ARRAY, documentation, response);
		}
		// Signals that an element should be accepted as ·valid· when it has no content
		// despite
		// a content type which does not require or even necessarily allow empty
		// content.
		// An element may be ·valid· without content if it has the attribute xsi:nil
		// with
		// the value true.
		if (!attributeAlreadyExists(root, actualPrefix, "type")) {
			documentation = "Specifies the type of an element. This attribute labels an element as a particular type, even though there might not be an element declaration in the schema binding that element to the type.";
			name = actualPrefix + ":type";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation,
					response);
		}
		// The xsi:schemaLocation and xsi:noNamespaceSchemaLocation attributes can be
		// used in a document
		// to provide hints as to the physical location of schema documents which may be
		// used for ·assessment·.
		if (!schemaLocationExists && !noNamespaceSchemaLocationExists) {
			documentation = "The xsi:schemaLocation attribute can be used in an XML document "
					+ "to reference an XML Schema document that has a target namespace.\r\n  " + "```xml  \r\n"
					+ "<ns:root xmlns:ns=\"http://example.com/ns\""
					+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
					+ " xsi:schemaLocation=\"http://example.com/ns example-ns.xsd\">\r\n " + "  <!-- ... -->\r\n  "
					+ "</ns:root>\r\n  " + "```";
			name = actualPrefix + ":schemaLocation";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation,
					response);

			documentation = "The xsi:noNamespaceSchemaLocation attribute can be used in an XML document "
					+ "to reference an XML Schema document that does not have a target namespace.\r\n  "
					+ "```xml  \r\n" + "\r\n<root xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  "
					+ "  xsi:noNamespaceSchemaLocation=\"example.xsd\">  \r\n" + "  <!-- ... -->  \r\n"
					+ "</root>  \r\n" + "```";
			name = actualPrefix + ":noNamespaceSchemaLocation";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation,
					response);
		}
	}

	private static void createCompletionItem(String attrName, boolean canSupportSnippet, boolean generateValue,
			Range editRange, String defaultValue, Collection<String> enumerationValues, String documentation,
			ICompletionResponse response) {
		CompletionItem item = new AttributeCompletionItem(attrName, canSupportSnippet, editRange, generateValue,
				defaultValue, enumerationValues);
		MarkupContent markup = new MarkupContent();
		markup.setKind(MarkupKind.MARKDOWN);
		markup.setValue(documentation);
		item.setDocumentation(markup);
		response.addCompletionItem(item);
	}

	private static boolean attributeAlreadyExists(DOMElement root, String actualPrefix, String suffix) {
		return root.getAttributeNode(actualPrefix + ":" + suffix) != null;
	}
}