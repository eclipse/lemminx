/**
 *  Copyright (c) 2018 Red Hat, Inc. and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Red Hat Inc. - initial API and implementation
 */
package org.eclipse.lsp4xml.services;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.services.extensions.IHoverRequest;
import org.eclipse.lsp4xml.settings.SharedSettings;
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
		if(!hasAttribute(nodeAtOffset, actualPrefix, "nil")) {
			documentation = NIL_DOC;
			name = actualPrefix + ":nil";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, StringUtils.TRUE, 
				StringUtils.TRUE_FALSE_ARRAY, documentation, response);
		}
		//Signals that an element should be accepted as ·valid· when it has no content despite 
		//a content type which does not require or even necessarily allow empty content. 
		//An element may be ·valid· without content if it has the attribute xsi:nil with 
		//the value true.
		if(!hasAttribute(nodeAtOffset, actualPrefix, "type")) {
			documentation = TYPE_DOC;
			name = actualPrefix + ":type";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response);	
		}
		//The xsi:schemaLocation and xsi:noNamespaceSchemaLocation attributes can be used in a document 
		//to provide hints as to the physical location of schema documents which may be used for ·assessment·.
		if(inRootElement && !schemaLocationExists && !noNamespaceSchemaLocationExists) {
			documentation = SCHEMA_LOCATION_DOC;
			name = actualPrefix + ":schemaLocation";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response);	
			
			documentation = NO_NAMESPACE_SCHEMA_LOCATION_DOC;
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

	public static void computeValueCompletionResponses(ICompletionRequest request, 
			ICompletionResponse response, Range editRange, DOMDocument document, SharedSettings settings) throws BadLocationException {
		
		int offset = document.offsetAt(editRange.getStart());
		DOMElement nodeAtOffset = (DOMElement) document.findNodeAt(offset);
		
		String actualPrefix = document.getSchemaInstancePrefix();
		
		// Value completion for 'nil' attribute
		DOMAttr nilAttr = nodeAtOffset.getAttributeNode(actualPrefix, "nil");
		if(nilAttr != null) {
			createCompletionItemsForValues(StringUtils.TRUE_FALSE_ARRAY, editRange, document, response, settings);
		}
	}

	private static void createSingleCompletionItemForValue(String value, Range editRange, DOMDocument document, ICompletionResponse response, SharedSettings settings) {
		createCompletionItemsForValues(Arrays.asList(value), editRange, document, response, settings);
	}

	private static void createCompletionItemsForValues(Collection<String> enumerationValues, Range editRange, DOMDocument document, ICompletionResponse response, SharedSettings settings) {
		
		// Figure out which quotation to use for filter text
		String settingQuotation = settings.formattingSettings.getQuotationAsString();
		String currentQuotation = settingQuotation;
		try {
			int start = document.offsetAt(editRange.getStart());
			int end = document.offsetAt(editRange.getEnd());
			if(start != end) {
				currentQuotation = String.valueOf(document.getText().charAt(start));
			}
		} catch (BadLocationException e) {
		}

		CompletionItem item;
		for (String option : enumerationValues) {
			String optionWithQuotes = settingQuotation + option + settingQuotation;
			item = new CompletionItem();
			item.setLabel(option);
			item.setFilterText(currentQuotation + option + currentQuotation);
			item.setKind(CompletionItemKind.Enum);
			item.setTextEdit(new TextEdit(editRange, optionWithQuotes));
			response.addCompletionItem(item);
		}
	}

	/**
	 * Checks if a given root has an attribute with the name:
	 * 	{@code prefix:suffix}.
	 * If no prefix exists, put the name in {@code suffix}
	 */
	private static boolean hasAttribute(DOMElement root, String prefix, String suffix) {
		return root.getAttributeNode(prefix, suffix) != null;
		
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