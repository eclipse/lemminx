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
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;
import org.eclipse.lsp4xml.utils.StringUtils;

/**
 * This class holds values that represent the XSI xsd. Can be seen at
 * https://www.w3.org/2001/XMLSchema-instance
 */
public class XSISchemaModel {
	private static String lineSeparator = System.lineSeparator();
	public static final String TYPE_DOC = "Specifies the type of an element. This attribute labels an element as a particular type, even though there might not be an element declaration in the schema binding that element to the type."; 
	public static final String NIL_DOC = "Indicates if an element should contain content. Valid values are `true` or `false`";
	public static final String SCHEMA_LOCATION_DOC = 
				"The xsi:schemaLocation attribute can be used in an XML document " +
				"to reference an XML Schema document that has a target namespace. " +  lineSeparator +
				"```xml  " + lineSeparator +
				"<ns:root  "+ lineSeparator +
				"  xmlns:ns=\"http://example.com/ns\"  " + lineSeparator +
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  " + lineSeparator +
				"  xsi:schemaLocation=\"http://example.com/ns example.xsd\">" + lineSeparator +
				"  <!-- ... -->  " + lineSeparator +
				"</ns:root>  " + lineSeparator +
				"```" ;
	public static final String NO_NAMESPACE_SCHEMA_LOCATION_DOC= 
				"The xsi:noNamespaceSchemaLocation attribute can be used in an XML document " + lineSeparator +
				"to reference an XML Schema document that does not have a target namespace.  " + lineSeparator +
				"```xml  " + lineSeparator +
				"<root  " + lineSeparator + 
				"  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"  "+ lineSeparator +
				"  xsi:noNamespaceSchemaLocation=\"example.xsd\">" + lineSeparator +
				"  <!-- ... -->  " + lineSeparator +
				"</root>  " + lineSeparator +
				"```" ;
	public static void computeCompletionResponses(ICompletionRequest request, 
			ICompletionResponse response, Range editRange, DOMDocument document, boolean generateValue) throws BadLocationException {
		
		DOMElement rootElement = document.getDocumentElement();
		int offset = document.offsetAt(editRange.getStart());
		boolean inRootElement = false;
		DOMElement nodeAtOffset = (DOMElement) document.findNodeAt(offset);
		if(rootElement.equals(nodeAtOffset)) {
			inRootElement = true;
		}
		
		boolean isSnippetsSupported = request.getCompletionSettings().isCompletionSnippetsSupported();
		String actualPrefix = document.getSchemaInstancePrefix();
		String name;
		String documentation;
		
		boolean schemaLocationExists = document.hasSchemaLocation();
		boolean noNamespaceSchemaLocationExists = document.hasNoNamespaceSchemaLocation();
		//Indicates that no values are allowed inside an XML element
		if(!attributeAlreadyExists(nodeAtOffset, actualPrefix, "nil")) {
			documentation = NIL_DOC;
			name = actualPrefix + ":nil";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, StringUtils.TRUE, 
				StringUtils.TRUE_FALSE_ARRAY, documentation, response);
		}
		//Signals that an element should be accepted as ·valid· when it has no content despite 
		//a content type which does not require or even necessarily allow empty content. 
		//An element may be ·valid· without content if it has the attribute xsi:nil with 
		//the value true.
		if(!attributeAlreadyExists(nodeAtOffset, actualPrefix, "type")) {
			documentation = TYPE_DOC;
			name = actualPrefix + ":type";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response);	
		}
		//The xsi:schemaLocation and xsi:noNamespaceSchemaLocation attributes can be used in a document 
		//to provide hints as to the physical location of schema documents which may be used for ·assessment·.
		if(inRootElement && !schemaLocationExists && !noNamespaceSchemaLocationExists) {
			documentation = NO_NAMESPACE_SCHEMA_LOCATION_DOC;
			name = actualPrefix + ":schemaLocation";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response);	
			
			documentation = SCHEMA_LOCATION_DOC;
			name = actualPrefix + ":noNamespaceSchemaLocation";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response);		
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

	public static Hover computeHoverResponse(DOMAttr attribute, IHoverRequest request) {

		String name = attribute.getName();
		if(!name.startsWith(request.getXMLDocument().getSchemaInstancePrefix() + ":")) {
			return null;
		}

		DOMDocument document = request.getXMLDocument();
		DOMElement root = document.getDocumentElement();
		String doc = null;
		if(root != null) {
			if(root.equals(document.findNodeAt(attribute.getStart()))) {
				if(name.endsWith(":schemaLocation")) {
					doc = SCHEMA_LOCATION_DOC;
				}
				else if(name.endsWith(":noNamespaceSchemaLocation")) {
					doc = NO_NAMESPACE_SCHEMA_LOCATION_DOC;
				}
			}
		} else {
			return null;
		}
		if(doc == null) {
			if(name.endsWith(":nil")) {
				doc = NIL_DOC;
			}
			else if(name.endsWith(":type")) {
				doc = TYPE_DOC;
			}
			else {
				return null;
			}
		}
		
		MarkupContent content = new MarkupContent();
		content.setKind(MarkupKind.MARKDOWN);
		content.setValue(doc);
		return new Hover(content);
	}
}