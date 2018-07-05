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
package org.eclipse.lsp4xml.contentmodel;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4xml.extensions.ICompletionParticipant;
import org.eclipse.lsp4xml.extensions.ICompletionRequest;
import org.eclipse.lsp4xml.extensions.ICompletionResponse;
import org.eclipse.lsp4xml.model.Node;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * Extension to support XML completion based on content model (XML Schema completion,
 * etc)
 */
public class ContentModelCompletionParticipant implements ICompletionParticipant {

	@Override
	public void onXMLContent(ICompletionRequest request, ICompletionResponse response) {
		try {
			Node parentNode = request.getParentNode();
			CMElement cmlElement = ContentModelManager.getInstance().findCMElement(parentNode);
			if (cmlElement != null) {

				XMLDocument document = parentNode.getOwnerDocument();
				int lineNumber = request.getPosition().getLine();
				String lineText = document.lineText(lineNumber);
				String lineDelimiter = document.lineDelimiter(lineNumber);
				String whitespacesIndent = getStartWhitespaces(lineText);

				XMLGenerator generator = new XMLGenerator(request.getFormattingSettings(), whitespacesIndent,
						lineDelimiter);
				for (CMElement child : cmlElement.getElements()) {
					CompletionItem item = new CompletionItem(child.getName());
					item.setKind(CompletionItemKind.Property);
					String documentation = child.getDocumentation();
					if (documentation != null) {
						item.setDetail(documentation);
					}
					String xml = generator.generate(child);
					item.setTextEdit(new TextEdit(new Range(request.getPosition(), request.getPosition()), xml));
					item.setInsertTextFormat(InsertTextFormat.Snippet);
					response.addCompletionItem(item);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	@Override
	public void onAttributeValue(String valuePrefix, Range fullRange, ICompletionRequest request,
			ICompletionResponse response) {
	}

}
