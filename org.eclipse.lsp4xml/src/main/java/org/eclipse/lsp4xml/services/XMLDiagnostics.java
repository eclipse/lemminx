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
package org.eclipse.lsp4xml.services;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4xml.commons.BadLocationException;
import org.eclipse.lsp4xml.commons.TextDocument;
import org.eclipse.lsp4xml.extensions.IDiagnosticsParticipant;
import org.eclipse.lsp4xml.extensions.XMLExtensionsRegistry;
import org.eclipse.lsp4xml.internal.parser.Scanner;
import org.eclipse.lsp4xml.internal.parser.Token;
import org.eclipse.lsp4xml.internal.parser.TokenType;
import org.eclipse.lsp4xml.internal.parser.XMLScanner;
import org.eclipse.lsp4xml.model.XMLDocument;

/**
 * XML diagnostics support.
 *
 */
class XMLDiagnostics {

	private final XMLExtensionsRegistry extensionsRegistry;
	private XMLDocument xmlDocument;

	public XMLDiagnostics(XMLExtensionsRegistry extensionsRegistry) {
		this.extensionsRegistry = extensionsRegistry;
	}

	public List<Diagnostic> doDiagnostics(TextDocument document, CancelChecker monitor, XMLDocument xmlDocument) {
		this.xmlDocument = xmlDocument;
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
		try {
			doBasicDiagnostics(document, diagnostics, monitor);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		doExtensionsDiagnostics(document, diagnostics, monitor);
		return diagnostics;
	}

	/**
	 * Do basic validation to check the no XML valid.
	 * 
	 * @param document
	 * @param diagnostics
	 * @param monitor
	 * @throws BadLocationException
	 */
	private void doBasicDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor)
			throws BadLocationException {
		Scanner scanner = XMLScanner.createScanner(document.getText());
		List previousRegion = null;
    List region = null;
    boolean isClosed = true;
		TokenType tokenType = scanner.scan();
		while (tokenType != TokenType.EOS) {
			monitor.checkCanceled();
			Token token = new Token(tokenType, scanner.getTokenText(), scanner.getTokenOffset(), scanner.getTokenEnd());
      isClosed = false;
      if ((tokenType == TokenType.Content) || (tokenType == TokenType.XML_CHAR_REFERENCE)
          || (tokenType == TokenType.Entity) || (tokenType == TokenType.StartProlog)
          || (tokenType == TokenType.StartTagOpen) || (tokenType == TokenType.EndTagOpen)
          || (tokenType == TokenType.StartCommentTag) || (tokenType == TokenType.CDATATagOpen)
          || (tokenType == TokenType.Declaration)) {
        // Validate the previous
        // Create a new Region
        previousRegion = region;
        region = new ArrayList(0);
        region.add(token);
        if (tokenType == TokenType.StartProlog) {
          checkContentBeforeProcessingInstruction(previousRegion, diagnostics);
        } else if (tokenType == TokenType.Content) {
           checkForSpaceBeforeName(token, previousRegion, reporter);
        }
        checkForTagClose(previousRegion, reporter);

      } else if ((tokenType == TokenType.XML_TAG_NAME) || (tokenType == TokenType.XML_TAG_ATTRIBUTE_NAME)
          || (tokenType == TokenType.XML_TAG_ATTRIBUTE_EQUALS) || (tokenType == TokenType.XML_TAG_ATTRIBUTE_VALUE)
          || (tokenType == TokenType.XML_COMMENT_TEXT) || (tokenType == TokenType.XML_PI_CONTENT)
          || (tokenType == TokenType.XML_DOCTYPE_INTERNAL_SUBSET)) {
        region.add(token);
      } else if ((tokenType == TokenType.XML_PI_CLOSE) || (tokenType == TokenType.XML_TAG_CLOSE)
          || (tokenType == TokenType.XML_EMPTY_TAG_CLOSE) || (tokenType == TokenType.XML_COMMENT_CLOSE)
          || (tokenType == TokenType.XML_DECLARATION_CLOSE) || (tokenType == TokenType.XML_CDATA_CLOSE)) {
        region.add(token);
        if (tokenType == TokenType.XML_PI_CLOSE) {
          checkNamespacesInProcessingInstruction(region, reporter);
        } else if (tokenType == TokenType.XML_TAG_CLOSE || tokenType == TokenType.XML_EMPTY_TAG_CLOSE) {
          checkEmptyTag(region, reporter);
          final int regionLength = region.size();
          if (regionLength > 0) {
            Token first = (Token) region.get(0);
            if (first.type == TokenType.XML_END_TAG_OPEN) {
              checkAttributsInEndTag(first, region, reporter);
              if (first.type == TokenType.XML_END_TAG_OPEN && tagStack != null) {
                if (regionLength > 1) {
                  Token name = (Token) region.get(1);
                  if (tagStack.isEmpty()) {
                    // We have an end tag without a start tag
                    createMissingTagError(name, false, reporter);
                  } else {
                    if (!((Token) tagStack.peek()).text.equals(name.text)) {
                      boolean wasFound = false;
                      final int stackSize = tagStack.size();
                      for (int i = stackSize - 1; i >= 0; i--) {
                        Token pointer = (Token) tagStack.get(i);
                        if (pointer.text.equals(name.text)) {
                          wasFound = true;
                          Token top = null;
                          // Found the opening tag - everything in between that was unclosed should be
                          // flagged
                          while (!tagStack.isEmpty() && !(top = (Token) tagStack.pop()).text.equals(pointer.text)) {
                            createMissingTagError(top, true, reporter);
                          }
                          break;
                        }
                      }
                      if (!wasFound) {
                        // End tag doesn't have a matching start
                        createMissingTagError(name, false, reporter);
                      }
                    } else {
                      // We've got a match
                      tagStack.pop();
                    }
                  }
                }
              }
            } else if (first.type == TokenType.XML_TAG_OPEN) {
              checkAttributes(region, reporter);
              if (tokenType == TokenType.XML_TAG_CLOSE && tagStack != null && regionLength > 1) {
                Token name = (Token) region.get(1);
                if (name.type == TokenType.XML_TAG_NAME) {
                  tagStack.push(name);
                }
              }
            }
          }
        }
        isClosed = true;
      }
			tokenType = scanner.scan();
		}
	}

