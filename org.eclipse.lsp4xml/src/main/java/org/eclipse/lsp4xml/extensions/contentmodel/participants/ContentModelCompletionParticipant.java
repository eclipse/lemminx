/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.extensions.contentmodel.participants;

import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.extensions.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.extensions.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.extensions.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.services.AttributeCompletionItem;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;
import org.eclipse.lsp4xml.uriresolver.CacheResourceDownloadingException;

/**
 * Extension to support XML completion based on content model (XML Schema
 * completion, etc)
 */
public class ContentModelCompletionParticipant extends CompletionParticipantAdapter {

	@Override
	public void onTagOpen(ICompletionRequest request, ICompletionResponse response) throws Exception {
		try {
			DOMDocument document = request.getXMLDocument();
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			DOMElement parentElement = request.getParentElement();
			if (parentElement == null) {
				// XML is empty, in case of XML file associations, a XMl Schema/DTD can be bound
				// check if it's root element (in the case of XML file associations, the link to
				// XML Schema is done with pattern and not with XML root element)
				CMDocument cmDocument = contentModelManager.findCMDocument(document, null);
				if (cmDocument != null) {
					fillWithChildrenElementDeclaration(null, cmDocument.getElements(), null, false, request, response);
				}
				return;
			}
			// Try to retrieve XML Schema/DTD element declaration for the parent element
			// where completion was triggered.
			final CMDocument cmRootDocument = contentModelManager.findCMDocument(parentElement,
					parentElement.getNamespaceURI());

			CMElementDeclaration cmElement = contentModelManager.findCMElement(parentElement);
			String defaultPrefix = null;

			if (cmElement != null) {
				defaultPrefix = parentElement.getPrefix();
				fillWithChildrenElementDeclaration(parentElement,
						cmElement.getPossibleElements(parentElement, request.getOffset()), defaultPrefix, false,
						request, response);
			}
			if (parentElement.isDocumentElement()) {
				// completion on root document element
				Collection<String> prefixes = parentElement.getAllPrefixes();
				for (String prefix : prefixes) {
					if (defaultPrefix != null && prefix.equals(defaultPrefix)) {
						continue;
					}
					String namespaceURI = parentElement.getNamespaceURI(prefix);
					if (cmRootDocument == null || !cmRootDocument.hasNamespace(namespaceURI)) {
						// The model document root doesn't define the namespace, try to load the
						// external model document (XML Schema, DTD)
						CMDocument cmDocument = contentModelManager.findCMDocument(parentElement, namespaceURI);
						if (cmDocument != null) {
							fillWithChildrenElementDeclaration(parentElement, cmDocument.getElements(), prefix, true,
									request, response);
						}
					}
				}
			}
			// Completion on tag based on internal content model (ex : internal DTD declared
			// in XML)
			CMElementDeclaration cmInternalElement = contentModelManager.findInternalCMElement(parentElement);
			if (cmInternalElement != null) {
				defaultPrefix = parentElement.getPrefix();
				fillWithChildrenElementDeclaration(parentElement,
						cmInternalElement.getPossibleElements(parentElement, request.getOffset()), defaultPrefix, false,
						request, response);
			}
		} catch (CacheResourceDownloadingException e) {
			// XML Schema, DTD is loading, ignore this error
		}
	}

	private void fillWithChildrenElementDeclaration(DOMElement element, Collection<CMElementDeclaration> cmElements,
			String p, boolean forceUseOfPrefix, ICompletionRequest request, ICompletionResponse response)
			throws BadLocationException {
		XMLGenerator generator = request.getXMLGenerator();
		for (CMElementDeclaration child : cmElements) {
			String prefix = forceUseOfPrefix ? p : (element != null ? element.getPrefix(child.getNamespace()) : null);
			String label = child.getName(prefix);
			CompletionItem item = new CompletionItem(label);
			item.setFilterText(request.getFilterForStartTagName(label));
			item.setKind(CompletionItemKind.Property);
			MarkupContent documentation = XMLGenerator.createMarkupContent(child, request);
			item.setDocumentation(documentation);
			String xml = generator.generate(child, prefix);
			item.setTextEdit(new TextEdit(request.getReplaceRange(), xml));
			item.setInsertTextFormat(InsertTextFormat.Snippet);
			response.addCompletionItem(item, true);
		}
	}

