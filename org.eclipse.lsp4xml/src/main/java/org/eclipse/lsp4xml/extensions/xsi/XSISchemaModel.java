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
package org.eclipse.lsp4xml.extensions.xsi;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.services.AttributeCompletionItem;
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
	public static final String XSI_WEBSITE = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String XSI_DOC = "The namespace that defines important attributes such as `noNamespaceSchemaLocation` and `schemaLocation`.";
	public static void computeCompletionResponses(ICompletionRequest request, 
			ICompletionResponse response, Range editRange, DOMDocument document, boolean generateValue, SharedSettings settings) throws BadLocationException {
		
		DOMElement rootElement = document.getDocumentElement();
		int offset = document.offsetAt(editRange.getStart());
		if(rootElement == null || offset <= rootElement.getStart() || offset >= rootElement.getEnd()) {
			return;
		}
		boolean inRootElement = false;
		DOMNode nodeAtOffset =  document.findNodeAt(offset);
		DOMElement elementAtOffset;
		if(nodeAtOffset != null && nodeAtOffset.isElement()) {
			elementAtOffset = (DOMElement) nodeAtOffset;
		}
		else {
			return;
		}
		if(rootElement.equals(nodeAtOffset)) {
			inRootElement = true;
		}

		boolean isSnippetsSupported = request.getCompletionSettings().isCompletionSnippetsSupported();
		if(inRootElement) {
			if(!hasAttribute(elementAtOffset, "xmlns") && !response.hasAttribute("xmlns")) { // "xmlns" completion
				createCompletionItem("xmlns", isSnippetsSupported, generateValue, editRange, null, null, null, response, settings);
			}
			if(document.hasSchemaInstancePrefix() == false) { // "xmlns:xsi" completion
				createCompletionItem("xmlns:xsi", isSnippetsSupported, generateValue, editRange, XSI_WEBSITE, null, XSI_DOC, response, settings);
				return;// All the following completion cases dont exist, so return.
			}
		}
		
		String actualPrefix = document.getSchemaInstancePrefix();
		if(actualPrefix == null) {
			return;
		}
		
		String name;
		String documentation;
		
		boolean schemaLocationExists = document.hasSchemaLocation();
		boolean noNamespaceSchemaLocationExists = document.hasNoNamespaceSchemaLocation();
		//Indicates that no values are allowed inside an XML element
		if(!hasAttribute(elementAtOffset, actualPrefix, "nil")) {
			documentation = NIL_DOC;
			name = actualPrefix + ":nil";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, StringUtils.TRUE, 
				StringUtils.TRUE_FALSE_ARRAY, documentation, response, settings);
		}
		//Signals that an element should be accepted as ·valid· when it has no content despite 
		//a content type which does not require or even necessarily allow empty content. 
		//An element may be ·valid· without content if it has the attribute xsi:nil with 
		//the value true.
		if(!hasAttribute(elementAtOffset, actualPrefix, "type")) {
			documentation = TYPE_DOC;
			name = actualPrefix + ":type";
			createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response, settings);	
		}
		
		if(inRootElement) {
			if(!schemaLocationExists && !noNamespaceSchemaLocationExists) {
				//The xsi:schemaLocation and xsi:noNamespaceSchemaLocation attributes can be used in a document 
				//to provide hints as to the physical location of schema documents which may be used for ·assessment·.
				documentation = SCHEMA_LOCATION_DOC;
				name = actualPrefix + ":schemaLocation";
				createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response, settings);	
				
				documentation = NO_NAMESPACE_SCHEMA_LOCATION_DOC;
				name = actualPrefix + ":noNamespaceSchemaLocation";
				createCompletionItem(name, isSnippetsSupported, generateValue, editRange, null, null, documentation, response, settings);	
			}	
		}
	}

	private static void createCompletionItem(String attrName, boolean canSupportSnippet, boolean generateValue,
			Range editRange, String defaultValue, Collection<String> enumerationValues, String documentation,
			ICompletionResponse response, SharedSettings settings){
		CompletionItem item = new AttributeCompletionItem(attrName, canSupportSnippet, editRange, generateValue,
				defaultValue, enumerationValues, settings);
		MarkupContent markup = new MarkupContent();
		markup.setKind(MarkupKind.MARKDOWN);
		markup.setValue(StringUtils.getDefaultString(documentation));
		item.setDocumentation(markup);
		response.addCompletionItem(item);
	}

	public static void computeValueCompletionResponses(ICompletionRequest request, 
			ICompletionResponse response, Range editRange, DOMDocument document, SharedSettings settings) throws BadLocationException {
		
		int offset = document.offsetAt(editRange.getStart());
		DOMNode nodeAtOffset = document.findNodeAt(offset);
		
		String actualPrefix = document.getSchemaInstancePrefix();
		DOMAttr attrAtOffset = nodeAtOffset.findAttrAt(offset);
		String attrName = attrAtOffset.getName();
		
		if(attrName != null) {
			if(actualPrefix != null && attrName.equals(actualPrefix + ":nil")) { // Value completion for 'nil' attribute
				createCompletionItemsForValues(StringUtils.TRUE_FALSE_ARRAY, editRange, document, response, settings);
			}
			else if(document.getDocumentElement() != null && document.getDocumentElement().equals(nodeAtOffset)) { // if in the root element
				if(attrName.equals("xmlns:xsi")) {
					createSingleCompletionItemForValue(XSI_WEBSITE, editRange, document, response, settings);
				}
			}
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

	private static boolean hasAttribute(DOMElement root, String name) {
		return hasAttribute(root, null, name);
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