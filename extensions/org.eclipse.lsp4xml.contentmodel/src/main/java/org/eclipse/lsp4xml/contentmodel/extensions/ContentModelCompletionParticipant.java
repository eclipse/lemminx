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
package org.eclipse.lsp4xml.contentmodel.extensions;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.contentmodel.CMElement;
import org.eclipse.lsp4xml.contentmodel.ContentModelManager;
import org.eclipse.lsp4xml.contentmodel.utils.XMLGenerator;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;
import org.eclipse.lsp4xml.services.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.services.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.services.extensions.ICompletionResponse;

/**
 * Extension to support XML completion based on content model (XML Schema
 * completion, etc)
 */
public class ContentModelCompletionParticipant implements ICompletionParticipant {

	@Override
	public void onTagOpen(ICompletionRequest request, ICompletionResponse response) throws Exception {
		Node element = request.getParentNode();
		CMElement cmlElement = ContentModelManager.getInstance().findCMElement(element);
		if (cmlElement != null) {
			XMLDocument document = element.getOwnerDocument();
			int lineNumber = request.getPosition().getLine();
			String lineText = document.lineText(lineNumber);
			String lineDelimiter = document.lineDelimiter(lineNumber);
			String whitespacesIndent = getStartWhitespaces(lineText);

			XMLGenerator generator = new XMLGenerator(request.getFormattingSettings(), whitespacesIndent,
					lineDelimiter);
			for (CMElement child : cmlElement.getElements()) {
				String tag = child.getName();
				if (!element.hasTag(tag)) {
					String label = child.getName();
					CompletionItem item = new CompletionItem(label);
					item.setFilterText(label);
					item.setKind(CompletionItemKind.Property);
					String documentation = child.getDocumentation();
					if (documentation != null) {
						item.setDetail(documentation);
					}
					String xml = generator.generate(child);
					// Remove the first '<' character
					xml = xml.substring(1, xml.length());
					item.setTextEdit(new TextEdit(request.getReplaceRange(), xml));
					item.setInsertTextFormat(InsertTextFormat.Snippet);
					response.addCompletionItem(item);
				}
			}
		}
	}

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) throws Exception {
		// Do nothing
	}

	@Override
	public void onAttributeName(String namePrefix, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) {

	}

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) {
	}

	private static String getStartWhitespaces(String lineText) {
		StringBuilder whitespaces = new StringBuilder();
		char[] chars = lineText.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			char c = chars[i];
			if (Character.isWhitespace(c)) {
				whitespaces.append(c);
			} else {
				break;
			}
		}
		return whitespaces.toString();
	}
}
