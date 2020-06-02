/*******************************************************************************
* Copyright (c) 2020 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v20.html
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/
package org.eclipse.lemminx.extensions.contentmodel.participants.codeactions;

import java.util.List;

import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.commons.CodeActionFactory;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMElement;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.LineIndentInfo;
import org.eclipse.lemminx.services.extensions.ICodeActionParticipant;
import org.eclipse.lemminx.services.extensions.IComponentProvider;
import org.eclipse.lemminx.settings.SharedSettings;
import org.eclipse.lemminx.utils.XMLPositionUtility;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

/**
 * Code action to fix close start tag element.
 *
 */
public class CloseStartTagCodeAction implements ICodeActionParticipant {

	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			SharedSettings sharedSettings, IComponentProvider componentProvider) {
		Range diagnosticRange = diagnostic.getRange();
		try {
			int startOffset = document.offsetAt(diagnosticRange.getStart());
			DOMNode node = document.findNodeAt(startOffset);
			if (node != null && node.isElement()) {
				int diagnosticEndOffset = document.offsetAt(diagnosticRange.getEnd());
				DOMElement element = (DOMElement) node;
				if (!element.hasStartTag()) {
					// </foo>
					DOMElement parent = element.getParentElement();
					if (parent != null && parent.getTagName() != null) {
						// <a><b></c>
						// Replace with 'b' closing tag
						String tagName = parent.getTagName();
						Range replaceRange = XMLPositionUtility.selectEndTagName(element);
						CodeAction replaceTagAction = CodeActionFactory.replace("Replace with '" + tagName + "' closing tag",
								replaceRange, tagName, document.getTextDocument(), diagnostic);
						codeActions.add(replaceTagAction);
					}
				} else {
					// <foo>
					boolean startTagClosed = element.isStartTagClosed();
					char c = document.getText().charAt(diagnosticEndOffset - 1);
					if (c != '/') {
						if (startTagClosed) {
							// ex : <foo attr="" >
							// // Close with '</$tag>
							String tagName = element.getTagName();
							if (tagName != null) {
								String label = "</" + tagName + ">";
								String insertText = label;
								Position endPosition = null;
								if (!element.hasChildNodes()) {
									int endOffset = element.getStartTagCloseOffset() + 1;
									endPosition = document.positionAt(endOffset);
								} else {
									String text = document.getText();
									// the element have some children(Text node, Element node, etc)
									int endOffset = element.getLastChild().getEnd() - 1;
									if (endOffset < text.length()) {
										// remove whitespaces
										char ch = text.charAt(endOffset);
										while (Character.isWhitespace(ch)) {
											endOffset--;
											ch = text.charAt(endOffset);
										}
									}
									endOffset++;
									endPosition = document.positionAt(endOffset);
									if (hasElements(element)) {
										// The element have element node as children
										// the </foo> must be inserted with a new line and indent
										LineIndentInfo indentInfo = document
												.getLineIndentInfo(diagnosticRange.getStart().getLine());
										insertText = indentInfo.getLineDelimiter() + indentInfo.getWhitespacesIndent()
												+ insertText;
									}
								}
								CodeAction closeEndTagAction = CodeActionFactory.insert("Close with '" + label + "'",
										endPosition, insertText, document.getTextDocument(), diagnostic);
								codeActions.add(closeEndTagAction);
							}

						} else {
							// ex : <foo attr="
							// Close with '/>
							CodeAction autoCloseAction = CodeActionFactory.insert("Close with '/>'",
									diagnosticRange.getEnd(), "/>", document.getTextDocument(), diagnostic);
							codeActions.add(autoCloseAction);
							// // Close with '></$tag>
							String tagName = element.getTagName();
							if (tagName != null) {
								String insertText = "></" + tagName + ">";
								CodeAction closeEndTagAction = CodeActionFactory.insert(
										"Close with '" + insertText + "'", diagnosticRange.getEnd(), insertText,
										document.getTextDocument(), diagnostic);
								codeActions.add(closeEndTagAction);
							}
						}
					}

					if (!startTagClosed) {
						// Close with '>
						CodeAction closeAction = CodeActionFactory.insert("Close with '>'", diagnosticRange.getEnd(),
								">", document.getTextDocument(), diagnostic);
						codeActions.add(closeAction);
					}
				}
			}
		} catch (BadLocationException e) {
			// do nothing
		}
	}

	/**
	 * Returns true if the given element has elements as children and false
	 * otherwise.
	 * 
	 * @param element the DOM element.
	 * 
	 * @return true if the given element has elements as children and false
	 *         otherwise.
	 */
	private static boolean hasElements(DOMElement element) {
		for (DOMNode node : element.getChildren()) {
			if (node.isElement()) {
				return true;
			}
		}
		return false;
	}

}