	@Override
	public void onAttributeName(boolean generateValue, ICompletionRequest request, ICompletionResponse response)
			throws Exception {
		// otherwise, manage completion based on XML Schema, DTD.
		DOMElement parentElement = request.getNode().isElement() ? (DOMElement) request.getNode() : null;
		if (parentElement == null) {
			return;
		}
		try {
			Range fullRange = request.getReplaceRange();
			boolean canSupportSnippet = request.isCompletionSnippetsSupported();
			XMLFormattingOptions formattingSettings = request.getFormattingSettings();
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			// Completion on attribute based on external grammar
			CMElementDeclaration cmElement = contentModelManager.findCMElement(parentElement);
			fillAttributesWithCMAttributeDeclarations(parentElement, fullRange, cmElement, canSupportSnippet,
					generateValue, request, response, formattingSettings);
			// Completion on attribute based on internal grammar
			cmElement = contentModelManager.findInternalCMElement(parentElement);
			fillAttributesWithCMAttributeDeclarations(parentElement, fullRange, cmElement, canSupportSnippet,
					generateValue, request, response, formattingSettings);
		} catch (CacheResourceDownloadingException e) {
			// XML Schema, DTD is loading, ignore this error
		}
	}

	private void fillAttributesWithCMAttributeDeclarations(DOMElement parentElement, Range fullRange,
			CMElementDeclaration cmElement, boolean canSupportSnippet, boolean generateValue,
			ICompletionRequest request, ICompletionResponse response, XMLFormattingOptions formattingOptions) {
		if (cmElement == null) {
			return;
		}
		Collection<CMAttributeDeclaration> attributes = cmElement.getAttributes();
		if (attributes == null) {
			return;
		}
		for (CMAttributeDeclaration cmAttribute : attributes) {
			String attrName = cmAttribute.getName();
			if (!parentElement.hasAttribute(attrName)) {
				CompletionItem item = new AttributeCompletionItem(attrName, canSupportSnippet, fullRange, generateValue,
						cmAttribute.getDefaultValue(), cmAttribute.getEnumerationValues(), formattingOptions);
				MarkupContent documentation = XMLGenerator.createMarkupContent(cmAttribute, cmElement, request);
				item.setDocumentation(documentation);
				response.addCompletionAttribute(item);
			}
		}
	}

	@Override
	public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response)
			throws Exception {
		DOMElement parentElement = request.getNode().isElement() ? (DOMElement) request.getNode() : null;
		if (parentElement == null) {
			return;
		}
		try {
			ContentModelManager contentModelManager = request.getComponent(ContentModelManager.class);
			// Completion on attribute values based on external grammar
			CMElementDeclaration cmElement = contentModelManager.findCMElement(parentElement);
			fillAttributeValuesWithCMAttributeDeclarations(cmElement, request, response);
			// Completion on attribute values based on internal grammar
			cmElement = contentModelManager.findInternalCMElement(parentElement);
			fillAttributeValuesWithCMAttributeDeclarations(cmElement, request, response);
		} catch (CacheResourceDownloadingException e) {
			// XML Schema, DTD is loading, ignore this error
		}
	}

	private void fillAttributeValuesWithCMAttributeDeclarations(CMElementDeclaration cmElement,
			ICompletionRequest request, ICompletionResponse response) {
		if (cmElement != null) {
			String attributeName = request.getCurrentAttributeName();
			CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
			if (cmAttribute != null) {
				Range fullRange = request.getReplaceRange();
				cmAttribute.getEnumerationValues().forEach(value -> {
					CompletionItem item = new CompletionItem();
					item.setLabel(value);
					String insertText = request.getInsertAttrValue(value);
					item.setLabel(value);
					item.setKind(CompletionItemKind.Value);
					item.setFilterText(insertText);
					item.setTextEdit(new TextEdit(fullRange, insertText));
					MarkupContent documentation = XMLGenerator.createMarkupContent(cmAttribute, value, cmElement,
							request);
					item.setDocumentation(documentation);
					response.addCompletionItem(item);
				});
			}
		}
	}

}