	private void checkContentBeforeProcessingInstruction(List previousRegion, List<Diagnostic> diagnostics) {
		if (previousRegion != null && previousRegion.size() > 0) {
      Token first = (Token) previousRegion.get(0);
      if (first.type == TokenType.Content && first.startOffset == 0) {
        // XML declaration only allowed at the start of the document
				String messageText = XMLDiagnosticMessages.CONTENT_BEFORE_OPEN_TAG;
				createAndSetDiagnostic(first, messageText, diagnostics);
      }
    }
	}

	private void checkForSpaceBeforeName(Token token, List previousRegion, List<Diagnostic> diagnostics) {
    if (previousRegion != null && previousRegion.size() == 1) {
      // Check that the start tag's name comes right after the <
      Token first = (Token) previousRegion.get(0);
      if (TokenType.StartTagOpen.equals(first.type) && token.tokenText.trim().length() == 0) {
        final String messageText = XMLDiagnosticMessages.START_TAG_MISSING_NAME;
        createAndSetDiagnostic(first, messageText, diagnostics);
      }
    }
	}
	
	private void checkForTagClose(List previousRegion, List<Diagnostic> diagnostics) {
    if (previousRegion != null && previousRegion.size() > 0) {
      final Token first = (Token) previousRegion.get(0);
      // If the previous region was a start- or end-tag, look for the tag close
      if (first.type == TokenType.StartTagOpen || first.type == TokenType.EndTagOpen) {
        final int length = previousRegion.size();
        boolean isClosed = false;
        int textLength = first.length;
        for (int i = 1; i < length; i++) {
          Token t = (Token) previousRegion.get(i);
          // Valid tag closings, EMPTY_TAG_CLOSE only works for a start tag, though
          if ((t.type == TokenType.StartTagSelfClose && first.type == TokenType.StartTagOpen)
              || t.type == TokenType.StartTagClose) {
            isClosed = true;
            break;
          } else if (t.type == TokenType.StartTag) {
            textLength += t.length;
          }
        }
        if (!isClosed) {
          String messageText = XMLDiagnosticMessages.TAG_NOT_CLOSED;
					createAndSetDiagnostic(first, messageText, diagnostics);
          
        }
      }
    }
  }

	/**
	 * Do validation with extension (XML Schema, etc)
	 * 
	 * @param document
	 * @param diagnostics
	 * @param monitor
	 */
	private void doExtensionsDiagnostics(TextDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
		for (IDiagnosticsParticipant diagnosticsParticipant : extensionsRegistry.getDiagnosticsParticipants()) {
			diagnosticsParticipant.doDiagnostics(document, diagnostics, monitor);
		}
	}

	private Range createRange(Token token) {
		Position start;
		try {
			start = xmlDocument.positionAt(token.startOffset);
			Position end = xmlDocument.positionAt(token.endOffset);
			return new Range(start,end);
		} catch (BadLocationException e) {
			//TODO
		}
		return null;
	}

	private void createAndSetDiagnostic(Token token, String messageText, List<Diagnostic> diagnostics) {
		Range range = createRange(token);
		Diagnostic tempDiagnostic = new Diagnostic(range, messageText);
		diagnostics.add(tempDiagnostic);
	}

}
