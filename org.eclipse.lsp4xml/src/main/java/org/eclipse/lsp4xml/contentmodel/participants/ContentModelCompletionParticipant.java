/**
 *  Copyright (c) 2018 Angelo ZERR
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.lsp4xml.contentmodel.participants;

import java.util.Collection;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.contentmodel.model.CMAttributeDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.CMDocument;
import org.eclipse.lsp4xml.contentmodel.model.CMElementDeclaration;
import org.eclipse.lsp4xml.contentmodel.model.ContentModelManager;
import org.eclipse.lsp4xml.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.dom.Element;
import org.eclipse.lsp4xml.dom.Node;
import org.eclipse.lsp4xml.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;

/**
 * Extension to support XML completion based on content model (XML Schema
 * completion, etc)
 */
public class ContentModelCompletionParticipant extends CompletionParticipantAdapter {

	@Override
	public void onTagOpen(ICompletionRequest request, ICompletionResponse response) throws Exception {
		Node parentNode = request.getParentNode();
		if (parentNode == null || !parentNode.isElement()) {
			return;
		}
		Element parentElement = (Element) parentNode;
		CMElementDeclaration cmElement = ContentModelManager.getInstance().findCMElement(parentElement);
		String defaultPrefix = null;
		if (cmElement != null) {
			defaultPrefix = parentElement.getPrefix();
			fillWithChildrenElementDeclaration(parentElement, cmElement.getElements(), defaultPrefix, request,
					response);
		}
		if (parentElement.isDocumentElement()) {
			// root document element
			Collection<String> prefixes = parentElement.getAllPrefixes();
			for (String prefix : prefixes) {
				if (defaultPrefix != null && prefix.equals(defaultPrefix)) {
					continue;
				}
				String namespaceURI = parentElement.getNamespaceURI(prefix);
				CMDocument cmDocument = ContentModelManager.getInstance().findCMDocument(parentElement, namespaceURI);
				if (cmDocument != null) {
					fillWithChildrenElementDeclaration(parentElement, cmDocument.getElements(), prefix, request,
							response);
				}
			}
		}
	}

	private void fillWithChildrenElementDeclaration(Element element, Collection<CMElementDeclaration> cmElements,
			String prefix, ICompletionRequest request, ICompletionResponse response) throws BadLocationException {
		XMLGenerator generator = request.getXMLGenerator();
		for (CMElementDeclaration child : cmElements) {
			String label = child.getName(prefix);
			CompletionItem item = new CompletionItem(label);
			item.setFilterText(request.getFilterForStartTagName(label));
			item.setKind(CompletionItemKind.Property);
			String documentation = child.getDocumentation();
			if (documentation != null) {
				item.setDetail(documentation);
			}
			String xml = generator.generate(child, prefix);
			item.setTextEdit(new TextEdit(request.getReplaceRange(), xml));
			item.setInsertTextFormat(InsertTextFormat.Snippet);
			response.addCompletionItem(item);
		}
	}

	@Override
	public void onAttributeName(boolean generateValue, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) throws Exception {
		Node parentNode = request.getParentNode();
		if (parentNode == null || !parentNode.isElement()) {
			return;
		}
		boolean canSupportSnippet = request.getCompletionSettings().isCompletionSnippetsSupported();
		Element parentElement = (Element) parentNode;
		CMElementDeclaration cmElement = ContentModelManager.getInstance().findCMElement(parentElement);
		if (cmElement != null) {
			Collection<CMAttributeDeclaration> attributes = cmElement.getAttributes();
			if (attributes != null) {
				for (CMAttributeDeclaration cmAttribute : attributes) {
					String attrName = cmAttribute.getName();
					if (!parentElement.hasAttribute(attrName)) {
						CompletionItem item = new CompletionItem();
						item.setLabel(attrName);
						item.setKind(CompletionItemKind.Value);
						StringBuilder attributeContent = new StringBuilder(attrName);
						if (generateValue) {
							attributeContent.append("=\"");
							String defaultValue = cmAttribute.getDefaultValue();
							if (defaultValue == null) {
								if (canSupportSnippet) {
									attributeContent.append("$1");
								}
							} else {
								if (canSupportSnippet) {
									attributeContent.append("${1:");
								}
								attributeContent.append(defaultValue);
								if (canSupportSnippet) {
									attributeContent.append("}");
								}
							}
							attributeContent.append("\"");
							if (canSupportSnippet) {
								attributeContent.append("$0");
							}
						}
						item.setTextEdit(new TextEdit(fullRange, attributeContent.toString()));
						item.setInsertTextFormat(InsertTextFormat.Snippet);
						String documentation = cmAttribute.getDocumentation();
						if (documentation != null) {
							item.setDetail(documentation);
						}
						response.addCompletionAttribute(item);
					}
				}
			}
		}

	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, boolean addQuotes, ICompletionRequest request,
			ICompletionResponse response) throws Exception {
		Node parentNode = request.getParentNode();
		if (parentNode == null || !parentNode.isElement()) {
			return;
		}
		Element parentElement = (Element) parentNode;
		CMElementDeclaration cmElement = ContentModelManager.getInstance().findCMElement(parentElement);
		if (cmElement != null) {
			String attributeName = request.getCurrentAttributeName();
			CMAttributeDeclaration cmAttribute = cmElement.findCMAttribute(attributeName);
			if (cmAttribute != null) {
				cmAttribute.getEnumerationValues().forEach(value -> {
					CompletionItem item = new CompletionItem();
					item.setLabel(value);
					item.setKind(CompletionItemKind.Value);
					response.addCompletionAttribute(item);
				});
			}
		}
	}
}
