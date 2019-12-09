/*******************************************************************************
* Copyright (c) 2019 Red Hat Inc. and others.
* All rights reserved. This program and the accompanying materials
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/el-v20.html
*
* Contributors:
*     Red Hat Inc. - initial API and implementation
*******************************************************************************/

package org.eclipse.lsp4xml.extensions.contentmodel.participants.codeactions;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.CodeActionFactory;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMElement;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.extensions.contentmodel.participants.XMLSyntaxErrorCode;
import org.eclipse.lsp4xml.services.extensions.ICodeActionParticipant;
import org.eclipse.lsp4xml.services.extensions.IComponentProvider;
import org.eclipse.lsp4xml.settings.XMLFormattingOptions;

/**
 * MarkupEntityMismatchCodeAction is a code action that triggers when the end tag of the
 * root element is missing. This will provide a codeaction that inserts that missing
 * end tag.
 */
public class MarkupEntityMismatchCodeAction implements ICodeActionParticipant {
	private static final Logger LOGGER = Logger.getLogger(MarkupEntityMismatchCodeAction.class.getName());


	@Override
	public void doCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
			XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		createEndTagInsertCodeAction(diagnostic, range, document, codeActions, formattingSettings, componentProvider);
	}

	public static void createEndTagInsertCodeAction(Diagnostic diagnostic, Range range, DOMDocument document, List<CodeAction> codeActions,
	XMLFormattingOptions formattingSettings, IComponentProvider componentProvider) {
		try {
			int offset = document.offsetAt(diagnostic.getRange().getStart());
			DOMNode node = document.findNodeAt(offset);
			if(!node.isElement()) {
				return;
			}
			
			DOMElement element = (DOMElement) node;
			int startOffset = element.getStartTagOpenOffset();
			if(startOffset == -1) {
				return;
			}
			Position startPosition = document.positionAt(startOffset);
			Position endPosition;
			
			XMLSyntaxErrorCode code = XMLSyntaxErrorCode.get(diagnostic.getCode());
			switch (code) {
				case MarkupEntityMismatch:
					endPosition = document.positionAt(document.getEnd());
					if (endPosition.getLine() > startPosition.getLine()) {
						endPosition.setCharacter(startPosition.getCharacter());
					}
					break;
				case ETagRequired:
					endPosition = document.positionAt(element.getStartTagCloseOffset() + 1);
					break;
				default:
					return;
			}

			String elementName = element.getTagName();
			CodeAction action = CodeActionFactory.insert("Close with '</" + elementName + ">'", endPosition, "</" + elementName + ">", document.getTextDocument(), diagnostic);
			codeActions.add(action);
		} catch (BadLocationException e) {
			LOGGER.log(Level.WARNING, "Exception while resolving the code action for " + diagnostic.getCode() + ":", e);
		}
	}

	
}