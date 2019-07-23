/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lsp4xml.extensions.dtd.participants;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.parsers.DTDParser;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DTDAttlistDecl;
import org.eclipse.lsp4xml.dom.DTDDeclParameter;
import org.eclipse.lsp4xml.dom.DTDElementDecl;
import org.eclipse.lsp4xml.extensions.dtd.utils.DTDUtils;
import org.eclipse.lsp4xml.extensions.xsd.DataType;
import org.eclipse.lsp4xml.extensions.xsd.utils.XSDUtils;
import org.eclipse.lsp4xml.extensions.xsd.utils.XSDUtils.BindingType;
import org.eclipse.lsp4xml.services.XMLCompletions;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.utils.DOMUtils;
import org.eclipse.lsp4xml.utils.XMLPositionUtility;

/**
 * DTD completion for
 * 
 * <ul>
 * <li><!ELEMENT element-name | -> to provide other <!ELEMENT</li> *
 * <li><!ATTRLIST | -> to provide <!ELEMENT</li>
 * </ul>
 *
 */
public class DTDCompletionParticipant extends CompletionParticipantAdapter {

	private static final Logger LOGGER = Logger.getLogger(DTDCompletionParticipant.class.getName());

	@Override
	public void onDTDContent(ICompletionRequest request, ICompletionResponse response, boolean isContent)
			throws Exception {
		DTDDeclParameter parameter = null;
		DOMNode node = request.getNode();
		int offset = request.getOffset();
		if (node.isDTDElementDecl()) {
			DTDElementDecl elementDecl = (DTDElementDecl) node;
			parameter = elementDecl.getParameterAt(offset);			
		} else if (node.isDTDAttListDecl()) {
			DTDAttlistDecl attlistDecl = (DTDAttlistDecl) node;
			if (attlistDecl.isInElementName(offset)) {
				parameter = attlistDecl.getElementNameNode();
			}
		}

		if (parameter != null) {
			Range fullRange = XMLPositionUtility.createRange(parameter);
			DTDUtils.searchDTDTargetElementDecl(parameter, false, targetElement -> {
				CompletionItem item = new CompletionItem();
				String value = targetElement.getParameter();
				String insertText = value;
				item.setLabel(value);
				item.setKind(CompletionItemKind.Value);
				item.setFilterText(insertText);
				item.setTextEdit(new TextEdit(fullRange, insertText));
				response.addCompletionItem(item);
			});
			return;
		}
		// Insert DTD Element Declaration
		// see https://www.w3.org/TR/REC-xml/#dt-eldecl
		boolean isSnippetsSupported = request.isCompletionSnippetsSupported();
		InsertTextFormat insertFormat = request.getInsertTextFormat();
		CompletionItem elementDecl = new CompletionItem();
		elementDecl.setLabel("Insert DTD Element declaration");
		elementDecl.setKind(CompletionItemKind.EnumMember);
		elementDecl.setFilterText("<!ELEMENT ");
		elementDecl.setInsertTextFormat(insertFormat);
		int startOffset = request.getOffset();
		Range editRange = null;
		DOMDocument document = request.getXMLDocument();
//		DOMNode node = document.findNodeAt(startOffset);
		try {
			if (node.isDoctype()) {
				editRange = XMLCompletions.getReplaceRange(startOffset, startOffset, request);
			} else {
				if (isContent) {
					editRange = document.getTrimmedRange(node.getStart(), node.getEnd());
				}
				if (editRange == null) {
					editRange = XMLCompletions.getReplaceRange(node.getStart(), node.getEnd(), request);
				}
			}
		} catch (BadLocationException e) {
			LOGGER.log(Level.SEVERE, "While performing getReplaceRange for DTD completion.", e);
		}
		String textEdit = isSnippetsSupported ? "<!ELEMENT ${1:element-name} (${2:#PCDATA})>"
				: "<!ELEMENT element-name (#PCDATA)>";
		elementDecl.setTextEdit(new TextEdit(editRange, textEdit));
		elementDecl.setDocumentation("<!ELEMENT element-name (#PCDATA)>");
		response.addCompletionItem(elementDecl);

		// Insert DTD AttrList Declaration
		// see https://www.w3.org/TR/REC-xml/#attdecls
		CompletionItem attrListDecl = new CompletionItem();
		attrListDecl.setLabel("Insert DTD Attributes list declaration");
		attrListDecl.setKind(CompletionItemKind.EnumMember);
		attrListDecl.setFilterText("<!ATTLIST ");
		attrListDecl.setInsertTextFormat(insertFormat);
		startOffset = request.getOffset();

		textEdit = isSnippetsSupported ? "<!ATTLIST ${1:element-name} ${2:attribute-name} ${3:ID} ${4:#REQUIRED}>"
				: "<!ATTLIST element-name attribute-name ID #REQUIRED>";
		attrListDecl.setTextEdit(new TextEdit(editRange, textEdit));
		attrListDecl.setDocumentation("<!ATTLIST element-name attribute-name ID #REQUIRED>");
		response.addCompletionItem(attrListDecl);

		// Insert Internal DTD Entity Declaration
		// see https://www.w3.org/TR/REC-xml/#dt-entdecl
		CompletionItem internalEntity = new CompletionItem();
		internalEntity.setLabel("Insert Internal DTD Entity declaration");
		internalEntity.setKind(CompletionItemKind.EnumMember);
		internalEntity.setFilterText("<!ENTITY ");
		internalEntity.setInsertTextFormat(insertFormat);
		startOffset = request.getOffset();

		textEdit = isSnippetsSupported ? "<!ENTITY ${1:entity-name} \"${2:entity-value}\">"
				: "<!ENTITY entity-name \"entity-value\">";
		internalEntity.setTextEdit(new TextEdit(editRange, textEdit));
		internalEntity.setDocumentation("<!ENTITY entity-name \"entity-value\">");
		response.addCompletionItem(internalEntity);

		// Insert External DTD Entity Declaration
		// see https://www.w3.org/TR/REC-xml/#dt-entdecl
		CompletionItem externalEntity = new CompletionItem();
		externalEntity.setLabel("Insert External DTD Entity declaration");
		externalEntity.setKind(CompletionItemKind.EnumMember);
		externalEntity.setFilterText("<!ENTITY ");
		externalEntity.setInsertTextFormat(insertFormat);
		startOffset = request.getOffset();

		textEdit = isSnippetsSupported ? "<!ENTITY ${1:entity-name} SYSTEM \"${2:entity-value}\">"
				: "<!ENTITY entity-name SYSTEM \"entity-value\">";
		externalEntity.setTextEdit(new TextEdit(editRange, textEdit));
		externalEntity.setDocumentation("<!ENTITY entity-name SYSTEM \"entity-value\">");
		response.addCompletionItem(externalEntity);

	}
	
	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response)
			throws Exception {
		DOMNode node = request.getNode();
		DOMDocument document = node.getOwnerDocument();
		if (!DOMUtils.isXSD(document)) {
			return;
		}
		Range fullRange = request.getReplaceRange();
		DOMAttr originAttr = node.findAttrAt(request.getOffset());
		BindingType bindingType = XSDUtils.getBindingType(originAttr);
		if (bindingType != BindingType.NONE) {
			// Completion on
			// - @type (ex : xs:element/@type)
			// - @base (ex : xs:extension/@base)
			// bound to complextTypes/@name
			XSDUtils.searchXSTargetAttributes(originAttr, bindingType, false, (targetNamespacePrefix, targetAttr) -> {
				CompletionItem item = new CompletionItem();
				item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, DataType.getDocumentation(targetAttr)));
				String value = createComplexTypeValue(targetAttr, targetNamespacePrefix);
				String insertText = request.getInsertAttrValue(value);
				item.setLabel(value);
				item.setKind(CompletionItemKind.Value);
				item.setFilterText(insertText);
				item.setTextEdit(new TextEdit(fullRange, insertText));
				response.addCompletionItem(item);
			});
			if (bindingType.isSimple()) {
				// Completion on @type (ex : xs:element/@type) bound to Built-in types (ex:
				// xs:string) ->
				// https://www.w3.org/TR/xmlschema11-2/#built-in-datatypes
				String prefix = document.getSchemaPrefix();
				DataType.getDataTypes().forEach(dataType -> {
					CompletionItem item = new CompletionItem();
					item.setDocumentation(new MarkupContent(MarkupKind.MARKDOWN, dataType.getDocumentation()));
					String value = createDatatypeValue(dataType, prefix);
					String insertText = request.getInsertAttrValue(value);
					item.setLabel(value);
					item.setKind(CompletionItemKind.Value);
					item.setFilterText(insertText);
					item.setTextEdit(new TextEdit(fullRange, insertText));
					response.addCompletionItem(item);
				});
			}
		}
	}

	private static String createComplexTypeValue(DOMAttr targetAttr, String targetNamespacePrefix) {
		StringBuilder value = new StringBuilder();
		if (targetNamespacePrefix != null) {
			value.append(targetNamespacePrefix);
			value.append(":");
		}
		value.append(targetAttr.getValue());
		return value.toString();
	}

	private static String createDatatypeValue(DataType dataType, String prefix) {
		StringBuilder value = new StringBuilder();
		if (prefix != null) {
			value.append(prefix);
			value.append(":");
		}
		value.append(dataType.getName());
		return value.toString();
	}

}
